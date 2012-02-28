package de.hansinator.message.io;

import java.io.IOException;
import java.io.InputStream;
import de.hansinator.message.MessageObject;

public interface MessageFactory<T extends MessageObject> {

    public T assemble(InputStream in) throws IOException;
}
