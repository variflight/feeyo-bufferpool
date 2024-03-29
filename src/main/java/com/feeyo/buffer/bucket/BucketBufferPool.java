package com.feeyo.buffer.bucket;

import com.feeyo.buffer.BufferPool;
import com.feeyo.buffer.bucket.ref.ByteBufferReferenceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 堆外内存池
 * @author zhuam
 */
public class BucketBufferPool extends BufferPool {
	
	private static Logger LOGGER = LoggerFactory.getLogger( BucketBufferPool.class );
	//
	protected List<AbstractBucket> buckets;
	protected long sharedOptsCount;
	
	public BucketBufferPool(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		this(minBufferSize, maxBufferSize, chunkSizes, ByteOrder.BIG_ENDIAN);
	}
	//
	public BucketBufferPool(long minBufferSize, long maxBufferSize, int[] chunkSizes, ByteOrder byteOrder) {
		super(minBufferSize, maxBufferSize, chunkSizes, byteOrder);
		//
		// 初始化桶
		this.buckets = new ArrayList<AbstractBucket>();
		long bucketCapacity = minBufferSize / chunkSizes.length;
		for (int i = 0; i < chunkSizes.length; i++) {
			int chunkSize = chunkSizes[i];
			int chunkCount = (int) (bucketCapacity / chunkSize);
			this.addBucket(i, chunkSize, chunkCount);
		}
		//
		// 引用检测
		ByteBufferReferenceUtil.referenceCheck(buckets);
	}
	
	//
	// 构建桶
	protected void addBucket(int bucketIdx, int chunkSize, int count) {
		this.buckets.add(bucketIdx, new ArrayBucket(this, chunkSize, count));
	}
	//
	// 根据size寻找 桶
	private AbstractBucket bucketFor(int size) {
		if (size > maxChunkSize)
			return null;
		//
		for (int i = 0; i < buckets.size(); i++) {
			AbstractBucket bucket = buckets.get(i);
			if (bucket.getChunkSize() >= size)
				return bucket;
		}
		return null;
	}
	//
	//TODO : debug err, TMD, add temp synchronized
	@Override
	public ByteBuffer allocate(int size) {		
	    	
		ByteBuffer byteBuf = null;
		//
		// 根据容量大小size定位到对应的桶Bucket
		AbstractBucket bucket = bucketFor(size);
		if ( bucket != null) {
			byteBuf = bucket.allocate();
			byteBuf.order( byteOrder );
		}
		// 堆内
		if (byteBuf == null) {
			byteBuf =  ByteBuffer.allocate( size );
			byteBuf.order( byteOrder );
		}
		return byteBuf;
	}

	@Override
	public void recycle(ByteBuffer buf) {
		if (buf == null) 
			return;
		//
		if( !buf.isDirect() ) 
			return;
      	//
		AbstractBucket bucket = bucketFor( buf.capacity() );
		if (bucket != null) {
			bucket.recycle( buf );
			sharedOptsCount++;
		//
		} else {
			LOGGER.warn("Trying to put a buffer, not created by this pool! Will be just ignored");
		}
	}

	public synchronized AbstractBucket[] buckets() {
		AbstractBucket[] tmp = new AbstractBucket[ buckets.size() ];
		int i = 0;
		for(AbstractBucket b: buckets) {
			tmp[i] = b;
			i++;
		}
		return tmp;
	}
	
	@Override
	public long getSharedOptsCount() {
		return sharedOptsCount;
	}

	@Override
	public long capacity() {
		return this.maxBufferSize;
	}

	@Override
	public long size() {
		return this.usedBufferSize.get();
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("buffer.factory.min", this.getMinBufferSize());
		map.put("buffer.factory.used", this.getUsedBufferSize());
		map.put("buffer.factory.max", this.getMaxBufferSize());
		//
		for (AbstractBucket b: buckets) {
			StringBuffer sBuffer = new StringBuffer();
			sBuffer.append(" chunkSize=").append( b.getChunkSize() ).append(",");
			sBuffer.append(" queueSize=").append( b.getQueueSize() ).append( ", " );
			sBuffer.append(" count=").append( b.getCount() ).append( ", " );
			sBuffer.append(" useCount=").append( b.getUsedCount() ).append( ", " );
			sBuffer.append(" shared=").append( b.getShared() );		
			//
			map.put("buffer.factory.bucket." + b.getChunkSize(),  sBuffer.toString());
		}
		return map;
	}
}