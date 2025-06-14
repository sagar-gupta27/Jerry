package servlet.util.net;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import logging.Log;
import logging.LogFactory;
import servlet.util.StringManager;

//Generic class to Wrap sockets of all type  
public abstract class SocketWrapper<T> {

    private static final Log log = LogFactory.getLog(SocketWrapper.class);

    protected static final StringManager sm = StringManager.getManager(SocketWrapper.class);
    protected T socket; // socket object
    private static final AtomicLong connIdGenerator = new AtomicLong(0);
    private final ReentrantLock lock = new ReentrantLock(); // mutual exclusion , thread already holding the lock and
                                                            // re-enter , doesn't blocks itself
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    private volatile long readTimeout;
    private volatile long writeTimeout;
    private volatile int keepAliveLeft;
    private final String connectionId;
    private final AbstractEndPoint<T, ?> endpoint;
    /*
     * Following cached for speed / reduced GC
     */
    protected String localAddr = null;
    protected String localName = null;
    protected int localPort = -1;
    protected String remoteAddr = null;
    protected String remoteHost = null;
    protected int remotePort = -1;
    // protected volatile ServletConnection servletConnection = null;
    protected volatile SocketBufferWrapper socketBufferWrapper; // wrapper object to manage the bytebuffer
    private final AtomicReference<Object> currentProcessor = new AtomicReference<>(); //

    // Asynchorous operations
    protected final Semaphore readPending;
    protected final Semaphore writePending;
    protected int bufferedWriteSize = 64 * 1024; // 64k default write buffer
     protected final WriteBuffer nonBlockingWriteBuffer = new WriteBuffer(bufferedWriteSize);

    public SocketWrapper(T socket, AbstractEndPoint<T, ?> endpoint) {
        this.socket = socket;
        this.endpoint = endpoint;
        if (endpoint.getUseAsyncIO() || needSemaphores()) {
            readPending = new Semaphore(1);
            writePending = new Semaphore(1);
        }else{
            readPending = null;
            writePending = null;
        }
        connectionId = Long.toHexString(connIdGenerator.getAndIncrement());
    }

    public boolean needSemaphores() {
        return false;
    }

    public T getSocket() {
        return socket;
    }

    protected void reset(T closedSocket) {
        socket = closedSocket;
    }

    protected AbstractEndPoint<T, ?> getEndPoint() {
        return endpoint;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Object getCurrentProcessor() {
        return currentProcessor.get();
    }

    public void setCurrentProcessor(Object currentProcessor) {
        this.currentProcessor.set(currentProcessor);
    }

    public Object takeCurrentProcessor() {
        return currentProcessor.getAndSet(null);
    }

    public void execute(Runnable task) throws Exception{
        ExecutorService executor = endpoint.getExecutor();

        if (!endpoint.isRunning() || executor == null) {
            throw new Exception();
        }
        executor.submit(task);
    }

    public void setReadTimeout(long readTimeout) {
        if (readTimeout > 0) {
            this.readTimeout = readTimeout;
        } else {
            this.readTimeout = -1;
        }
    }

    public long getReadTimeout() {
        return this.readTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        if (writeTimeout > 0) {
            this.writeTimeout = writeTimeout;
        } else {
            this.writeTimeout = -1;
        }
    }

    public long getWriteTimeout() {
        return this.writeTimeout;
    }

    public void setKeepAliveLeft(int keepAliveLeft) {
        this.keepAliveLeft = keepAliveLeft;
    }

    public int decrementKeepAliveLeft() {
        return (--keepAliveLeft);
    }

    public String getRemoteHost() {
        if (remoteHost == null) {
            populateRemoteHost();
        }
        return remoteHost;
    }

    protected abstract void populateRemoteHost();

    public String getRemoteAddr() {
        if (remoteAddr == null) {
            populateRemoteAddr();
        }
        return remoteAddr;
    }

    protected abstract void populateRemoteAddr();

    public int getRemotePort() {
        if (remotePort == -1) {
            populateRemotePort();
        }
        return remotePort;
    }

    protected abstract void populateRemotePort();

    public String getLocalName() {
        if (localName == null) {
            populateLocalName();
        }
        return localName;
    }

    protected abstract void populateLocalName();

    public String getLocalAddr() {
        if (localAddr == null) {
            populateLocalAddr();
        }
        return localAddr;
    }

    protected abstract void populateLocalAddr();

    public int getLocalPort() {
        if (localPort == -1) {
            populateLocalPort();
        }
        return localPort;
    }

    protected abstract void populateLocalPort();

    public SocketBufferWrapper getSocketBufferWrapper() {
        return this.socketBufferWrapper;
    }

    public boolean hasDataToRead() {
        return true;
    }

    public boolean hasDataToWrite() {
        return !socketBufferWrapper.isWriteBufferEmpty() || nonBlockingWriteBuffer.isEmpty();
    }

    public boolean isReadyForWrite() {
        boolean result = canWrite();

        if (!result) {
            registerWriteInterest();
        }

        return result;
    }

    public boolean canWrite() {
        if (socketBufferWrapper == null) {
            throw new IllegalStateException(sm.getString("socket.closed"));
        }
        return socketBufferWrapper.isWriteBufferWritable() && nonBlockingWriteBuffer.isEmpty();
    }

    // Reading the buffer data
    protected int copyToReadBuffer(byte[] buf , int off , int len){
         socketBufferWrapper.configureReadBufferForRead();
         ByteBuffer readBuffer = socketBufferWrapper.getReadBuffer();

         int remaining = readBuffer.remaining();

         if(remaining > 0){
            remaining = Math.min(remaining,len);
            readBuffer.get(buf,off,len);


            if(log.isTraceEnabled()){
               log.trace("Socket: [" + this + "], Read from buffer: [" + remaining + "]");
            }
         }

         return remaining;

     }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                getEndPoint().getHandler().release(this);
            } catch (Exception e) {

            } finally {
                getEndPoint().countDownConnection();
                doClose();
            }

        }
    }

    // Perform the actual close
    protected abstract void doClose();

    public boolean isClosed() {
        return closed.get();
    }

    public abstract void registerReadInterest();

    public abstract void registerWriteInterest();


  /*   public ServletConnection getServletConnection(String protocol, String protocolConnectionId) {
        if (servletConnection == null) {
            servletConnection =
                    new ServletConnectionImpl(connectionId, protocol, protocolConnectionId, endpoint.isSSLEnabled());
        }
        return servletConnection;
    } */

}
