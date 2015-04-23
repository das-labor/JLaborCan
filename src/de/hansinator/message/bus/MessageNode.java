package de.hansinator.message.bus;

import de.hansinator.message.MessageObject;

/**
 * The minimal interface for message bus nodes.
 * 
 * @author hansinator
 * 
 * @param <T>
 *            message type
 */
public interface MessageNode<T extends MessageObject> {

	/**
	 * Called by the bus to deliver a message to this node.
	 * 
	 * @param message
	 *            message object
	 * @return true if message was processed
	 */
	boolean onMessageReceived(T message);

	/**
	 * Return the nodes bus object. Implementations within this library never return null.
	 * 
	 * @return message bus
	 */
	MessageBus<T> getBus();
}
