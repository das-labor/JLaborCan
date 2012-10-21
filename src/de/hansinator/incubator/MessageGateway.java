package de.hansinator.incubator;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;

/**
 *
 * @author hansinator
 */
public class MessageGateway<T extends MessageObject> {
    final MessageInput<T> in;
    final MessageOutput<T> out;

    public MessageGateway(MessageInput<T> in, MessageOutput<T> out) {
        this.in = in;
        this.out = out;
    }
}
