package com.feeyo.buffer.bucket;

public class BucketBufferPool2 extends BucketBufferPool {

	public BucketBufferPool2(long minBufferSize, long maxBufferSize, int[] chunkSizes) {
		super(minBufferSize, maxBufferSize, chunkSizes);
	}
	
	@Override
	protected void addBucket(int bucketIdx, int chunkSize, int count) {
		this._buckets.add(bucketIdx, new DefaultBucket(this, chunkSize, count));
	}

}
