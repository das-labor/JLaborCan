package de.hansinator.message.io;

import java.io.IOException;
import de.hansinator.message.MessageObject;

public interface MessageInput<T extends MessageObject> {

    public T read() throws IOException;
}
