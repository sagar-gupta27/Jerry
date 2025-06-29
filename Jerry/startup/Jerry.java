package startup;
//The main server class

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logging.Log;
import logging.LogFactory;
import startup.ClassLoaderFactory.Repository;
import startup.ClassLoaderFactory.RepositoryType;

public final class Jerry {

    private static Log log = LogFactory.getLog(Jerry.class);
    private static final Object daemonLock = new Object();

    private static volatile Jerry daemon = null;

    private static final File mouseletBaseFile;
    private static final File mouseletHomeFile;
    private static final Pattern PATH_PATTERN = Pattern.compile("(\"[^\"]*\")|(([^,])*)");
    // --> this parttern means start with " {anything except "} 0 or more time then
    // " OR match all characters except ,

    private Object mouseletDaemon = null;

    ClassLoader commonLoader = null;
    ClassLoader mouseletLoader = null;
    ClassLoader sharedLoader = null;
    // setting jerry home and base path

    static {

        String userDir = System.getProperty("user.dir");
        String home = System.getProperty(Constants.JERRY_HOME_PROPERTY);
        File homeFile = null;

        if (home != null) {
            File f = new File(home);

            try {
                homeFile = f.getCanonicalFile();
            } catch (Exception e) {
                homeFile = f.getAbsoluteFile();
            }
        }

        if (homeFile == null) {
            File bootstrapJar = new File(userDir, "Jerry.jar");

            if (bootstrapJar.exists()) {
                File f = new File(userDir, "..");

                try {
                    homeFile = f.getCanonicalFile();
                } catch (Exception e) {
                    homeFile = f.getAbsoluteFile();
                }
            }
        }

        if (homeFile == null) {
            File f = new File(userDir);
            try {
                homeFile = f.getCanonicalFile();
            } catch (Exception e) {
                homeFile = f.getAbsoluteFile();
            }
        }

        mouseletHomeFile = homeFile;
        System.setProperty(Constants.JERRY_HOME_PROPERTY, mouseletHomeFile.getPath());

        // Setting Base

        String base = System.getProperty(Constants.JERRY_BASE_PROPERTY);
        File baseFile = null;
        if (base == null) {
            mouseletBaseFile = mouseletHomeFile;
        } else {
            File f = new File(base);

            try {
                baseFile = f.getCanonicalFile();
            } catch (Exception e) {
                baseFile = f.getAbsoluteFile();
            }

            mouseletBaseFile = baseFile;
        }

        System.setProperty(Constants.JERRY_BASE_PROPERTY, mouseletBaseFile.getPath());
    }

    public static File getMouseletHomeFile() {
        return mouseletHomeFile;
    }

    public static File getMouseletBaseFile() {
        return mouseletBaseFile;
    }

    private String replace(String str) {
        String result = str;
        int stPos = str.indexOf("${");

        if (stPos >= 0) {
            StringBuilder strBuilder = new StringBuilder();
            int endPos = -1;

            while (stPos >= 0) {
                strBuilder.append(str, endPos + 1, stPos);

                endPos = str.indexOf("}", stPos + 2);

                if (endPos < 0) {
                    endPos = stPos - 1;
                    break;
                }

                String propName = str.substring(stPos + 1, endPos);
                String replacement;

                if (propName.isEmpty()) {
                    replacement = null;
                } else if (propName == Constants.JERRY_HOME_PROPERTY) {
                    replacement = getMouseletHome();
                } else if (propName == Constants.JERRY_BASE_PROPERTY) {
                    replacement = getMouseletBase();
                } else {
                    replacement = System.getProperty(propName);
                }

                if (replacement != null) {
                    strBuilder.append(replacement);
                } else {
                    strBuilder.append(str, stPos, endPos + 1);
                }

                stPos = str.indexOf("${", endPos + 1);
            }

            strBuilder.append(str, endPos + 1, str.length());
            result = strBuilder.toString();
        }

        return result;
    }

    private ClassLoader creatClassLoader(String name, ClassLoader parent) throws Exception {
        String value = JerryProperties.getProperty(name + ".loader");

        if (value == null || (value.isEmpty()))
            return parent;

        replace(value);

        List<Repository> repos = new ArrayList<>();

        String[] repoPaths = getPaths(value);

        for (String repo : repoPaths) {

            try {
                URI uri = new URI(repo);
                URL url = uri.toURL();
                repos.add(new Repository(repo, RepositoryType.URL));
                continue;
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {

            }

            if (repo.endsWith("*.jar")) {
                repo = repo.substring(0, repo.length() - "*.jar".length());
                repos.add(new Repository(repo, RepositoryType.GLOB));
            } else if (repo.endsWith(".jar")) {
                repos.add(new Repository(repo, RepositoryType.JAR));
            } else {
                repos.add(new Repository(repo, RepositoryType.DIR));
            }
        }

        return ClassLoaderFactory.creatClassLoader(repos, parent);
    }

    public String[] getPaths(String value) {
        List<String> result = new ArrayList<>();
        Matcher matcher = PATH_PATTERN.matcher(value);

        while (matcher.find()) {
            String path = value.substring(matcher.start(), matcher.end());

            path = path.trim();

            if (path.isEmpty())
                continue;

            char first = path.charAt(0);
            char last = path.charAt(path.length() - 1);

            if (first == '"' && last == '"' && path.length() > 1) {
                path = path.substring(1, path.length() - 1);
                path = path.trim();
                if (path.isEmpty()) {
                    continue;
                }
            } else if (path.contains("\"")) {
                // Unbalanced quotes
                // Too early to use standard i18n support. The class path hasn't
                // been configured.
                throw new IllegalArgumentException(
                        "The double quote [\"] character can only be used to quote paths. It must " +
                                "not appear in a path. This loader path is not valid: [" + value + "]");
            } else {
                // Not quoted - NO-OP
            }

            result.add(path);
        }

        return result.toArray(new String[0]);
    }

    public static String getMouseletBase() {
        return mouseletBaseFile.getPath();
    }

    public static String getMouseletHome() {
        return mouseletHomeFile.getPath();
    }

    private void initClassLoaders() {

        try {
            commonLoader = creatClassLoader("common", null);

            if (commonLoader == null) {
                commonLoader = this.getClass().getClassLoader();
            }

            mouseletLoader = creatClassLoader("server", commonLoader);
            sharedLoader = creatClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            handleThrowable(t);
            log.error("Classloade creation threw exception", t);
            System.exit(1);
        }
    }

    public void init() throws Exception {
        initClassLoaders();

        Thread.currentThread().setContextClassLoader(mouseletLoader);

        // Load the startup class

        if (log.isTraceEnabled()) {
            log.trace("Loading startup class mouselet");
        }

        Class<?> starterClass = mouseletLoader.loadClass("Jerry.startup.Mouselet");
        Object starterInstance = starterClass.getConstructor().newInstance();

        if (log.isTraceEnabled()) {
            log.trace("setting starter class properties");
        }

        String methodName = "setParentClassLoader";
        Class<?>[] paramTypes = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");

        Object[] paramVals = new Object[1];

        paramVals[0] = sharedLoader;

        Method method = starterInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(starterInstance, paramVals);

        mouseletDaemon = starterInstance;
    }

    public void load(String[] args) throws Exception {

        String methodName = "load";
        Object[] param;
        Class<?>[] paramType;

        if (args == null || args.length == 0) {
            param = null;
            paramType = null;
        } else {
            paramType = new Class[1];
            paramType[0] = args.getClass();
            param = new Object[1];
            param[0] = args;
        }

        Method method = mouseletDaemon.getClass().getMethod(methodName, paramType);
        if (log.isTraceEnabled()) {
            log.trace("calling startup class " + method);
        }

        method.invoke(mouseletDaemon, param);
    }

    public static void main(String[] args) {

        synchronized (daemonLock) {
            if (daemon == null) {
                Jerry jerry = new Jerry();
                try {
                    jerry.init();
                } catch (Throwable t) {
                    handleThrowable(t);
                    log.error("Init Exception");
                    return;
                }

                daemon = jerry;
            } else {
                Thread.currentThread().setContextClassLoader(daemon.mouseletLoader);
            }
        }

        try {
            String cmd = "start";

            if (args.length > 0) {
                cmd = args[args.length - 1];
            }

            switch (cmd) {
                case "startd":
                    args[args.length - 1] = "start";
                    daemon.load(args);
                    daemon.start();
                    break;
                case "stopd":
                    args[args.length - 1] = "stop";
                    daemon.stop();
                    break;
                case "start":
                    daemon.setAwait(true);
                    daemon.load(args);
                    daemon.start();

                    if (daemon.getServer() == null) {
                        System.exit(1);
                    }
                    break;
                case "stop":
                    daemon.stopServer();
                    break;
                case "configtest":
                    daemon.load(args);
                    if (daemon.getServer() == null) {
                        System.exit(1);
                    }

                    System.exit(0);
                default:
                    log.warn("Jerry cmd \"" + cmd + "\" doesnt exist.");
                    break;
            }
        } catch (Throwable t) {
            Throwable throwable = t;
            if (throwable instanceof InvocationTargetException && throwable.getCause() != null) {
                throwable = throwable.getCause();
            }

            handleThrowable(throwable);
            log.error("Error running cmd" + throwable);
            System.exit(1);
        }
    }

    public void init(String[] args) throws Exception {
        init();
        load(args);
    }

    public void start() throws Exception {
        if (mouseletDaemon == null) {
            init();
        }

        Method method = mouseletDaemon.getClass().getMethod("start", (Class<?>[]) null);
        method.invoke(mouseletDaemon, (Object[]) null);
    }

    // stop the mouselet daemon
    public void stop() throws Exception {
        Method method = mouseletDaemon.getClass().getMethod("stop", (Class<?>[]) null);
        method.invoke(mouseletDaemon, (Object[]) null);
    }

    // stop the standlone server
    public void stopServer() throws Exception {

        Method method = mouseletDaemon.getClass().getMethod("stopServer", (Class<?>[]) null);
        method.invoke(mouseletDaemon, (Object[]) null);
    }

    public void setAwait(boolean await) throws Exception {

        Class<?>[] paramTypes = new Class[1];
        paramTypes[0] = Boolean.TYPE;
        Object[] paramValues = new Object[1];
        paramValues[0] = Boolean.valueOf(await);
        Method method = mouseletDaemon.getClass().getMethod("setAwait", paramTypes);
        method.invoke(mouseletDaemon, paramValues);
    }

    public boolean getAwait() throws Exception {
        Class<?>[] paramTypes = new Class[0];
        Object[] paramValues = new Object[0];
        Method method = mouseletDaemon.getClass().getMethod("getAwait", paramTypes);
        Boolean b = (Boolean) method.invoke(mouseletDaemon, paramValues);
        return b.booleanValue();
    }

    public void stopServer(String[] arguments) throws Exception {

        Object[] param;
        Class<?>[] paramTypes;
        if (arguments == null || arguments.length == 0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method = mouseletDaemon.getClass().getMethod("stopServer", paramTypes);
        method.invoke(mouseletDaemon, param);
    }

    // destory mouslet
    public void destory() {

    }

    private Object getServer() throws Exception {

        String methodName = "getServer";
        Method method = mouseletDaemon.getClass().getMethod(methodName);
        return method.invoke(mouseletDaemon);
    }

    static void handleThrowable(Throwable t) {
        if (t instanceof StackOverflowError) {
            // Swallow silently - it should be recoverable
            return;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }
}
