package servlet.util;

public final class XReflectionIntrospectionUtils {

    public static boolean isEnabled() {
        return false;
    }

    public static boolean setPropertyInternal(Object o, String name, String value, boolean invokeSetProperty) {
        throw new UnsupportedOperationException("Unsupported method 'setPropertyInternal'");
    }

    public static Object getPropertyInternal(Object o, String name) {
        throw new UnsupportedOperationException("Unsupported method 'getPropertyInternal'");
    }

}
