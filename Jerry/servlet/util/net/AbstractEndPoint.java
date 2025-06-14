package servlet.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import logging.Log;
import servlet.interfaces.Executor;
import servlet.util.StringManager;
import servlet.util.net.Acceptor.AcceptorState;
import servlet.util.threads.LimitLatch;

/* User sends request --> Server socket (AbstractEndpoint) accepts it
                     --> Creates SocketWrapper for the new connection
                         --> SocketWrapper used by processor to handle the request
                         
*/

//Here S will the type of socket wrapper that is associated with the endpoint ; Nio socketWrapper , AprSocketWrapper etc; 
// T is the type of raw socket associated with endpoint , like socket , socketChannel etc; 
public abstract class AbstractEndPoint<S, T> {

    // Protected Members
     protected static final StringManager sm = StringManager.getManager(AbstractEndPoint.class);
    protected final SocketProperties socketProperties = new SocketProperties();
    protected Map<T, SocketWrapper<S>> connections = new ConcurrentHashMap<>();
    protected volatile boolean running = false;
    protected volatile boolean paused = false;

    protected Acceptor<T> acceptor;

    protected abstract Log getLog();

    protected volatile boolean internalExecutor = true;

    // Private Members
    private volatile LimitLatch connectionLimitLatch = null;
    private volatile BindState bindState = BindState.UNBOUND;
    private int maxConnections = 8 * 1024;
    private Handler<S> handler = null;
    private boolean useAsyncIO = true;
    private Executor executor = null;
    private ScheduledExecutorService utilityExecutor = null;

    public SocketProperties getSocketProperties() {
        return socketProperties;
    }

    public interface Handler<S> {

        enum SocketState {

            OPEN,
            CLOSED,
            LONG,
            ASYNC_END,
            SENDFILE,
            UPGRADING,
            UPGRADED,
            ASYNC_IO,
            SUSPENDED
        }

        SocketState process(SocketWrapper<S> socket, SocketEvent status);

        Object getGlobal();

        void release(SocketWrapper<S> socket);

        void pause();

        void recycle();
    }

    protected enum BindState {
        UNBOUND(false, false),
        BOUND_ON_INIT(true, true),
        BOUND_ON_START(true, true),
        SOCKET_CLOSED_ON_STOP(false, true);

        private final boolean bound;
        private final boolean wasBound;

        BindState(boolean bound, boolean wasBound) {
            this.bound = bound;
            this.wasBound = wasBound;
        }

        public boolean isBound() {
            return bound;
        }

        public boolean wasBound() {
            return wasBound;
        }
    }

    // To normalize negative timeouts
    public long toTimeout(long timeout) {
        return (timeout > 0) ? timeout : Long.MAX_VALUE;
    }

    /**
     * counter for nr of connections handled by an endpoint
     */

    protected void countUpOrAwaitConnection() throws InterruptedException {
        if (maxConnections == -1) {
            return;
        }

        LimitLatch latch = connectionLimitLatch;
        if (latch != null) {
            latch.countUpOrAwait();
        }
    }

    protected long countDownConnection() {
        if (maxConnections == -1) {
            return -1;
        }

        LimitLatch latch = connectionLimitLatch;
        if (latch != null) {
            long result = latch.countDown();
            if (result < 0) {
                getLog().warn("Incorrect count");
            }
            return result;
        } else {
            return -1;
        }

    }

    public long getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxCon) {
        this.maxConnections = maxCon;
        LimitLatch latch = this.connectionLimitLatch;
        if (latch != null) {
            // Update the latch that enforces this
            if (maxCon == -1) {
                releaseConnectionLatch();
            } else {
                latch.setLimit(maxCon);
            }
        } else if (maxCon > 0) {
            initializeConnectionLatch();
        }
    }

    protected LimitLatch initializeConnectionLatch() {
        if (maxConnections == -1)
            return null;

        if (connectionLimitLatch == null) {
            connectionLimitLatch = new LimitLatch(getMaxConnections());
        }

        return connectionLimitLatch;
    }

    protected abstract InetSocketAddress getLocalAddress() throws IOException;

    private static InetSocketAddress getUnlockAddress(InetSocketAddress localAddress) throws SocketException {
        if (localAddress.getAddress().isAnyLocalAddress()) {
            // Need a local address of the same type (IPv4 or IPV6) as the
            // configured bind address since the connector may be configured
            // to not map between types.
            InetAddress loopbackUnlockAddress = null;
            InetAddress linkLocalUnlockAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isPointToPoint() && networkInterface.isUp()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (localAddress.getAddress().getClass().isAssignableFrom(inetAddress.getClass())) {
                            if (inetAddress.isLoopbackAddress()) {
                                if (loopbackUnlockAddress == null) {
                                    loopbackUnlockAddress = inetAddress;
                                }
                            } else if (inetAddress.isLinkLocalAddress()) {
                                if (linkLocalUnlockAddress == null) {
                                    linkLocalUnlockAddress = inetAddress;
                                }
                            } else {
                                // Use a non-link local, non-loop back address by default
                                return new InetSocketAddress(inetAddress, localAddress.getPort());
                            }
                        }
                    }
                }
            }
            // Prefer loop back over link local since on some platforms (e.g.
            // OSX) some link local addresses are not included when listening on
            // all local addresses.
            if (loopbackUnlockAddress != null) {
                return new InetSocketAddress(loopbackUnlockAddress, localAddress.getPort());
            }
            if (linkLocalUnlockAddress != null) {
                return new InetSocketAddress(linkLocalUnlockAddress, localAddress.getPort());
            }
            // Fallback
            return new InetSocketAddress("localhost", localAddress.getPort());
        } else {
            return localAddress;
        }
    }

    protected void unlockAccept() {
        if (acceptor == null || acceptor.getState() != AcceptorState.RUNNING)
            return;

        InetSocketAddress unlockAddress;
        InetSocketAddress localAddress = null;

        try {
            localAddress = getLocalAddress();

        } catch (Exception e) {
            // TODO: handle exception
        }

        if (localAddress == null) {
            return;
        }

        try {
            unlockAddress = getUnlockAddress(localAddress);

            try (java.net.Socket s = new java.net.Socket()) {
                s.setSoTimeout(getSocketProperties().getUnlockTimeout());
                s.connect(unlockAddress, getSocketProperties().getUnlockTimeout());
            }

            long startTime = System.nanoTime();
            while (startTime + 1_000_000_000 > System.nanoTime() && acceptor.getState() == AcceptorState.RUNNING) // wait
                                                                                                                  // for
                                                                                                                  // 1s
                                                                                                                  // or
                                                                                                                  // acceptor
                                                                                                                  // state
                                                                                                                  // change
            {
                if (startTime + 1_000_000 < System.nanoTime()) { // wait until 1 milli sec has passed , afterthat give
                                                                 // sleep
                    Thread.sleep(1);
                }
            }

        } catch (Throwable t) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("unlock Failed");
            }
        }
    }

    private void releaseConnectionLatch() {
        LimitLatch latch = connectionLimitLatch;
        if (latch != null) {
            latch.releaseAll();
        }

        connectionLimitLatch = null;
    }

    protected BindState getBindState() {
        return bindState;
    }

    public final void closeServerSocketGraceful() {
        if (bindState == BindState.BOUND_ON_START) {

            acceptor.stopMillis(-1);

            releaseConnectionLatch(); // release the latch to unblock the acceptor thread , which may be waiting for
                                      // latch permit
            unlockAccept(); // unlock the acceptor thread by making bogus client connection

            getHandler().pause();

            bindState = BindState.SOCKET_CLOSED_ON_STOP;

            try {
                doCloseServerSocket();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    protected boolean isPaused() {
        return paused;
    }

    protected boolean isRunning() {
        return running;
    }

    public final long awaitConnetionsClose(long waitMillis) {
        while (waitMillis > 0 && !connections.isEmpty()) {
            try {
                Thread.sleep(50);
                waitMillis -= 50;
            } catch (Exception e) {

            }
        }

        return waitMillis;
    }

    protected abstract void doCloseServerSocket();

    protected abstract T serverSocketAccept() throws Exception;

    protected abstract boolean setSocketOptions(T socket);

    protected abstract void destroySocket(T socket);

    protected void closeSocket(T socket) {
        SocketWrapper<S> socketWrapper = connections.get(socket);
        if (socketWrapper != null) {
            socketWrapper.close();
        }
    }

    public void setHandler(Handler<S> handler) {
        this.handler = handler;
    }

    public Handler<S> getHandler() {
        return handler;
    }

    public void setUseAsyncIO(boolean useAsyncIO) {
        this.useAsyncIO = useAsyncIO;
    }

    public boolean getUseAsyncIO() {
        return useAsyncIO;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
        this.internalExecutor = (executor == null);
    }

    public Executor getExecutor() {
        return executor;
    }


     public long getConnectionCount() {
        LimitLatch latch = connectionLimitLatch;
        if (latch != null) {
            return latch.getCount();
        }
        return -1;
    }

     

    public void setUtilityExecutor(ScheduledExecutorService utilityExecutor) {
        this.utilityExecutor = utilityExecutor;
    }

    public ScheduledExecutorService getUtilityExecutor() {
        if (utilityExecutor == null) {
            getLog().warn(sm.getString("endpoint.warn.noUtilityExecutor"));
            utilityExecutor = new ScheduledThreadPoolExecutor(1);
        }
        return utilityExecutor;
    }

     /**
     * Priority of the acceptor threads.
     */
    protected int acceptorThreadPriority = Thread.NORM_PRIORITY;

    public void setAcceptorThreadPriority(int acceptorThreadPriority) {
        this.acceptorThreadPriority = acceptorThreadPriority;
    }

    public int getAcceptorThreadPriority() {
        return acceptorThreadPriority;
    }


     private long executorTerminationTimeoutMillis = 5000;

    public long getExecutorTerminationTimeoutMillis() {
        return executorTerminationTimeoutMillis;
    }

    public void setExecutorTerminationTimeoutMillis(long executorTerminationTimeoutMillis) {
        this.executorTerminationTimeoutMillis = executorTerminationTimeoutMillis;
    }


    private boolean useSendfile = true;

    public boolean getUseSendfile() {
        return useSendfile;
    }

    public void setUseSendfile(boolean useSendfile) {
        this.useSendfile = useSendfile;
    }
}
