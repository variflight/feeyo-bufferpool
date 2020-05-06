package com.feeyo.buffer.bucket;

public class BucketBufferPool2 extends BucketBufferPool {

	public BucketBufferPool2(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		super(minBufferSize, maxBufferSize, chunkSizes);
	}
	
	@Override
	protected void initBucket(int index, int chunkSize, int chunkCount) {
		AbstractBucket bucket = new ArrayBucket(this, chunkSize, chunkCount);
		this._buckets.add(index, bucket);
	}

}
