package startup;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SystemLogHandler extends PrintStream{

    private final PrintStream out;
    public SystemLogHandler(PrintStream wrapped) {
        super(wrapped);
        out = wrapped;
    }

    private static final ThreadLocal<Deque<CaptureLog>> logs = new ThreadLocal<>();

    private static final Queue<CaptureLog> reuse = new ConcurrentLinkedQueue<>();


    public static void startCatpure(){
        CaptureLog log;

        if(!reuse.isEmpty()){
            try {
                log = reuse.remove();

            } catch (NoSuchElementException e) {
                log = new CaptureLog();
            }
        }else{
            log = new CaptureLog();
        }

        Deque<CaptureLog> stack = logs.get();

        if(stack == null){
            stack = new ArrayDeque<>();
            logs.set(stack);
        }

        stack.addFirst(log);
    }

    public static String stopCapture(){
        Queue<CaptureLog> stack = logs.get();

        if(stack == null || stack.isEmpty()){
            return null;
        }

        CaptureLog log = stack.remove();
        if(log == null) return null;

        String capture = log.getCapture();
        log.reset();
        reuse.add(log);
        return capture;
    }

    protected PrintStream findStream(){
        Queue<CaptureLog> stack = logs.get();
        if(stack != null && !stack.isEmpty()){
            CaptureLog log = stack.peek();
            if(log != null){
                PrintStream ps = log.getStream();
                if(ps != null)
                return ps;
            }
        }

        return out;
    }

      // ---------------------------------------------------- PrintStream Methods


    @Override
    public void flush() {
        findStream().flush();
    }

    @Override
    public void close() {
        findStream().close();
    }

    @Override
    public boolean checkError() {
        return findStream().checkError();
    }

    @Override
    protected void setError() {
        // findStream().setError();
    }

    @Override
    public void write(int b) {
        findStream().write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        findStream().write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        findStream().write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        findStream().print(b);
    }

    @Override
    public void print(char c) {
        findStream().print(c);
    }

    @Override
    public void print(int i) {
        findStream().print(i);
    }

    @Override
    public void print(long l) {
        findStream().print(l);
    }

    @Override
    public void print(float f) {
        findStream().print(f);
    }

    @Override
    public void print(double d) {
        findStream().print(d);
    }

    @Override
    public void print(char[] s) {
        findStream().print(s);
    }

    @Override
    public void print(String s) {
        findStream().print(s);
    }

    @Override
    public void print(Object obj) {
        findStream().print(obj);
    }

    @Override
    public void println() {
        findStream().println();
    }

    @Override
    public void println(boolean x) {
        findStream().println(x);
    }

    @Override
    public void println(char x) {
        findStream().println(x);
    }

    @Override
    public void println(int x) {
        findStream().println(x);
    }

    @Override
    public void println(long x) {
        findStream().println(x);
    }

    @Override
    public void println(float x) {
        findStream().println(x);
    }

    @Override
    public void println(double x) {
        findStream().println(x);
    }

    @Override
    public void println(char[] x) {
        findStream().println(x);
    }

    @Override
    public void println(String x) {
        findStream().println(x);
    }

    @Override
    public void println(Object x) {
        findStream().println(x);
    }
}
