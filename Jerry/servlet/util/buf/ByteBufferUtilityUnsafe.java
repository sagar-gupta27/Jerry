package servlet.util.buf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import logging.Log;
import logging.LogFactory;
import servlet.util.StringManager;

public class ByteBufferUtilityUnsafe {

    private static final StringManager sm = StringManager.getManager(ByteBufferUtilityUnsafe.class);
    private static final Log log = LogFactory.getLog(ByteBufferUtilityUnsafe.class);

    private static final Object unsafe;
    private static final Method invokeCleanerMethod;
    static {
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(0);

        Object tempUnsafe;
        Method tempInvokeCleaner;

        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = clazz.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            tempUnsafe = theUnsafe.get(null);
            tempInvokeCleaner = clazz.getMethod("invokeCleaner", ByteBuffer.class);
            tempInvokeCleaner.invoke(tempUnsafe, tempBuffer); // call the underlaying method (invokeCleaner) on the
                                                              // given objet (tempUnsafe) with given parameters
                                                              // (tempBuffer)
        } catch (Exception e) {
            log.warn(sm.getString("byteBufferUtils.cleaner"), e);
            tempUnsafe = null;
            tempInvokeCleaner = null;
        }
        unsafe = tempUnsafe;
        invokeCleanerMethod = tempInvokeCleaner;
    }

    private ByteBufferUtilityUnsafe() {
        // private constructor as its a utility class
    }

    static void cleanDirectBuffer(ByteBuffer buff) {
        if (invokeCleanerMethod != null) {
            try {
                invokeCleanerMethod.invoke(unsafe, buff);
            } catch (Exception e) {
                if(log.isDebugEnabled()){
                    log.debug(sm.getString("bufferUtilUnsafe"), e);
                }
            }
        }
    }

}
