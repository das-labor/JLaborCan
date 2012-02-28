package de.hansinator.message.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import de.hansinator.message.MessageObject;

public class MessageInputAdapter<I extends MessageObject, O extends MessageObject> implements MessageInput<O> {

    final MessageFactory<O> factory;
    final MessageInput source;

    public MessageInputAdapter(MessageFactory<O> mf, MessageInput<I> in) {
        factory = mf;
        source = in;
    }

    public O read() throws IOException {
        byte[] data;
        do {
            data = source.read().getPayload();
        } while (data == null);
        return factory.assemble(new ByteArrayInputStream(data));
    }
}
