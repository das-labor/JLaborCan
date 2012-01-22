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
        return factory.assemble(new ByteArrayInputStream(source.read().getPayload()));
    }
    
    
}
