package com.feeyo.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓冲池
 */
public abstract class BufferPool {
	//
	protected long minBufferSize;
	protected long maxBufferSize;
	protected AtomicLong usedBufferSize = new AtomicLong(0); 
	
	protected int minChunkSize;
	protected int[] chunkSizes;
	protected int maxChunkSize;
	protected int decomposeBufferSize = 64 * 1024;	// 用于大buffer 分解
	
	public BufferPool(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		this.minBufferSize = minBufferSize;
		this.maxBufferSize = maxBufferSize;
		//
		if ( chunkSizes == null || chunkSizes.length ==0 )
			throw new IllegalArgumentException("chunkSizes cannot be empty!");
		//
		Arrays.sort( chunkSizes );
		//
		this.chunkSizes = chunkSizes;
		this.minChunkSize = chunkSizes[0];
		this.maxChunkSize = chunkSizes[ chunkSizes.length - 1 ];
	}
	
	public long getMinBufferSize() {
		return minBufferSize;
	}

	public long getMaxBufferSize() {
		return maxBufferSize;
	}
	
	public int getDecomposeBufferSize() {
		return decomposeBufferSize;
	}

	public AtomicLong getUsedBufferSize() {
		return usedBufferSize;
	}
	
	public int getMinChunkSize() {
		return minChunkSize;
	}
	
	public int[] getChunkSizes() {
		return chunkSizes;
	}

	public int getMaxChunkSize() {
		return maxChunkSize;
	}

	//
    public abstract ByteBuffer allocate(int size);
    public abstract void recycle(ByteBuffer theBuf);
    
    public abstract long capacity();
    public abstract long size();
    public abstract long getSharedOptsCount();
    //
    public abstract Map<String, Object> getStatistics();
    
}
