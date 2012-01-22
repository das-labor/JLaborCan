package org.labor.message;

import java.io.IOException;

public interface MessageOutputStream<T extends Message> {

    public void write(T message) throws IOException;
}
