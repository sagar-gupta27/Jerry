package servlet.util.net;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import servlet.util.buf.ByteBufferUtility;

public class SocketBufferWrapper {
    private volatile boolean readBufferConfiguredForWrite = true;
    private volatile ByteBuffer readBuffer;

    private volatile boolean writeBufferConfiguredForWrite = true;
    private volatile ByteBuffer writeBuffer;
    private final boolean direct; // to select the type of buffer wrapped by the wrapper

    public SocketBufferWrapper(int readBufferSize, int writeBufferSize, boolean direct) {
        this.direct = direct;
        if (direct) {
            readBuffer = ByteBuffer.allocateDirect(readBufferSize); // Allocated in navtive heap
            writeBuffer = ByteBuffer.allocateDirect(writeBufferSize);
        } else {
            readBuffer = ByteBuffer.allocate(readBufferSize); // Allocated in JVM memory
            writeBuffer = ByteBuffer.allocate(writeBufferSize);
        }
    }

    // Read Buffer Related Functions
    public void configureReadBufferForWrite() {
        setReadBufferConfiguredForWrite(true);
    }

    public void configureReadBufferForRead() {
        setReadBufferConfiguredForWrite(false);
    }

    private void setReadBufferConfiguredForWrite(boolean readBufferConFiguredForWrite) {
        // NO-OP if buffer is already in correct state
        if (this.readBufferConfiguredForWrite != readBufferConFiguredForWrite) {
            if (readBufferConFiguredForWrite) {
                // Switching to write
                int remaining = readBuffer.remaining();
                if (remaining == 0) {
                    readBuffer.clear();
                } else {
                    readBuffer.compact();
                }
            } else {
                // Switching to read
                readBuffer.flip();
            }
            this.readBufferConfiguredForWrite = readBufferConFiguredForWrite;
        }
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public boolean isReadBufferEmpty() {
        if (readBufferConfiguredForWrite) {
            return readBuffer.position() == 0;
        } else {
            return readBuffer.remaining() == 0;
        }
    }

    /// Write buffer related functions

       public void configureWriteBufferForWrite() {
        setWriteBufferConfiguredForWrite(true);
    }


    public void configureWriteBufferForRead() {
        setWriteBufferConfiguredForWrite(false);
    }

    private void setWriteBufferConfiguredForWrite(boolean writeBufferConfiguredForWrite) {
        // NO-OP if buffer is already in correct state
        if (this.writeBufferConfiguredForWrite != writeBufferConfiguredForWrite) {
            if (writeBufferConfiguredForWrite) {
                // Switching to write
                int remaining = writeBuffer.remaining(); // bytes remaing to be read
                if (remaining == 0) {
                    writeBuffer.clear();
                } else {
                    writeBuffer.compact(); // move all unread data to front and palce pos at end of unread data , limit
                                           // = cap;
                    writeBuffer.position(remaining);
                    writeBuffer.limit(writeBuffer.capacity());
                }
            } else {
                // Switching to read
                writeBuffer.flip();
            }
            this.writeBufferConfiguredForWrite = writeBufferConfiguredForWrite;
        }
    }

    public boolean isWriteBufferWritable() {
        if (writeBufferConfiguredForWrite) {
            return writeBuffer.hasRemaining(); // check if there is space for write (limit)
        } else {
            return writeBuffer.remaining() == 0; // only write if no data left for read
        }
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public boolean isWriteBufferEmpty() {
        if (writeBufferConfiguredForWrite) {
            return writeBuffer.position() == 0;
        } else {
            return writeBuffer.remaining() == 0;
        }
    }


      public void reset() {
        readBuffer.clear();
        readBufferConfiguredForWrite = true;
        writeBuffer.clear();
        writeBufferConfiguredForWrite = true;
    }


    public void expand(int newSize) {
        configureReadBufferForWrite();
        readBuffer = ByteBufferUtility.expand(readBuffer, newSize);
        configureWriteBufferForWrite();
        writeBuffer = ByteBufferUtility.expand(writeBuffer, newSize);
    }

    public void unReadReadBuffer(ByteBuffer returnedData) {
        if (isReadBufferEmpty()) {
            configureReadBufferForWrite();
            readBuffer.put(returnedData);
        } else {
            int bytesReturned = returnedData.remaining();

            if (readBufferConfiguredForWrite) {

                if (readBuffer.position() + bytesReturned > readBuffer.capacity()) {
                    throw new BufferOverflowException();
                } else {
                    // moving bytes up in reverse order to protect overflow
                    for (int i = readBuffer.position() - 1; i >= 0; i--) {
                        readBuffer.put(i + bytesReturned, readBuffer.get(i));
                    }

                    // Insert bytes in front

                    for (int i = 0; i < bytesReturned; i++) {
                        readBuffer.put(i, returnedData.get());
                    }

                    // Update the position
                    readBuffer.position(readBuffer.position() + bytesReturned);
                }
            } else { // In read Mode

                // we need to shift bytesToShift by bytesReturned
                int oldLimit = readBuffer.limit();
                int oldPosition = readBuffer.position();
                if (readBuffer.capacity() - readBuffer.limit() < bytesReturned) {
                    throw new BufferOverflowException();
                }

                readBuffer.limit(oldLimit + bytesReturned);

                for (int i = oldLimit - 1; i >= readBuffer.position(); i--) {
                    readBuffer.put(bytesReturned + i, readBuffer.get(i)); // make bytesReturned spaces
                }

                // Insert returnedData
                for (int i = oldPosition; i < oldPosition + bytesReturned; i++) {
                    readBuffer.put(i, returnedData.get());
                }

                readBuffer.position(oldPosition);
            }
        }
    }


    public void free() {
        if (direct) {
            ByteBufferUtility.clearDirectBuffer(readBuffer);
            ByteBufferUtility.clearDirectBuffer(writeBuffer);
        }
    }
}
