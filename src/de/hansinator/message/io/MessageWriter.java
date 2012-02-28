package de.hansinator.message.io;

import java.io.IOException;
import java.io.OutputStream;
import de.hansinator.message.MessageObject;

public class MessageWriter<T extends MessageObject> implements MessageOutputStream<T> {

    final OutputStream sink;

    public MessageWriter(OutputStream out) {
        sink = out;
    }

    public void write(T message) throws IOException {
        sink.write(message.encode());
    }
    
}
