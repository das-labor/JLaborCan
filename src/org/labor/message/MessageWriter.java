package org.labor.message;

import java.io.IOException;
import java.io.OutputStream;

public class MessageWriter<T extends Message> implements MessageOutputStream<T> {

    OutputStream sink;

    public MessageWriter(OutputStream out) {
        sink = out;
    }

    public void write(T message) throws IOException {
        sink.write(message.encode());
    }
    
}
