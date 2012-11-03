package de.hansinator.message.net;

import java.io.IOException;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;

public interface MessageEndpoint<T extends MessageObject> {
	
	public void connect(int timeout) throws IOException;
	
	public void connect() throws IOException;
	
	public void close() throws IOException;
	
	public boolean isConnected();

	public MessageInput<T> getMessageInput() throws IOException;
	
	public MessageOutput<T> getMessageOutput() throws IOException;
	
}
