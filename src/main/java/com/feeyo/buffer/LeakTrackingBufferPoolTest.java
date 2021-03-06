package com.feeyo.buffer;

import java.nio.ByteBuffer;

import com.feeyo.buffer.bucket.BucketBufferPool;

public class LeakTrackingBufferPoolTest {
	
	public static void main(String[] args) {
		//
		BufferPool bufferPool = new BucketBufferPool(1024 * 2, 1024 * 100,new int[] { 512, 1024 });
		LeakTrackingBufferPool x = new LeakTrackingBufferPool(bufferPool);
		
		for(int i=0; i< 10; i++) {
			ByteBuffer buf = x.allocate(888);
			System.out.println( buf );
			//
			x.recycle(buf);
			x.recycle(buf);
			x.recycle(buf);
			System.out.println("buf = " + i);
		}
		//
		System.out.println("BufferPool - leaked acquires:" + x.getLeakedAcquires());
	    System.out.println("BufferPool - leaked releases:"+ x.getLeakedReleases());
	    System.out.println("BufferPool - unreleased: " + x.getLeakedResources());
	        
	}

}
