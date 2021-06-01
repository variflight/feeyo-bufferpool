package com.feeyo.buffer.bucket.ref;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feeyo.buffer.bucket.AbstractBucket;

public class ByteBufferReferenceUtil {
	//
	private static Logger LOGGER = LoggerFactory.getLogger( ByteBufferReferenceUtil.class );
	// 
	private static ScheduledExecutorService referenceExecutor = Executors.newSingleThreadScheduledExecutor();
	public static void referenceCheck(List<AbstractBucket> buckets) {
		referenceExecutor.scheduleAtFixedRate(new ReleaseTask(buckets), 120L, 300L, TimeUnit.SECONDS); // 5分钟
	}
	
	private ByteBufferReferenceUtil() {}
	//
	private static final class ReleaseTask implements Runnable {
		private final List<AbstractBucket> buckets;
		private final AtomicBoolean checking = new AtomicBoolean( false );
		
		ReleaseTask(List<AbstractBucket> buckets) {
			this.buckets = buckets;
		}

		@Override
		public void run() {
			if (!checking.compareAndSet(false, true)) {
				return;
			}
			//
			try {
				Iterator<AbstractBucket> it = buckets.iterator();
				while( it.hasNext() ) {
					AbstractBucket bucket = it.next();
					bucket.releaseTimeoutBuffer();
				}
			} catch (Exception e) {
				LOGGER.warn("##referenceCheck err:", e);

			} finally {
				checking.set(false);
			}
		}
		
	}
}
