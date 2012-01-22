package org.labor.message;

import java.io.IOException;

public interface MessageInputStream<T extends MessageObject> {

    public T read() throws IOException;
}
