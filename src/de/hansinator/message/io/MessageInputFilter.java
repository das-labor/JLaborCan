package de.hansinator.message.io;

import java.io.IOException;

import de.hansinator.message.MessageObject;

public abstract class MessageInputFilter<I extends MessageObject> implements MessageInput<I> {

	final MessageInput<I> source;

	public MessageInputFilter(MessageInput<I> in) {
		source = in;
	}

	public abstract boolean accept(I message);

	public I read() throws IOException {
		I msg = null;
		do {
			msg = source.read();
		} while (!accept(msg));
		
		return msg;
	}
}
