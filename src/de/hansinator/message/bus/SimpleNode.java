package de.hansinator.message.bus;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import de.hansinator.message.MessageObject;

/**
 * A universal node to send messages or monitor incoming messages. Use this for basic bus
 * manipulation.
 * 
 * @author hansinator
 * 
 * @param <T>
 *            message type
 */
public class SimpleNode<T extends MessageObject> extends BaseNode<T> {

	private final List<MessageListener<T>> listeners = new ArrayList<SimpleNode.MessageListener<T>>();

	public interface MessageListener<T extends MessageObject> extends EventListener {
		public void onMessage(T message);
	}

	/**
	 * Construct a simple node on a bus.
	 * 
	 * @param bus message bus
	 */
	public SimpleNode(MessageBus<T> bus) {
		super(bus);
	}

	/**
	 * Add a message listener.
	 * 
	 * @param listener the listener
	 * @return true if added
	 */
	public synchronized boolean addMessageListener(MessageListener<T> listener) {
		if (listeners.contains(listener))
			return true;
		return listeners.add(listener);
	}

	/**
	 * Remove a message listener.
	 * 
	 * @param listener the listener
	 * @return true if removed
	 */
	public synchronized boolean removeMessageListener(MessageListener<T> listener) {
		return listeners.remove(listener);
	}

	@Override
	public synchronized boolean onMessageReceived(T message) {
		for (MessageListener<T> listener : listeners)
			listener.onMessage(message);
		return true;
	}

	/**
	 * Send a message to the bus.
	 * 
	 * @param message
	 *            message object
	 */
	@Override
	public void sendMessage(T message) {
		super.sendMessage(message);
	}
}
