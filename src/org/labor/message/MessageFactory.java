package org.labor.message;

import java.io.InputStream;

public interface MessageFactory<T extends Message> {

    public T assemble(InputStream in);
}
