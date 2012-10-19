package de.hansinator.message.net;

import java.io.IOException;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;

public class LoopMessageEndpoint<T extends MessageObject> implements MessageEndpoint<T> {

	private final MessageObject[] buffer;

	private int index = 0;

	private final MessageInput<T> input = new MessageInput<T>() {
		@SuppressWarnings("unchecked")
		@Override
		public T read() throws IOException {
			synchronized (buffer) {
				if (index == 0) {
					try {
						buffer.wait();
					} catch (InterruptedException e) {
						throw new IOException("Read interrupted", e);
					}
				}
				return (T) buffer[--index];
			}
		}
	};

	private final MessageOutput<T> output = new MessageOutput<T>() {
		public void write(T message) throws IOException {
			synchronized (buffer) {
				if (index == buffer.length) {
					try {
						buffer.wait();
					} catch (InterruptedException e) {
						throw new IOException("Write interrupted", e);
					}
				}
				buffer[index++] = message;
				buffer.notify();
			}
		}
	};

	public LoopMessageEndpoint(int bufSize) {
		buffer = new MessageObject[bufSize];
	}

	@Override
	public void connect(int timeout) throws IOException {
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public MessageInput<T> getMessageInput() {
		return input;
	}

	@Override
	public MessageOutput<T> getMessageOutput() {
		return output;
	}
}
