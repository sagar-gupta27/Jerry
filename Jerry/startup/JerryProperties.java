package startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.VirtualMachineError;
import logging.Log;
import logging.LogFactory;

public class JerryProperties {
    private static Properties properties = null;
    private static Log log = LogFactory.getLog(JerryProperties.class);

    static{
        loadProperties();
    }


    public static String getProperty(String name){
        return properties.getProperty(name);
    }




    private static void loadProperties(){
       InputStream inputStream = null;
       String fileName = "jerry.properties";

       try {
          String configUrl = System.getProperty("jerry.config");

          if(configUrl != null){

            if(configUrl.indexOf('/') == -1){
                fileName = configUrl;
            }else{
                inputStream = new URI(configUrl).toURL().openStream();
            }
          }
       } catch (Throwable t) {
        handleThrowable(t);
       }

       if(inputStream == null){
          try {
             File home = new File(Jerry.getJerryBase());
             File conf = new File(home, "conf");
             File propsFile = new File(conf, fileName);
             inputStream = new FileInputStream(propsFile);
          } catch (Throwable t) {
            handleThrowable(t);
          }
       }


       if(inputStream == null){
        try {
            inputStream = JerryProperties.class.getResourceAsStream("path/to/default/jerry/properties");
        } catch (Throwable t) {
            handleThrowable(t);
        }
       }


       if(inputStream != null){
        try {
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            handleThrowable(e);
        }
        finally{
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("Could not close jerry properties file");
                e.printStackTrace();
            }
        }
       }

       if(inputStream == null){
         log.warn("Failed to load jerry properties file");
         properties = new Properties(); // default
       }

       //Register these properties as system properties
       Enumeration<?> enumm = properties.propertyNames();

       while(enumm.hasMoreElements()){
        String name = (String) enumm.nextElement();
        String value = properties.getProperty(name);

        if(value != null){
            System.setProperty(name, value);
        }
       }

    }


    private static void handleThrowable(Throwable t){
         if( t instanceof VirtualMachineError){
            throw (VirtualMachineError) t;
         }
    }
}
