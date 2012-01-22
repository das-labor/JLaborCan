package org.labor.message;

import java.io.InputStream;

public final class MessageReader<T extends Message> implements MessageInputStream<T> {

    MessageFactory<T> factory;
    InputStream source;

    public MessageReader(MessageFactory<T> mf, InputStream in) {
        factory = mf;
        source = in;
    }

    public T read() {
        return factory.assemble(source);
    }
}
