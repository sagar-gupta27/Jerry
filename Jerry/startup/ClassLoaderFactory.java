package startup;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import logging.Log;
import logging.LogFactory;

public final class ClassLoaderFactory {
    private static final Log log = LogFactory.getLog(ClassLoaderFactory.class);

    public static ClassLoader creatClassLoader(List<Repository> repositories, ClassLoader parent) throws Exception {

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

                    if (!validatefile(file)) {
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

                    if (!validatefile(dir)) {
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

                    if (!validatefile(dir)) {
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

    public static boolean validatefile(File file, RepositoryType type) {
        return true;
    }

    public static boolean validatefile(File file) {
        return true;
    }

    public static URL buildClassLoaderUrl(File file) {
        return null;
    }

    public static URL buildClassLoaderUrl(String url) {
        return null;
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
