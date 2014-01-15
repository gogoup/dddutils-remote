package org.gogoup.dddutils.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Reference:
 *      http://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream/6603018#6603018
 * 
 * @author sunr
 *
 */
public class ByteBufferOutputStream extends OutputStream {
    
    private ByteBuffer buf;
    
    public ByteBufferOutputStream(ByteBuffer buf) {
        this.buf = buf;
    }
    
    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        if (null == bytes) {
            throw new NullPointerException();
        }
        buf.put(bytes, off, len);
    }
    
}
