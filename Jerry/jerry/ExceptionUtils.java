package jerry;

public class ExceptionUtils {
    public static void handleThrowable(Throwable t){
        if(t instanceof StackOverflowError) return;

        if(t instanceof VirtualMachineError) throw (VirtualMachineError)t;

    }

    //Calling a static function forces the JVM to load the class
    //This prevents masking of original error in case this class is not loaded
    public static void preload(){
        //NO - OP
    }
}
