package com.feeyo.buffer.bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feeyo.buffer.BufferPool;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractBucket implements Comparable<AbstractBucket> {

    private static Logger LOGGER = LoggerFactory.getLogger(AbstractBucket.class);
    //
    private final AtomicInteger count;
    private final AtomicInteger usedCount;
    private final int chunkSize;
    private final BufferPool bufferPool;
    private final Object _lock = new Object();
    private long _shared = 0;


    public AbstractBucket(BufferPool pool, int chunkSize) {
        this(pool, chunkSize, 0);
    }

    public AbstractBucket(BufferPool pool, int chunkSize, int count) {
        this.bufferPool = pool;
        this.chunkSize = chunkSize;

        this.count = new AtomicInteger(count);
        this.usedCount = new AtomicInteger(0);
    }

    protected abstract boolean queueOffer(ByteBuffer buffer);

    protected abstract ByteBuffer queuePoll();

    protected abstract void containerClear();

    public abstract int getQueueSize();

    
    public ByteBuffer allocate() {

        ByteBuffer bb = queuePoll();
        if (bb != null) {
        	this.usedCount.incrementAndGet();
        	//
        	// Clear sets limit == capacity. Position == 0.
            bb.clear();
            return bb;
        }
        //
        // 桶内内存块不足，创建新的块
        synchronized (_lock) {
        	
            // 容量阀值
            long poolUsed = bufferPool.getUsedBufferSize().get();
            if ((poolUsed + chunkSize) < bufferPool.getMaxBufferSize()) {
            	//
            	ByteBuffer buf = ByteBuffer.allocateDirect(chunkSize);
                //
            	this.count.incrementAndGet();
                this.usedCount.incrementAndGet();
                bufferPool.getUsedBufferSize().addAndGet(chunkSize);
                return buf;
            }
        }
        
        return null;
    }

    public void recycle(ByteBuffer buf) {

        if (buf.capacity() != this.chunkSize) {
            LOGGER.warn("Trying to put a buffer, not created by this bucket! Will be just ignored");
            return;
        }
        
        usedCount.decrementAndGet(); 
        
        buf.clear();
        queueOffer(buf);
        _shared++;
    }

  
    @SuppressWarnings("restriction")
    public synchronized void clear() {
        ByteBuffer buffer;
        while ((buffer = queuePoll()) != null) {
            if (buffer.isDirect()) {
                ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
            }
        }
        containerClear();
    }

    public int getCount() {
        return this.count.get();
    }

    public int getUsedCount() {
        return this.usedCount.get();
    }

    public long getShared() {
        return _shared;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public String toString() {
        return String.format("Bucket@%x{%d/%d}", hashCode(), count.get(), chunkSize);
    }

    @Override
    public int hashCode() {
        // return super.hashCode();
        return this.chunkSize;
    }
}