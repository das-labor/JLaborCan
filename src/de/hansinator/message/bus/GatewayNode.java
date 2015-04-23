package de.hansinator.message.bus;

import de.hansinator.message.MessageObject;
import de.hansinator.message.net.MessageEndpoint;

/**
 * 
 * @author hansinator
 */
public abstract class GatewayNode<T extends MessageObject> extends MessageDispatcher<T> implements MessageNode<T> {

	private final MessageBus<T> bus;

	public GatewayNode(MessageBus<T> bus, final MessageEndpoint<T> endpoint, boolean autoConnect) {
		super(endpoint, autoConnect);
		this.bus = bus;
		bus.addMessageNode(this);
	}

	public synchronized boolean blockingStart(int timeout) {
		if (start(timeout)) {
			// wait for connection or die
			long end = System.currentTimeMillis() + timeout;
			while (!isConnected() && isConnecting() && (end > System.currentTimeMillis()))
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			return isConnected();
		} else
			return false;
	}

	@Override
	protected void handleMessage(T msg) {
		bus.sendMessage(GatewayNode.this, msg);
	}

	@Override
	public boolean onMessageReceived(T msg) {
		try {
			return enqueueMessageWrite(msg);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public MessageBus<T> getBus() {
		return bus;
	}
}
