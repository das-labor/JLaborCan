package de.hansinator.message.io;

import java.io.IOException;
import de.hansinator.message.MessageObject;

public interface MessageOutput<T extends MessageObject> {

    public void write(T message) throws IOException;
}
