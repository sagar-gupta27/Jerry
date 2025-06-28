package startup;
//Main servlet container class


import logging.Log;
import logging.LogFactory;
import servlet.interfaces.LifecycleException;
import servlet.interfaces.LifecycleState;
import servlet.interfaces.Server;
import servlet.util.StringManager;

public class Mouselet {
    protected static final StringManager sm = StringManager.getManager(Constants.Package);
    protected static final Log log = LogFactory.getLog(Mouselet.class);
    public static final String SERVER_XML = "conf/server.xml";

    protected boolean await = false;
    protected String configFile = SERVER_XML;

    protected ClassLoader parentClassLoader = Mouselet.class.getClassLoader();

    protected Server server = null;
    protected boolean loaded = false;
    protected boolean useShutDownHook = true;

    protected Thread shutDownHook = null;

    public Server getServer() {
        return this.server;
    }


    //Start new server instance
    public void load(){
     if(loaded){
        return;
     }
    }


    // stop existing server instance
    public void stop() {
        try {
            if (useShutDownHook) {
                Runtime.getRuntime().removeShutdownHook(shutDownHook);

                 // If JULI is being used, re-enable JULI's shutdown to ensure
                // log messages are not lost
                // LogManager logManager = LogManager.getLogManager();
                // if (logManager instanceof ClassLoaderLogManager) {
                //     ((ClassLoaderLogManager) logManager).setUseShutdownHook(true);
                // }
            }
        } catch (Exception e) {
            
        }

        // shutdown the server
        try {
            Server s = getServer();
            LifecycleState state = s.getState();

            if(LifecycleState.STOPPING_PREP.compareTo(state) <= 0 && LifecycleState.DESTROYED.compareTo(state) >= 0){
                //Nothing to do stop was already callled
            }else{
                s.stop();
                s.destroy();
            }
        } catch (LifecycleException e) {
            log.error(sm.getString("jerry.stopError"),e);
        }
    }

//Await and shutdown
    public void await(){
        getServer().await();
    }



}
