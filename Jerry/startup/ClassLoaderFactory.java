package startup;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import logging.Log;
import logging.LogFactory;

public final class ClassLoaderFactory {
    private static final Log log = LogFactory.getLog(ClassLoaderFactory.class);

    public static ClassLoader creatClassLoader(List<Repository> repositories, final ClassLoader parent)
            throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("creating new class loader");
        }

        // Construct classpath for class loader

        Set<URL> set = new LinkedHashSet<>();

        if (repositories != null) {
            for (Repository repo : repositories) {

                if (repo.type() == RepositoryType.URL) {
                    URL url = buildClassLoaderUrl(repo.location());

                    if (log.isDebugEnabled()) {
                        log.debug("Adding URL" + url);
                    }

                    set.add(url);
                } else if (repo.type() == RepositoryType.JAR) {
                    File file = new File(repo.location());
                    file = file.getCanonicalFile();

                    if (!validatefile(file, RepositoryType.URL)) {
                        continue;
                    }

                    URL url = buildClassLoaderUrl(file);
                    set.add(url);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding jar file" + url);
                    }

                } else if (repo.type() == RepositoryType.DIR) {
                    File dir = new File(repo.location());
                    dir = dir.getCanonicalFile();

                    if (!validatefile(dir, RepositoryType.DIR)) {
                        continue;
                    }

                    URL url = buildClassLoaderUrl(dir);
                    set.add(url);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding  dir" + url);
                    }
                } else if (repo.type() == RepositoryType.GLOB) {
                    File dir = new File(repo.location());
                    dir = dir.getCanonicalFile();

                    if (!validatefile(dir, RepositoryType.GLOB)) {
                        continue;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Including directory glob");
                    }

                    String filenames[] = dir.list();

                    if (filenames == null) {
                        continue;
                    }

                    for (String s : filenames) {
                        String fname = s.toLowerCase(Locale.ENGLISH);

                        if (!fname.endsWith(".jar")) {
                            continue;
                        }

                        File file = new File(dir, fname);
                        file = file.getCanonicalFile();

                        if (!validatefile(file, RepositoryType.JAR)) {
                            continue;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Including Glob jar file" + file.getAbsolutePath());
                        }

                        URL url = buildClassLoaderUrl(file);
                        set.add(url);
                    }
                }
            }
        }

        final URL[] arr = set.toArray(new URL[0]);

        for (int i = 0; i < arr.length; i++) {
            if (log.isTraceEnabled()) {
                log.trace("location" + i + "is" + arr[i]);
            }
        }

        if (parent == null)
            return new URLClassLoader(arr);
        else
            return new URLClassLoader(arr, parent);
    }

    public static ClassLoader createClassLoader(File[] unpacked, File[] packed, final ClassLoader parent)
            throws IOException {

        Set<URL> set = new LinkedHashSet<>();
        if (unpacked != null) {
            for (File file : unpacked) {

                if (!file.canRead()) {
                    continue;
                }

                file = new File(file.getCanonicalPath());
                URL url = file.toURI().toURL();

                if (log.isDebugEnabled()) {
                    log.debug("Adding dir " + url);
                }

                set.add(url);

            }
        }

        if (packed != null) {
            for (File dir : packed) {

                if (!dir.isDirectory() || dir.canRead()) {
                    continue;
                }

                String fnames[] = dir.list();

                for (String s : fnames) {

                    String fname = s.toLowerCase(Locale.ENGLISH);
                    if (!fname.endsWith(".jar")) {
                        continue;
                    }

                    File file = new File(dir, s);

                    if (log.isDebugEnabled()) {
                        log.debug("Adding jar file" + file.getAbsolutePath());
                    }

                    URL url = file.toURI().toURL();
                    set.add(url);
                }
            }
        }

        URL urls[] = set.toArray(new URL[0]);

        if (parent == null)
            return new URLClassLoader(urls);
        else
            return new URLClassLoader(urls, parent);
    }

    public static boolean validatefile(File file, RepositoryType type) throws IOException {

        if (type == RepositoryType.DIR || type == RepositoryType.GLOB) {

            if (!file.isDirectory() || !file.canRead()) {
                String msg = "Problem with directory[" + file + "] , exits[" + file.exists() + "],  canRead["
                        + file.canRead() + "], isDir[" + file.isDirectory() + "]";

                File home = new File(Jerry.getJerryHome());
                home = home.getCanonicalFile();

                File base = new File(Jerry.getJerryBase());
                base = base.getCanonicalFile();

                File defaultVal = new File(base, "lib");

                // In case home and base path are different ${jerry.base}/lib needs not be
                // present hide warn

                if (!home.getPath().equals(base.getParent()) && defaultVal.getPath().equals(file.getPath())
                        && !file.exists()) {
                    log.debug(msg);
                } else {
                    log.warn(msg);
                }
            }
            return false;
        } else if (type == RepositoryType.JAR) {
            if (!file.canRead()) {
                String msg = "Problem with jar [" + file + "] , exits[" + file.exists() + "],  canRead["
                        + file.canRead() + "]";

                log.warn(msg);
                return false;
            }
        }

        return true;
    }

    public static URL buildClassLoaderUrl(File file) throws MalformedURLException, URISyntaxException {
        String strUrl = file.toURI().toString();
        strUrl = strUrl.replace("!/", "%21/");
        return new URI(strUrl).toURL();
    }

    public static URL buildClassLoaderUrl(String url) throws MalformedURLException, URISyntaxException {

        /*
         * In case we have a folder named fold! the path may look like -->
         * path/to/fold!/file.txt
         * which looks like jars internal path --> file.txt is some file inside jar
         * fold.jar
         * to avoid this , replace ! with its URL encoded- version i.e %21
         * 
         */
        String result = url.replace("!/", "%21/");

        return new URI(result).toURL();
    }

    public record Repository(String location, RepositoryType type) {
    }

    public enum RepositoryType {
        DIR,
        URL,
        JAR,
        GLOB
    }
}
