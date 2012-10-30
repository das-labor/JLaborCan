package de.hansinator.message.io;

import java.io.IOException;
import java.io.InputStream;
import de.hansinator.message.MessageObject;

/**
 * A message factory handles assembly of a message from a byte oriented input stream.
 * 
 * @author hansinator
 * 
 * @param <T>
 *            message type
 */
public interface MessageFactory<T extends MessageObject> {

	/**
	 * Assemble a message from the byte oriented input stream.
	 * 
	 * @param in input stream
	 * @return assembled message
	 * @throws IOException thrown on stream or protocol errors
	 */
	public T assemble(InputStream in) throws IOException;
}
