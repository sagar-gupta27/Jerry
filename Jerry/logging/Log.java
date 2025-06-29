package logging;

public interface Log {
    boolean isDebugEnabled();
    boolean isErrorEnabled();
    boolean isFatalEnabled();
    boolean isInfoEnabled();
    boolean isTraceEnabled();
    boolean isWarnEnabled();

    void trace(Object msg);
    void warn(Object msg);
    void error(Object msg);
    void fatal(Object msg);
    void debug(Object msg);

    void info(Object msg);


    void trace(Object msg , Throwable t);
    void warn(Object msg , Throwable t);
    void error(Object msg , Throwable t);
    void fatal(Object msg , Throwable t);
    void debug(Object msg , Throwable t);

    void info(Object msg, Throwable t);
}
