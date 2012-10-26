package de.hansinator.incubator;

import java.io.IOException;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageInputAdapter;
import de.hansinator.message.io.MessageInputFilter;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.net.MessageEndpoint;
import de.hansinator.message.protocol.CANTCPMessage;
import de.hansinator.message.protocol.LAPMessage;

public class LAPTCPCanGateway extends MessageGateway<LAPMessage, CANTCPMessage> {
	public LAPTCPCanGateway(MessageBus<LAPMessage> bus, MessageEndpoint<CANTCPMessage> endpoint) {
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
}
