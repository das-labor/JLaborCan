package org.labor.message;

import java.io.IOException;
import java.io.InputStream;

public interface MessageFactory<T extends MessageObject> {

    public T assemble(InputStream in) throws IOException;
}
