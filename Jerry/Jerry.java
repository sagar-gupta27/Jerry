//The main server class

import java.io.File;
import java.util.regex.Pattern;

public final class Jerry {

    private static final Object daemonLock = new Object();

    private static volatile Jerry daemon = null;

    private static final Pattern PATH_PATTERN = Pattern.compile("(\"[^\"]*\")|(([^,])*)")

    static {

        String userDir = System.getProperty("user.dir");
        String home = System.getProperty(Constants.JERRY_HOME_PROPERTY);
        File homeFile = null;

        if (home != null) {
            File f = new File(home);

        }

    }

    public static void main(String[] args) {


    }
}
