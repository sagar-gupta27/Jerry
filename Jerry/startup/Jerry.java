package startup;
//The main server class

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import startup.ClassLoaderFactory.Repository;

public final class Jerry {

    private static final Object daemonLock = new Object();

    private static volatile Jerry daemon = null;

    private static final File jerryBaseFile;
    private static final File jerryHomeFile;
    private static final Pattern PATH_PATTERN = Pattern.compile("(\"[^\"]*\")|(([^,])*)");

    private Object jerryDaemon = null;

    ClassLoader commonLoader = null;
    ClassLoader jerryLoader = null;
    ClassLoader sharedLoader = null;
    //setting jerry home and base path

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

        jerryHomeFile = homeFile;
        System.setProperty(Constants.JERRY_HOME_PROPERTY, jerryHomeFile.getPath());

        // Setting Base

        String base = System.getProperty(Constants.JERRY_BASE_PROPERTY);
        File baseFile = null;
        if (base == null) {
            jerryBaseFile = jerryHomeFile;
        } else {
            File f = new File(base);

            try {
                baseFile = f.getCanonicalFile();
            } catch (Exception e) {
                baseFile = f.getAbsoluteFile();
            }

            jerryBaseFile = baseFile;
        }

        System.setProperty(Constants.JERRY_BASE_PROPERTY, jerryBaseFile.getPath());
    }


    private void initClassLoaders(){
        try {
            //commonLoader = createClassLoader("common",null);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    private String replace(String str){
        return null;
    }


    private ClassLoader creatClassLoader(String name, ClassLoader parent) throws Exception{
        String value = JerryProperties.getProperty(name);

        if(value == null || (value.isEmpty()))
        return parent;

        replace(value);

        List<Repository> repositories = new ArrayList<>();
        return null;
    }


    public static String getJerryBase(){
        return jerryBaseFile.getPath();
    }
    public static void main(String[] args) {


    }
}
