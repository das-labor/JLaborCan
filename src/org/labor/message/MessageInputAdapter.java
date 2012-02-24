package org.labor.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MessageInputAdapter<I extends MessageObject, O extends MessageObject> implements MessageInputStream<O> {

    final MessageFactory<O> factory;
    final MessageInputStream source;

    public MessageInputAdapter(MessageFactory<O> mf, MessageInputStream<I> in) {
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
