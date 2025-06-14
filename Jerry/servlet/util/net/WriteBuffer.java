package servlet.util.net;

import servlet.util.buf.ByteBufferHolder;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;

public class WriteBuffer {
    private final int bufferSize;
    private final LinkedBlockingDeque<ByteBufferHolder> buffers = new LinkedBlockingDeque<>();

    public WriteBuffer(int bufferSize){
     this.bufferSize = bufferSize;
    }

    public boolean isEmpty(){
      return buffers.isEmpty();
    }

    void add(byte[] buf, int offset,  int length){
        ByteBufferHolder holder = getByteBufferHolder(length);
        holder.getBuffer().put(buf,offset,length);
    }

    private ByteBufferHolder getByteBufferHolder(int capacity){
        ByteBufferHolder holder = buffers.peekLast();

        if(holder == null || holder.isFlipped() || holder.getBuffer().remaining() < capacity){
            ByteBuffer byteBuffer = ByteBuffer.allocate(Math.max(bufferSize, capacity));
            holder = new ByteBufferHolder(byteBuffer, false);
            buffers.add(holder);
        }

        return holder;
    }
}
