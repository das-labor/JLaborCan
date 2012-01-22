package org.labor.message;

import java.io.IOException;
import java.io.InputStream;

public final class MessageReader<T extends MessageObject> implements MessageInputStream<T> {

    final MessageFactory<T> factory;
    final InputStream source;

    public MessageReader(MessageFactory<T> mf, InputStream in) {
        factory = mf;
        source = in;
    }

    public T read() throws IOException {
        return factory.assemble(source);
    }
}
