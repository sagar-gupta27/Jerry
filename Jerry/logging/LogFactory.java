package logging;

import java.lang.reflect.Constructor;
import java.nio.file.FileSystems;
import java.util.ServiceLoader;

public class LogFactory {
    private static final LogFactory singleton = new LogFactory();

    private final Constructor<? extends Log> discoveredLogConstructor ;

    private LogFactory(){
      FileSystems.getDefault();

      //Check using service loader if any implementation of log with constructor with string name exits
      ServiceLoader<Log> logLoader = ServiceLoader.load(Log.class); 
      Constructor<? extends Log> m = null;

      for(Log log : logLoader){
        Class<? extends Log> c = log.getClass();

        try {
              m = c.getConstructor(String.class);
              break;
        } catch (Exception e) {
            throw new Error(e);
        }
      }
      
      discoveredLogConstructor = m;
    }
    


    public Log getInstance(String name) throws Exception{
      if(discoveredLogConstructor == null){
        return JDKLog.getInstance(name);
      }


      try {
        return discoveredLogConstructor.newInstance(name);
      } catch (Exception e) {
        throw new Exception();
      }
    }

    public Log getInstance(Class<?> clazz) {
        return getInstance(clazz.getClass());
    }
    public static LogFactory getFactory() {
        return singleton;
    }

    public static Log getLog(Class<?> clazz) {
        return getFactory().getInstance(clazz);
    }
}
