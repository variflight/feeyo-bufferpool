package com.feeyo.buffer.bucket;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.feeyo.buffer.BufferPool;

public class DefaultBucket extends AbstractBucket {
	
	private final ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<ByteBuffer>();

	public DefaultBucket(BufferPool pool, int chunkSize, int count) {
		super(pool, chunkSize, count);
		// 初始化
		for(int j = 0; j < count; j++ ) {
			queueOffer( ByteBuffer.allocateDirect(chunkSize) );
			pool.getUsedBufferSize().addAndGet( chunkSize );
		}
	}

	@Override
	public int compareTo(AbstractBucket o) {
		if (this.getChunkSize() > o.getChunkSize()) {
			return 1;
		} else if (this.getChunkSize() < o.getChunkSize()) {
			return -1;
		}
		return 0;
	}

	@Override
	protected boolean queueOffer(ByteBuffer buffer) {
		return this.queue.offer( buffer );
	}

	@Override
	protected ByteBuffer queuePoll() {
		return this.queue.poll();
	}

	@Override
	protected void containerClear() {
		queue.clear();
	}

	@Override
	public int getQueueSize() {
		return this.queue.size();
	}
}