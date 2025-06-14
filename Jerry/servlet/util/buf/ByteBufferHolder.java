package servlet.util.buf;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ByteBufferHolder {
    private ByteBuffer buffer;
    private final AtomicBoolean flipped;

    public ByteBufferHolder(ByteBuffer buff,boolean flipped){
        this.buffer = buff;
        this.flipped = new AtomicBoolean(flipped);
    }

    public ByteBuffer getBuffer(){
        return buffer;
    }

    public boolean isFlipped(){
        return flipped.get();
    }

    public boolean flip(){ // flips only once
        if(flipped.compareAndSet(false, true)){
            buffer.flip(); // change from write mode to read
            return true;
        }else return false;
    }
}
