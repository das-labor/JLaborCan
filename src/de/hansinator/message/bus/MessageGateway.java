package de.hansinator.message.bus;

import java.io.IOException;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.net.MessageEndpoint;

/**
 * 
 * @author hansinator
 */
public abstract class MessageGateway<BUS extends MessageObject, EP extends MessageObject> implements MessageNode<BUS> {

	private static Object lock = new Object();

	private final MessageBus<BUS> bus;

	private final MessageEndpoint<EP> endpoint;

	private MessageInput<BUS> in = null;

	private MessageOutput<BUS> out = null;

	private MessageDispatcher<BUS> dispatcher;

	public MessageGateway(MessageBus<BUS> bus, MessageEndpoint<EP> endpoint) {
		this.endpoint = endpoint;
		this.bus = bus;
		bus.addMessageNode(this);
	}

	protected MessageEndpoint<EP> getEndpoint() {
		return endpoint;
	}

	protected abstract MessageOutput<BUS> buildOutputChain(final MessageOutput<EP> out);

	protected abstract MessageInput<BUS> buildInputChain(final MessageInput<EP> in);

	public synchronized boolean connect(int timeout) {
		try {
			endpoint.connect(timeout);
			final MessageInput<EP> i = endpoint.getMessageInput();
			final MessageOutput<EP> o = endpoint.getMessageOutput();
			synchronized (lock) {
				out = buildOutputChain(o);
				in = buildInputChain(i);
			}

			this.dispatcher = new MessageDispatcher<BUS>(in) {

				@Override
				protected void handleMessage(BUS msg) {
					bus.sendMessage(MessageGateway.this, msg);
				}
			};
			return dispatcher.start();
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized void disconnect() throws IOException {
		dispatcher.stop();
		synchronized (lock) {
			out = null;
			in = null;
		}
		endpoint.close();
	}

	public boolean onMessageReceived(BUS msg) {
		MessageOutput<BUS> out;
		synchronized (lock) {
			if (this.out == null)
				return false;
			out = this.out;
		}
		try {
			out.write(msg);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
