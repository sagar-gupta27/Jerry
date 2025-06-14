package servlet.util.threads;

import java.io.Serial;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import logging.Log;
import logging.LogFactory;
import servlet.util.StringManager;

public class LimitLatch {

    private static final Log log = LogFactory.getLog(LimitLatch.class);
     private static final StringManager sm = StringManager.getManager(LimitLatch.class);
    public class Sync extends AbstractQueuedSynchronizer {
       @Serial
        private static final long serialVersionUID = 1L;
        @Override
        protected int tryAcquireShared(int arg) {
            long newCount = count.incrementAndGet();

            if (!released && newCount > limit) {
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString("limitLatch.exceeded", Long.valueOf(limit)));
                }
                // Limit exceeded
                count.decrementAndGet();
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg){
            count.decrementAndGet();
            return true;
        }
    }

    private final Sync sync;
    private final AtomicLong count;
    private volatile long limit;
    private volatile boolean released = false;

    public LimitLatch(long limit){
        this.limit = limit;
        this.sync = new Sync();
        this.count = new AtomicLong(0);
    }

    public long getCount(){
        return count.get();
    }

    public long getLimit(){
        return limit;
    }

    public void setLimit(long limit){
        this.limit = limit;
    }
    public void countUpOrAwait() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    public long countDown(){
        sync.releaseShared(0); // this internally calls tryReleaseShared method
        long result = getCount();

        /* 
         if (log.isTraceEnabled()) {
            log.trace("Counting down["+Thread.currentThread().getName()+"] latch="+result);
        } */
        return result;
    }

    public boolean releaseAll(){
        released =true;
        return sync.releaseShared(0);
    }

    public void reset(){
        this.count.set(0);
        released = false;
    }

    public boolean hasQueuedThreads(){
        return sync.hasQueuedThreads();
    }

    public Collection<Thread> getQueuedThreads(){
        return sync.getQueuedThreads();
    }
}
