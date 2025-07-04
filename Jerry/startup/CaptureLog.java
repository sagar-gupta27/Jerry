package startup;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CaptureLog {

    public CaptureLog(){
         baos = new ByteArrayOutputStream();
         ps = new PrintStream(baos);
    }

    private final ByteArrayOutputStream baos;
    private final PrintStream ps;

    protected void reset(){
        baos.reset();
    }

    protected String getCapture(){
        return baos.toString();
    }

    protected PrintStream getStream(){
        return ps;
    }
}
