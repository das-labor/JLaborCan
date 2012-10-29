package de.hansinator.automation.lap;

import java.io.IOException;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.bus.MessageGateway;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageInputAdapter;
import de.hansinator.message.io.MessageInputFilter;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.net.AsyncWriteMessageProxy;
import de.hansinator.message.net.MessageEndpoint;
import de.hansinator.message.net.TCPMessageEndpoint;
import de.hansinator.message.protocol.CANTCPMessage;
import de.hansinator.message.protocol.LAPMessage;

public class LAPTCPCanGateway extends MessageGateway<LAPMessage, CANTCPMessage> {
	private LAPTCPCanGateway(MessageBus<LAPMessage> bus, MessageEndpoint<CANTCPMessage> endpoint) {
		super(bus, endpoint);
	}

	@Override
	protected MessageInput<LAPMessage> buildInputChain(final MessageInput<CANTCPMessage> in) {
		return new MessageInputAdapter<CANTCPMessage, LAPMessage>(LAPMessage.factory, new MessageInputFilter<CANTCPMessage>(in) {

			@Override
			public boolean accept(CANTCPMessage message) {

				return message.getCommand() == 0x11;
			}

		});
	}

	@Override
	protected MessageOutput<LAPMessage> buildOutputChain(final MessageOutput<CANTCPMessage> out) {
		return new MessageOutput<LAPMessage>() {

			public void write(LAPMessage message) throws IOException {
				out.write(new CANTCPMessage((byte) 0x11, message.encode()));
			}
		};
	}

	public boolean up(int timeout, boolean autoReconnect) {
		AsyncWriteMessageProxy<CANTCPMessage> ep = (AsyncWriteMessageProxy<CANTCPMessage>)getEndpoint();
		ep.setAutoConnect(autoReconnect);
		if (connect(timeout)) {
			// wait for connection or die
			long end = System.currentTimeMillis() + timeout;
			while (!ep.isConnected() && ep.isConnecting() && (end > System.currentTimeMillis()))
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			return ep.isConnected();
		}
		else return false;
	}
	
	public static LAPTCPCanGateway makeGateway(MessageBus<LAPMessage> bus, String host, int port, boolean autoConnect) {
		// setup lap connection
		MessageEndpoint<CANTCPMessage> endpoint = new AsyncWriteMessageProxy<CANTCPMessage>(
				new TCPMessageEndpoint<CANTCPMessage>("10.0.1.2", 2342, CANTCPMessage.factory), autoConnect);

		// create gateway
		return new LAPTCPCanGateway(bus, endpoint);
	}
}
