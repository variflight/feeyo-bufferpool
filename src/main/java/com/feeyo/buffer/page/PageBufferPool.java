package com.feeyo.buffer.page;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import com.feeyo.buffer.BufferPool;

public class PageBufferPool extends BufferPool {
	//
	private final Pages pages;

	public PageBufferPool(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		this(minBufferSize, maxBufferSize, chunkSizes, ByteOrder.BIG_ENDIAN);
	}
	
	public PageBufferPool(long minBufferSize, long maxBufferSize, int[] chunkSizes, ByteOrder byteOrder) {
		super(minBufferSize, maxBufferSize, chunkSizes, byteOrder);
		//
		this.pages = new Pages(this.maxBufferSize, this.minChunkSize, byteOrder);
    	this.pages.initialize();
	}

	@Override
	public ByteBuffer allocate(int size) {
		return pages.allocate(size);
	}

	@Override
	public void recycle(ByteBuffer theBuf) {
		pages.recycle(theBuf);
	}

	@Override
	public long capacity() {
		return this.pages.getCapacity();
	}

	@Override
	public long size() {
		return this.pages.getUsageSize();
	}

	@Override
	public long getSharedOptsCount() {
		return this.pages.getSharedOptsCount();
	}

	@Override
	public Map<String, Object> getStatistics() {
		//
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("buffer.factory.capacity", pages.getCapacity());
		map.put("buffer.factory.usedSize", pages.getUsageSize());
		map.put("buffer.factory.usedCnt", pages.getUsageCount());
		map.put("buffer.factory.sharedCnt", pages.getSharedOptsCount());
		return map;
	}

}
