package com.feeyo.buffer;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeakDetector<T> implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LeakDetector.class);

	private final ReferenceQueue<T> queue = new ReferenceQueue<>();
	private final ConcurrentHashMap<String, LeakInfo> resources = new ConcurrentHashMap<>();
	private Thread thread;
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	//
	public boolean acquired(T resource) {
		String id = id(resource);
		LeakInfo info = resources.putIfAbsent(id, new LeakInfo(resource, id));
		if (info != null) {
			return false; // 检测到泄漏，先前获取存在（未释放）或id冲突
		}
		return true;
	}

	public boolean released(T resource) {
		String id = id(resource);
		LeakInfo info = resources.remove(id);
		if (info != null) {
			return true;
		}
		return false; // 检测到泄漏（未获取即释放）
	}
	
	//
	protected String id(T resource) {
		return String.valueOf(System.identityHashCode(resource));
	}

	public void start() throws Exception {
		if ( !isRunning.compareAndSet(false, true) )
			return;
		thread = new Thread(this, getClass().getSimpleName());
		thread.setDaemon(true);
		thread.start();
	}

	protected void stop() throws Exception {
		if ( !isRunning.compareAndSet(true, false))
			return;
		thread.interrupt();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			while (isRunning.get()) {
				LeakInfo leakInfo = (LeakInfo) queue.remove();
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Resource GC'ed: {}", leakInfo);
				//
				if (resources.remove(leakInfo.id) != null)
					leaked(leakInfo);
			}
		} catch (InterruptedException x) {
			// Exit
		}
	}

	protected void leaked(LeakInfo leakInfo) {
		LOGGER.warn("Resource leaked: " + leakInfo.description, leakInfo.stackFrames);
	}

	//
	public class LeakInfo extends PhantomReference<T> {
		private final String id;
		private final String description;
		private final Throwable stackFrames;

		private LeakInfo(T referent, String id) {
			super(referent, queue);
			this.id = id;
			this.description = referent.toString();
			this.stackFrames = new Throwable();
		}

		public String getResourceDescription() {
			return description;
		}

		public Throwable getStackFrames() {
			return stackFrames;
		}

		@Override
		public String toString() {
			return description;
		}
	}
}
