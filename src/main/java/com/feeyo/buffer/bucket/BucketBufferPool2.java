package com.feeyo.buffer.bucket;

public class BucketBufferPool2 extends BucketBufferPool {

	public BucketBufferPool2(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		super(minBufferSize, maxBufferSize, chunkSizes);
	}
	
	@Override
	protected void preheatBucket(int bucketIdx, int chunkSize, int count) {
		AbstractBucket bucket = new DefaultBucket(this, chunkSize, count);
		this._buckets.add(bucketIdx, bucket);
	}

}
