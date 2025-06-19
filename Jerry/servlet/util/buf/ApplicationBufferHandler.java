package servlet.util.buf;

import java.nio.ByteBuffer;

public interface ApplicationBufferHandler {
    ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    ApplicationBufferHandler EMPTY = new ApplicationBufferHandler() {

        @Override
        public void setByteBuffer(ByteBuffer buff) {
        }

        @Override
        public ByteBuffer getByteBuffer() {
           return EMPTY_BUFFER;
        }

        @Override
        public void expand(int size) {
        }
        
    };

    public void setByteBuffer(ByteBuffer buff);
    public ByteBuffer getByteBuffer();
    public void expand(int size);

}
