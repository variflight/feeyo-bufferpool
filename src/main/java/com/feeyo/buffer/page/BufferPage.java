package com.feeyo.buffer.page;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * 用来保存一个一个ByteBuffer为底层存储的内存页
 * 
 * @author mycat
 * @author zhuam
 */
public class BufferPage {

	private ByteBuffer byteBuffer;
	//
	private final int chunkSize;					// chunk的大小, 一般为4k
	private final int chunkCount;					// chunk的个数
	private final BitSet chunkAllocateTrack;		// 某个chunk是否被分配
	
	// 
	// 此处优化可大大提升分配的性能
	private int lastChunkIndex = 0;
	private boolean isRejectAllocate = false;

	//
	public BufferPage(ByteBuffer byteBuffer, int chunkSize) {
		this.byteBuffer = byteBuffer;
		this.chunkSize = chunkSize;
		this.chunkCount = byteBuffer.capacity() / chunkSize;
		this.chunkAllocateTrack = new BitSet(chunkCount);
	}

	/*
	 * ByteBuffer 分配
	 *  
	 * @param theChunkCount 需要的 chunk 数量
	 * @return
	 */
	public synchronized ByteBuffer allocateByteBuffer(int theChunkCount) {
		//
		if ( isRejectAllocate )
			return null;
		//
		if ( lastChunkIndex == chunkCount - 1)
			lastChunkIndex = 0;
		//
		if ( lastChunkIndex > 0 ) {
			ByteBuffer newBuf = allocateByteBuffer(lastChunkIndex, theChunkCount);
			if ( newBuf != null )
				return newBuf;
			//
			lastChunkIndex = 0;
		} 
		//
		return allocateByteBuffer(lastChunkIndex, theChunkCount);
	}
	
	private ByteBuffer allocateByteBuffer(int index, int theChunkCount) {

		int startChunk = -1; 		// 从startChunk开始
		int continueCount = 0;		// 连续的chunk个数
		//
		int usedCount = 0;			// 已使用的chunk数
		//
		// 寻找连续的N个chunk
		for (int i = index; i < chunkCount; i++) {
			// 找到一个可用的
			if ( !chunkAllocateTrack.get(i) ) {
				// 如果是第一个 则设置startChunk
				if (startChunk == -1) {
					// 从头开始找
					startChunk = i;
					continueCount = 1;
					if (theChunkCount == 1) 
						break;
				} else {
					// 连续chunk个数加一 ,是否找到连续的chunk个数,是则返回.
					if (++continueCount == theChunkCount)
						break;
				}
			} else {
				//不连续
				startChunk = -1;
				continueCount = 0;
				//
				usedCount++;
			}
		}
		
		//
		//找到了
		if (continueCount == theChunkCount) {
			int offStart = startChunk * chunkSize;
			int offEnd = offStart + theChunkCount * chunkSize;
			byteBuffer.limit(offEnd);
			byteBuffer.position(offStart);
			//
			ByteBuffer newBuf = byteBuffer.slice();
			markChunksUsed(startChunk, theChunkCount);	// 设置chunk为已用
			//
			lastChunkIndex = startChunk + theChunkCount;
			//
			return newBuf;
		} else {
			//
			if ( usedCount == chunkCount )
				isRejectAllocate = true;
			//
			return null;
		}
	}
	
	/*
	 * ByteBuffer 回收
	 * 
	 * @param parent 		当前要释放的buf的parent 
	 * @param recycleBuf	当前要释放的recycleBuf
	 * @param startChunk
	 * @param chunkCount
	 * @return
	 */
	public synchronized boolean recycleByteBuffer(ByteBuffer parent, ByteBuffer recycleBuf, int startChunk, int chunkCount) {
		if (parent == this.byteBuffer) {
			markChunksUnused(startChunk, chunkCount);	// 清空已用状态
			isRejectAllocate = false;
			return true;
		}
		return false;
	}

	///
	// 设置已用
	private void markChunksUsed(int startChunk, int theChunkCount) {
		for (int i = 0; i < theChunkCount; i++) {
			chunkAllocateTrack.set(startChunk + i);
		}
	}

	// 清空不可用
	private void markChunksUnused(int startChunk, int theChunkCount) {
		for (int i = 0; i < theChunkCount; i++) {
			chunkAllocateTrack.clear(startChunk + i);
		}
	}
}