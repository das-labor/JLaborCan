package de.hansinator.automation.lap;

import java.io.IOException;

import de.hansinator.message.bus.GatewayNode;
import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageInputAdapter;
import de.hansinator.message.io.MessageInputFilter;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.net.MessageEndpoint;
import de.hansinator.message.net.TCPMessageEndpoint;
import de.hansinator.message.protocol.CANTCPMessage;
import de.hansinator.message.protocol.LAPMessage;

public class LAPTCPCanGateway extends GatewayNode<LAPMessage> {

	private LAPTCPCanGateway(MessageBus<LAPMessage> bus, MessageEndpoint<LAPMessage> endpoint, boolean autoConnect) {
		super(bus, endpoint, autoConnect);
	}

	private static MessageInput<LAPMessage> buildInputChain(final MessageInput<CANTCPMessage> in) {
		return new MessageInputAdapter<CANTCPMessage, LAPMessage>(LAPMessage.factory,
				new MessageInputFilter<CANTCPMessage>(in) {

					@Override
					public boolean accept(CANTCPMessage message) {

						return message.getCommand() == 0x11;
					}

				});
	}

	private static MessageOutput<LAPMessage> buildOutputChain(final MessageOutput<CANTCPMessage> out) {
		return new MessageOutput<LAPMessage>() {

			public void write(LAPMessage message) throws IOException {
				out.write(new CANTCPMessage((byte) 0x11, message.encode()));
			}
		};
	}

	public static LAPTCPCanGateway makeGateway(MessageBus<LAPMessage> bus, String host, int port, boolean autoConnect) {
		// setup lap connection
		final MessageEndpoint<CANTCPMessage> endpoint = new TCPMessageEndpoint<CANTCPMessage>("10.0.1.2", 2342,
				CANTCPMessage.factory);

		// build laptcp converter
		final MessageEndpoint<LAPMessage> converter = new MessageEndpoint<LAPMessage>() {

			@Override
			public void connect(int timeout) throws IOException {
				endpoint.connect(timeout);
			}

			@Override
			public void connect() throws IOException {
				endpoint.connect();
			}

			@Override
			public void close() throws IOException {
				endpoint.close();
			}

			@Override
			public boolean isConnected() {
				return endpoint.isConnected();
			}

			@Override
			public MessageInput<LAPMessage> getMessageInput() throws IOException {
				return buildInputChain(endpoint.getMessageInput());
			}

			@Override
			public MessageOutput<LAPMessage> getMessageOutput() throws IOException {
				return buildOutputChain(endpoint.getMessageOutput());
			}

		};

		// create gateway
		return new LAPTCPCanGateway(bus, converter, autoConnect);
	}
}
