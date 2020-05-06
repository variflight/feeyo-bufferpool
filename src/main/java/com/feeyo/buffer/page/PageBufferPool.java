package com.feeyo.buffer.page;

import java.nio.ByteBuffer;
import java.util.Map;

import com.feeyo.buffer.BufferPool;

public class PageBufferPool extends BufferPool {

	public PageBufferPool(long minBufferSize, long maxBufferSize, int decomposeBufferSize, int minChunkSize,
			int[] increments, int maxChunkSize) {
		super(minBufferSize, maxBufferSize, decomposeBufferSize, minChunkSize, increments, maxChunkSize);
	}

	@Override
	public ByteBuffer allocate(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recycle(ByteBuffer theBuf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long capacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSharedOptsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, Object> getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

}
