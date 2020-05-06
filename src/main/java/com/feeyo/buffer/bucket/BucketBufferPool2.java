package com.feeyo.buffer.bucket;

public class BucketBufferPool2 extends BucketBufferPool {

	public BucketBufferPool2(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		super(minBufferSize, maxBufferSize, chunkSizes);
	}
	
	@Override
	protected void preheatBucket(int i, int size, int count) {
		AbstractBucket bucket = new DefaultBucket(this, size, count);
		this._buckets.add(i, bucket);
	}

}
