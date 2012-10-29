package de.hansinator.message.bus;

import de.hansinator.message.MessageObject;

/**
 * A basic message bus node that stores a bus reference and registers itself with the bus.
 * 
 * @author hansinator
 * 
 * @param <T>
 *            The message type
 */
public abstract class BaseNode<T extends MessageObject> implements MessageNode<T> {
	protected final MessageBus<T> bus;

	/**
	 * Construct a simple node on a bus.
	 * 
	 * @param bus
	 *            message bus
	 */
	public BaseNode(MessageBus<T> bus) {
		this.bus = bus;
		bus.addMessageNode(this);
	}

	@Override
	public MessageBus<T> getBus() {
		return bus;
	}

	/**
	 * Send a message to the bus.
	 * 
	 * @param message message object
	 */
	protected void sendMessage(T message) {
		bus.sendMessage(this, message);
	}
}
