package de.hansinator.message.io;

import java.io.IOException;
import java.io.OutputStream;
import de.hansinator.message.MessageObject;

public class MessageStreamWriter<T extends MessageObject> implements MessageOutput<T> {

    final OutputStream sink;

    public MessageStreamWriter(OutputStream out) {
        sink = out;
    }

    public void write(T message) throws IOException {
        sink.write(message.encode());
    }
    
}
