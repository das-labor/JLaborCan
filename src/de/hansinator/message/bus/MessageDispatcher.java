package de.hansinator.message.bus;

import java.io.IOException;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;

public abstract class MessageDispatcher<T extends MessageObject> {
	
	private Thread worker = null;

	private volatile boolean running;
	
	private final MessageInput<T> in;

	public MessageDispatcher(MessageInput<T> in) {
		this.in = in;
	}
	
	public synchronized boolean start() {
		if (!running && (worker == null || !worker.isAlive())) {
			worker = new Thread(new Runnable() {
				@Override
				public void run() {
					while (running) {
						try {
							handleMessage(in.read());
						} catch (IOException e) {
							running = false;
							return;
						}
					}
				}
			}, "MessageDispatcherWorker");
			running = true;
			worker.start();
		} else
			return false;

		return true;
	}
	
	public synchronized void stop() {
		running = false;
		if (worker != null && worker.isAlive())
			worker.interrupt();
	}

	protected abstract void handleMessage(final T msg);
}