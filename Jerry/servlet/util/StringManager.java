package servlet.util;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StringManager {
    
    private static final Map<String,Map<Locale,StringManager>> managers = new HashMap<>();

    private static final int LOCALE_CACHE_SIZE = 10;

    private StringManager(String packageName , Locale locale){
    
    }


    public static StringManager getManager(Class<?> clazz){
        return getManager(clazz.getPackage().getName());
    }

    public static StringManager getManager(String packageName){
        return getManager(packageName, Locale.getDefault());
    }

    public static StringManager getManager(String packageName , Locale locale){
       Map<Locale,StringManager> map = managers.get(packageName);

       if(map == null){


         map = new LinkedHashMap<>(LOCALE_CACHE_SIZE, 0.75f, true) {
                @Serial
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<Locale,StringManager> eldest) {
                    return size() > (LOCALE_CACHE_SIZE - 1);
                }
            };
            managers.put(packageName, map);
       }


       StringManager smgr = map.get(locale);

       if(smgr == null){
        smgr = new StringManager(packageName,locale);
        map.put(locale, smgr);
       }

       return smgr;
    }


    public String getString(String  key){
        return null;
    }

    public String getString(String key , Object obj){
        return null;
    }
}
