package de.hansinator.message;

/**
 * The base class that represents single messages with a payload.
 * 
 * @author hansinator
 * 
 */
public abstract class MessageObject {

	protected final byte[] data;

	/**
	 * Construct a new message with the given payload.
	 * 
	 * @param payload
	 *            payload
	 */
	public MessageObject(byte payload[]) {
		this.data = payload;
	}

	/**
	 * Return the payload length.
	 * 
	 * @return payload length
	 */
	public int getLength() {
		return data != null ? data.length : 0;
	}

	/**
	 * Return the message payload.
	 * 
	 * @return payload
	 */
	public byte[] getPayload() {
		return data;
	}

	/**
	 * Encode a message to its binary representation.
	 * 
	 * @return binary message
	 */
	public abstract byte[] encode();
}
