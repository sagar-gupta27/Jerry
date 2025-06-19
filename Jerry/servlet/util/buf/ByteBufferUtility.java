package servlet.util.buf;

import java.nio.ByteBuffer;

public class ByteBufferUtility {

    private ByteBufferUtility() {
    }

    public static ByteBuffer expand(ByteBuffer buff, int newSize) {
        if (buff.capacity() >= newSize) {
            return buff;
        }

        ByteBuffer out;
        boolean isDirect = buff.isDirect();
        if (isDirect) {
            out = ByteBuffer.allocateDirect(newSize);
        } else {
            out = ByteBuffer.allocate(newSize);
        }

        buff.flip();
        out.put(buff);

        if(isDirect){
            clearDirectBuffer(buff);
        }
        return out;

    }


    public static void clearDirectBuffer(ByteBuffer buff){
        ByteBufferUtilityUnsafe.cleanDirectBuffer(buff);
    }

}
