package com.feeyo.buffer.page;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 大页内存池
 * 
 * @author zhuam
 *
 */
class Pages {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( Pages.class );
	//
    public static final int DEFAULT_CHUNK_SIZE = 128;
    public static final int DEFAULT_PAGE_SIZE = 512 * 1024 * 1024;		// 512MB
    //
    protected final long capacity;
	//
	protected BufferPage[] allPages;
    //
    protected AtomicInteger prevAllocatedPage = new AtomicInteger(0);
    protected int pageSize;
    protected int pageCount;
    protected int chunkSize;
    //
    protected AtomicLong usageSize = new AtomicLong(0);
    protected AtomicLong usageCount = new AtomicLong(0);
    protected AtomicLong sharedOptsCount = new AtomicLong(0);
    
    //
	private ByteOrder byteOrder;
    //
    public Pages(long capacity, int chunkSize) {
    	this.capacity = capacity;
    	this.chunkSize = ( chunkSize < DEFAULT_CHUNK_SIZE ? DEFAULT_CHUNK_SIZE : chunkSize );
    	//
    	if ( capacity > Integer.MAX_VALUE ) {
    		this.pageSize = DEFAULT_PAGE_SIZE;
    		this.pageCount = (int) ( capacity / pageSize  + ( capacity % pageSize == 0 ? 0 : 1) );
    		
    	} else {
    		this.pageSize = (int) capacity;
    		this.pageCount = 1;
    	}
    	//
    	this.byteOrder = ByteOrder.BIG_ENDIAN;
    }
    
    
    public Pages(long capacity, int chunkSize, ByteOrder byteOrder) {
    	this(capacity, chunkSize);
    	this.byteOrder = byteOrder;
    }
	//
	public void initialize() {
    	allPages = new BufferPage[pageCount];
    	for (int i = 0; i < pageCount; i++) 
          allPages[i] = new BufferPage(ByteBuffer.allocateDirect(pageSize), chunkSize);
	}
	
	public void destroy() {
		// 
	}
	
	public long getCapacity() {
		return capacity;
	}

	public long getUsageSize() {
		return usageSize.get();
	}
	
	public long getUsageCount() {
		return usageCount.get();
	}
	
	public long getSharedOptsCount() {
		return sharedOptsCount.get();
	}
	
    //
	public ByteBuffer allocate(int size) {
		//
		final int theChunkCount = size / chunkSize + (size % chunkSize == 0 ? 0 : 1);
		final int selectedPage = prevAllocatedPage.incrementAndGet() % allPages.length;
		//
		ByteBuffer byteBuf = allocateBuffer(theChunkCount, 0, selectedPage);
		if (byteBuf == null) 
			byteBuf = allocateBuffer(theChunkCount, selectedPage, allPages.length);
		//
		if ( byteBuf != null ) {
			usageSize.addAndGet( byteBuf.capacity() );
			usageCount.incrementAndGet();
		} else {
			byteBuf = ByteBuffer.allocate(size);
		}
		//
		byteBuf.order( byteOrder );
		return byteBuf;
	}
	
	private ByteBuffer allocateBuffer(int theChunkCount, int startPage, int endPage) {
		for (int i = startPage; i < endPage; i++) {
			ByteBuffer buffer = allPages[i].allocateByteBuffer(theChunkCount);
			if (buffer != null) {
				prevAllocatedPage.getAndSet(i);
				return buffer;
			}
		}
		return null;
	}
	
	@SuppressWarnings("restriction")
	public boolean recycle(ByteBuffer theBuf) {
		// 堆内buffer直接就清空就好
		if (theBuf != null && (!(theBuf instanceof sun.nio.ch.DirectBuffer))) {
			theBuf.clear();
			return false;
		}
		
		//
		boolean recycled = false;
		sun.nio.ch.DirectBuffer thisNavBuf = (sun.nio.ch.DirectBuffer) theBuf;
		int chunkCount = theBuf.capacity() / chunkSize;											// chunk的个数
		sun.nio.ch.DirectBuffer parentBuf = (sun.nio.ch.DirectBuffer) thisNavBuf.attachment();  // page的DirectBuffer
		//
		// 已经使用的地址减去父类最开始的地址，即为所有已经使用的地址，除以chunkSize得到chunk当前开始的地址,得到整块内存开始的地址
		int startChunk = (int) ((thisNavBuf.address() - parentBuf.address()) / chunkSize);		
		for (BufferPage pageBuffer : allPages) {
			if ((recycled = pageBuffer.recycleByteBuffer((ByteBuffer) parentBuf, theBuf, startChunk, chunkCount))) 
				break;
		}
		//
		if ( recycled ) {
			usageSize.addAndGet( -theBuf.capacity() );
			usageCount.decrementAndGet();
			sharedOptsCount.incrementAndGet();
		} else {
			LOGGER.warn("not recycled buffer " + theBuf);
		}
		return recycled;
	}
}