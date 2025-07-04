package startup;
//Main servlet container class

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import servlet.util.digester.Digester;
import jerry.ExceptionUtils;
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
    protected boolean useNaming = true;
    protected Server server = null;
    protected boolean loaded = false;
    protected boolean useShutdownHook = true;

    protected Thread shutDownHook = null;

    /**
     * Rethrow exceptions on init failure.
     */
    protected boolean throwOnInitFailure = Boolean.getBoolean("Jerry.startup.EXIT_ON_INIT_FAILURE");

    /**
     * Generate Tomcat embedded code from configuration files.
     */
    protected boolean generateCode = false;

    /**
     * Location of generated sources.
     */
    protected File generatedCodeLocation = null;

    /**
     * Value of the argument.
     */
    protected String generatedCodeLocationParameter = null;

    /**
     * Top package name for generated source.
     */
    protected String generatedCodePackage = "jerryembedded";

    /**
     * Use generated code as a replacement for configuration files.
     */
    protected boolean useGeneratedCode = false;

    public Mouselet() {
        ExceptionUtils.preload();
    }

    public void setConfigFile(String file) {
        configFile = file;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setUseShutdownHook(boolean useShutdownHook) {
        this.useShutdownHook = useShutdownHook;
    }

    public boolean getUseShutdownHook() {
        return useShutdownHook;
    }

    public boolean getGenerateCode() {
        return this.generateCode;
    }

    public void setGenerateCode(boolean generateCode) {
        this.generateCode = generateCode;
    }

    public boolean getUseGeneratedCode() {
        return this.useGeneratedCode;
    }

    public void setUseGeneratedCode(boolean useGeneratedCode) {
        this.useGeneratedCode = useGeneratedCode;
    }

    public File getGeneratedCodeLocation() {
        return this.generatedCodeLocation;
    }

    public void setGeneratedCodeLocation(File generatedCodeLocation) {
        this.generatedCodeLocation = generatedCodeLocation;
    }

    public String getGeneratedCodePackage() {
        return this.generatedCodePackage;
    }

    public void setGeneratedCodePackage(String generatedCodePackage) {
        this.generatedCodePackage = generatedCodePackage;
    }

    /**
     * @return <code>true</code> if an exception should be thrown if an error occurs
     *         during server init
     */
    public boolean getThrowOnInitFailure() {
        return throwOnInitFailure;
    }

    /**
     * Set the behavior regarding errors that could occur during server init.
     *
     * @param throwOnInitFailure the new flag value
     */
    public void setThrowOnInitFailure(boolean throwOnInitFailure) {
        this.throwOnInitFailure = throwOnInitFailure;
    }

    /**
     * Set the shared extensions class loader.
     *
     * @param parentClassLoader The shared extensions class loader.
     */
    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null) {
            return parentClassLoader;
        }
        return ClassLoader.getSystemClassLoader();
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return this.server;
    }

    // Start new server instance
    public void load() {
        if (loaded) {
            return;
        }

        loaded = true;

        long t1 = System.nanoTime();

        initNaming();

        parseServerXml(true);

        Server s = getServer();

        if (s == null) {
            return;
        }

        getServer().setMouselet(this);
        getServer().setMouseletHome(Jerry.getMouseletHomeFile());
        getServer().setMouseletBase(Jerry.getMouseletBaseFile());

        initStreams();

        // Starting the new server
        try {
            getServer().init();

        } catch (LifecycleException e) {
            if (throwOnInitFailure) {
                throw new Error(e);
            } else {
                log.error(sm.getString("catalina.initError"), e);
            }
        }

        if (log.isInfoEnabled()) {
            log.info(sm
                    .getString("catalina.init" + Long.toString(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t1))));
        }

    }

    public void load(String[] args) {
        try {
            if (arguments(args)) {
                load();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    void initStreams() {
        System.setOut(new SystemLogHandler(System.out));
        System.setErr(new SystemLogHandler(System.err));
    }

    protected void initNaming() {
        // Setting additional variables
        if (!useNaming) {
            log.info(sm.getString("mouselet.noNaming"));
            System.setProperty("mouselet.useNaming", "false");
        } else {
            System.setProperty("mouselet.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = value + ":" + oldValue;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("mouselet.namingPrefix", value));
            }
            value = System.getProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
            if (value == null) {
                System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.naming.java.javaURLContextFactory");
            } else {
                log.debug(sm.getString("mouselet.initialContextFactory", value));
            }
        }
    }

    protected void generateLoader() {
        String loaderClassName = "DigesterGeneratedCodeLoader";
        StringBuilder code = new StringBuilder();
        code.append("package ").append(generatedCodePackage).append(';').append(System.lineSeparator());
        code.append("public class ").append(loaderClassName);
        code.append(" implements org.apache.tomcat.util.digester.Digester.GeneratedCodeLoader {")
                .append(System.lineSeparator());
        code.append("public Object loadGeneratedCode(String className) {").append(System.lineSeparator());
        code.append("switch (className) {").append(System.lineSeparator());
        for (String generatedClassName : Digester.getGeneratedClasses()) {
            code.append("case \"").append(generatedClassName).append("\" : return new ").append(generatedClassName);
            code.append("();").append(System.lineSeparator());
        }
        code.append("default: return null; }").append(System.lineSeparator());
        code.append("}}").append(System.lineSeparator());
        File loaderLocation = new File(generatedCodeLocation, generatedCodePackage);
        try (FileWriter writer = new FileWriter(new File(loaderLocation, loaderClassName + ".java"))) {
            writer.write(code.toString());
        } catch (IOException e) {
            // Should not happen
            log.debug(sm.getString("catalina.loaderWriteFail"), e);
        }
    }


    // stop existing server instance
    public void stop() {
        try {
            if (useShutdownHook) {
                Runtime.getRuntime().removeShutdownHook(shutDownHook);

                // If JULI is being used, re-enable JULI's shutdown to ensure
                // log messages are not lost
                // LogManager logManager = LogManager.getLogManager();
                // if (logManager instanceof ClassLoaderLogManager) {
                // ((ClassLoaderLogManager) logManager).setUseShutdownHook(true);
                // }
            }
        } catch (Exception e) {

        }

        // shutdown the server
        try {
            Server s = getServer();
            LifecycleState state = s.getState();

            if (LifecycleState.STOPPING_PREP.compareTo(state) <= 0 && LifecycleState.DESTROYED.compareTo(state) >= 0) {
                // Nothing to do stop was already callled
            } else {
                s.stop();
                s.destroy();
            }
        } catch (LifecycleException e) {
            log.error(sm.getString("jerry.stopError"), e);
        }
    }

    // Await and shutdown
    public void await() {
        getServer().await();
    }

    public boolean isUseNaming() {
        return this.useNaming;
    }

    /**
     * Enables or disables naming support.
     *
     * @param useNaming The new use naming value
     */
    public void setUseNaming(boolean useNaming) {
        this.useNaming = useNaming;
    }

    public void setAwait(boolean b) {
        await = b;
    }

    public boolean isAwait() {
        return await;
    }

    protected boolean arguments(String[] args) {
        boolean isConfig = false;
        boolean isGenerateCode = false;

        if (args.length < 1) {
            usage();
            return false;
        }

        for (String arg : args) {
            if (isConfig) {
                configFile = arg;
                isConfig = false;
            } else if (arg.equals("-config")) {
                isConfig = true;
            } else if (arg.equals("-generateCode")) {
                setGenerateCode(true);
                isGenerateCode = true;
            } else if (arg.equals("-useGeneratedCode")) {
                setUseGeneratedCode(true);
                isGenerateCode = false;
            } else if (arg.equals("-nonaming")) {
                setUseNaming(false);
                isGenerateCode = false;
            } else if (arg.equals("-help")) {
                usage();
                return false;
            } else if (arg.equals("start")) {
                isGenerateCode = false;
                // NOOP
            } else if (arg.equals("configtest")) {
                isGenerateCode = false;
                // NOOP
            } else if (arg.equals("stop")) {
                isGenerateCode = false;
                // NOOP
            } else if (isGenerateCode) {
                generatedCodeLocationParameter = arg;
                isGenerateCode = false;
            } else {
                usage();
                return false;
            }
        }
        return true;

    }

    protected File configFile() {
        File file = new File(configFile);
        if (!file.isAbsolute()) {
            file = new File(Jerry.getMouseletBase(), configFile);
        }

        return file;
    }

    // May or May not implement , based on whether to use XML based config or not
    protected Digester createStartDigester() {
        return null;
    }

    protected Digester createStopDigester() {
        return null;
    }

    protected void parseServerXml(boolean start) {

    }

    public void stopServer() {
        stopServer(null);
    }

    public void stopServer(String[] args) {
        if (args != null) {
            arguments(args);
        }

        Server s = getServer();

        if (s == null) {
            parseServerXml(false);

            if (getServer() == null) {
                log.error(sm.getString("jerry.error"));
                System.exit(1);
            }
        } else {
            try {
                s.stop();
                s.destroy();
            } catch (LifecycleException e) {
                log.error("jerry.error");
            }

            return;
        }

        s = getServer();

        if (s.getPortWithOffset() > 0) {
            try (Socket socket = new Socket(s.getAddress(), s.getPortWithOffset());
                    OutputStream stream = socket.getOutputStream()) {
                String shutdown = s.getShutdown();

                for (int i = 0; i < shutdown.length(); i++) {
                    stream.write(shutdown.charAt(i));
                }

                stream.flush();
            } catch (ConnectException e) {
                log.error(sm.getString("jerry.stopServer.connectException" + s.getAddress() +
                        String.valueOf(s.getPortWithOffset()) + String.valueOf(s.getPort()),
                        String.valueOf(s.getPortOffset())));
                log.error(sm.getString("jerry.stopError"), e);
                System.exit(1);
            } catch (IOException e) {
                log.error(sm.getString("jerry.stopError"), e);
                System.exit(1);
            }
        } else {
            log.error(sm.getString("jerry.stopServer"));
            System.exit(1);
        }
    }

    protected void usage() {
    }
}
