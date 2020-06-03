package com.feeyo.buffer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

public class HeapBufferPool extends BufferPool {

	public HeapBufferPool(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		super(minBufferSize, maxBufferSize, chunkSizes);
	}

	@Override
	public ByteBuffer allocate(int size) {
		return ByteBuffer.allocate(size);
	}

	@Override
	public void recycle(ByteBuffer theBuf) {
		//
	}

	@Override
	public long capacity() {
		return 0;
	}

	@Override
	public long size() {
		return 0;
	}

	@Override
	public long getSharedOptsCount() {
		return 0;
	}

	@Override
	public Map<String, Object> getStatistics() {
		return Collections.emptyMap();
	}

}
