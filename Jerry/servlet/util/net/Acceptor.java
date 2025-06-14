package servlet.util.net;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import logging.Log;
import logging.LogFactory;
//import servlet.util.StringManager;

//The Thread responsible for accepting new connections on the endpoint
public class Acceptor<U> implements Runnable {
    private static final Log log = LogFactory.getLog(Acceptor.class);
   // private static final StringManager sm = StringManager.getManager(Acceptor.class);

    private static final int INITIAL_ERROR_DELAY = 50;
    private static final int MAX_ERROR_DELAY = 1600;

    private final AbstractEndPoint<?, U> endpoint;
    private String threadName;

    private volatile boolean stopCalled = false;
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    protected volatile AcceptorState state = AcceptorState.NEW;

    public Acceptor(AbstractEndPoint<?, U> endpoint) {
        this.endpoint = endpoint;
    }

    public final AcceptorState getState() {
        return state;
    }

    final void setThreadName(final String threadName) {
        this.threadName = threadName;
    }

    final String getThreadName() {
        return threadName;
    }

    public void stopMillis(int waitMilliseconds) {
        stopCalled = true;
        if (waitMilliseconds > 0) {
            try {
                if (!stopLatch.await(waitMilliseconds, TimeUnit.MILLISECONDS)) {
                    // log.warn(sm.getString("acceptor.stop.fail", getThreadName()));
                }
            } catch (InterruptedException e) {
                // log.warn(sm.getString("acceptor.stop.interrupted", getThreadName()), e);
            }
        }
    }

    @Override
    public void run() {
        try {

            int errorDelay = 0;
            long pauseStart = 0;
            while (!stopCalled) {

                while (endpoint.isPaused() && !stopCalled) { // if endpoint is in paused state , we tightly wait
                                                             // initially and then with sleep to save CPU resources

                    if (state != AcceptorState.PAUSED) {
                        pauseStart = System.nanoTime();
                        state = AcceptorState.PAUSED;
                    }

                    if ((System.nanoTime() - pauseStart) > 1000000) {
                        try {
                            if ((System.nanoTime() - pauseStart) > 10_000_000) {
                                Thread.sleep(10);
                            } else {
                                Thread.sleep(1);
                            }
                        } catch (Exception e) {

                        }
                    }
                }

                //
                if (stopCalled)
                    break;

                state = AcceptorState.RUNNING;


                //EndPoint is running;
                try {
                    // if we reached max connections wait
                    endpoint.countUpOrAwaitConnection();

                    if (endpoint.isPaused()) {
                        continue;
                    }

                    U socket;

                    try {
                        socket = endpoint.serverSocketAccept();
                    } catch (Exception e) {
                        // didnt get the socket
                        endpoint.countDownConnection();

                        if (endpoint.isRunning()) {
                            // Introduce delay if necessary
                            errorDelay = handleExceptionWithDelay(errorDelay);
                            // re-throw
                            throw e;
                        } else {
                            break;
                        }
                    }

                  //succesfull accept configure the socket
                  errorDelay = 0;

                  if(!stopCalled && !endpoint.isPaused()){
                     //setSocket will hand off the socket to approperiate processor if successfull
                    if(!endpoint.setSocketOptions(socket)){
                        endpoint.closeSocket(socket);
                    }
                  } else {
                     endpoint.destroySocket(socket);
                  }

                } // endpoint running 
                catch (Throwable t) {
                    String msg = "endpoint Accept failed";
                    log.error(msg,t);
                }
            } // end of !stopCalled

        } finally{
             stopLatch.countDown();
        }

        state = AcceptorState.ENDED;
    }

    protected int handleExceptionWithDelay(int currentErrorDelay) {

        if (currentErrorDelay > 0) {
            try {
                Thread.sleep(currentErrorDelay);
            } catch (Exception e) {
                // ignore
            }
        }

        if (currentErrorDelay == 0) {
            return INITIAL_ERROR_DELAY;
        } else if (currentErrorDelay < MAX_ERROR_DELAY) {
            return currentErrorDelay * 2;
        } else {
            return MAX_ERROR_DELAY;
        }
    }

    public enum AcceptorState {
        NEW,
        RUNNING,
        PAUSED,
        ENDED
    }

}
