package com.feeyo.buffer.test;

import java.nio.ByteBuffer;

import com.feeyo.buffer.BufferPool;
import com.feeyo.buffer.bucket.BucketBufferPool;

public class LeakTrackingTest {
	
	public static void main(String[] args) {
		//
		BufferPool bufferPool = new BucketBufferPool(1024 * 2, 1024 * 100,new int[] { 512, 1024 });
		
		for(int i=0; i< 10; i++) {
			ByteBuffer buf = bufferPool.allocate(888);
			System.out.println( buf );
			//
			bufferPool.recycle(buf);
			bufferPool.recycle(buf);
			bufferPool.recycle(buf);
			System.out.println("buf = " + i);
		}
	        
	}

}
