package de.hansinator.message.io;

import java.io.IOException;
import de.hansinator.message.MessageObject;

public interface MessageOutputStream<T extends MessageObject> {

    public void write(T message) throws IOException;
}
