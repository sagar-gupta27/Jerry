package logging;

import java.util.logging.Logger;;

public class JDKLog implements Log{
       public final Logger logger;
     JDKLog(String name) {
        logger = Logger.getLogger(name);
    }

    @Override
    public boolean isDebugEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDebugEnabled'");
    }

    @Override
    public boolean isErrorEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isErrorEnabled'");
    }

    @Override
    public boolean isFatalEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isFatalEnabled'");
    }

    @Override
    public boolean isInfoEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isInfoEnabled'");
    }

    @Override
    public boolean isTraceEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isTraceEnabled'");
    }

    @Override
    public boolean isWarnEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isWarnEnabled'");
    }

    @Override
    public void trace(Object msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'trace'");
    }

    @Override
    public void warn(Object msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'warn'");
    }

    @Override
    public void error(Object msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'error'");
    }

    @Override
    public void fatal(Object msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fatal'");
    }

    @Override
    public void debug(Object msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'debug'");
    }

    @Override
    public void trace(Object msg, Throwable t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'trace'");
    }

    @Override
    public void warn(Object msg, Throwable t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'warn'");
    }

    @Override
    public void error(Object msg, Throwable t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'error'");
    }

    @Override
    public void fatal(Object msg, Throwable t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fatal'");
    }

    @Override
    public void debug(Object msg, Throwable t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'debug'");
    }
    
    static Log getInstance(String name) {
        return new JDKLog(name);
    }
}
