package de.hansinator.incubator;

import java.io.IOException;

import de.hansinator.message.MessageObject;
import de.hansinator.message.bus.MessageDispatcher;
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

	@SuppressWarnings("unused")
	private MessageDispatcher<BUS> dispatcher;

	public MessageGateway(MessageBus<BUS> bus, MessageEndpoint<EP> endpoint) {
		this.endpoint = endpoint;
		this.bus = bus;
	}

	protected abstract MessageOutput<BUS> buildOutputChain(final MessageOutput<EP> out);

	protected abstract MessageInput<BUS> buildInputChain(final MessageInput<EP> in);

	public synchronized boolean startEndpoint() {
		try {
			endpoint.connect(9669);
			synchronized (lock) {
				final MessageInput<EP> i = endpoint.getMessageInput();
				final MessageOutput<EP> o = endpoint.getMessageOutput();
				out = buildOutputChain(o);
				in = buildInputChain(i);
			}

			this.dispatcher = new MessageDispatcher<BUS>(in) {

				@Override
				protected void handleMessage(BUS msg) {
					bus.sendMessage(msg);
				}
			};
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized void stopEndpoint() throws IOException {
		endpoint.close();
	}

	public boolean onMessageReceived(BUS msg) {
		MessageOutput<BUS> out;
		// Log.v(LNET, "Sending message");
		synchronized (lock) {
			if (this.out == null) {
				// Log.d(LNET, "Send failed, got no connection");
				return false;
			}
			out = this.out;
		}
		try {
			out.write(msg);
			return true;
		} catch (IOException e) {
			// Log.d(LNET, "Failed to deliver message", e);
			return false;
		}
	}
}
