package de.hansinator.message.bus;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.net.MessageEndpoint;

/*
 * todo: replace boolean hell by state logic
 */
public abstract class MessageDispatcher<T extends MessageObject> {

	private Thread iworker = null, oworker = null;

	private volatile boolean irunning = false, orunning = false;

	private volatile boolean starting = false, autoRestart;

	private final MessageEndpoint<T> endpoint;

	private final BlockingQueue<T> messageQueue;

	private volatile int lastTimeout = 0;

	/**
	 * Construct an unconnected asynchronous endpoint with a default maximum send queue size of 64.
	 * 
	 * @param endpoint
	 *            The underlying endpoint
	 * @param autoConnect
	 *            If true, this endpoint will automatically connect when a message input is read or
	 *            output is written. This will also restore a broken connection, except when there
	 *            is an error during connection setup.
	 */
	public MessageDispatcher(MessageEndpoint<T> endpoint, boolean autoConnect) {
		this(endpoint, autoConnect, 64);
	}

	/**
	 * Construct an unconnected asynchronous endpoint with a maximum send queue size.
	 * 
	 * @param endpoint
	 *            The underlying endpoint
	 * @param queueSize
	 *            The maximum send queue size
	 * @param autoConnect
	 *            If true, this endpoint will automatically connect when a message input is read or
	 *            output is written. This will also restore a broken connection, except when there
	 *            is an error during connection setup.
	 */
	public MessageDispatcher(MessageEndpoint<T> endpoint, boolean autoConnect, int queueSize) {
		if (endpoint == null)
			throw new NullPointerException("endpoint is null");

		this.endpoint = endpoint;
		this.messageQueue = new LinkedBlockingQueue<T>(queueSize);
		this.autoRestart = autoConnect;
	}

	protected abstract void handleMessage(final T msg);

	/**
	 * Query the auto connection feature status
	 * 
	 * @see #connect(int)
	 * @return true is auto connect is enabled
	 */
	public boolean isAutoConnect() {
		return autoRestart;
	}

	/**
	 * @see #connect(int)
	 * @see #isAutoConnect()
	 * @param autoConnect
	 *            enable or disable auto connection status
	 */
	public void setAutoConnect(boolean autoConnect) {
		this.autoRestart = autoConnect;
	}

	/**
	 * Indicates if the worker thread starting and is in it's connection phase.
	 * 
	 * @see #connect(int)
	 * @return true if connection is being set-up
	 */
	public boolean isConnecting() {
		return starting;
	}

	/**
	 * See if this endpoint is connected and ready for I/O operations.
	 * 
	 * @see #connect(int)
	 * @return conenction state
	 */
	public boolean isConnected() {
		return orunning && endpoint.isConnected();
	}

	public synchronized boolean start(final int timeout) {
		// start output worker
		if (!orunning && !starting && (oworker == null || !oworker.isAlive())) {
			oworker = new Thread(new OutputWorker(), "MessageDispatcherOutputWorker");
			starting = true;
			lastTimeout = timeout;
			oworker.start();
		} else
			return false;

		// start input worker
		if (!irunning && (iworker == null || !iworker.isAlive())) {
			iworker = new Thread(new InputWorker(), "MessageDispatcherInputWorker");
			irunning = true;
			iworker.start();
		} else
			return false;

		return true;
	}

	public synchronized void stop() {
		orunning = false;
		irunning = false;
		if (oworker != null && oworker.isAlive())
			oworker.interrupt();
		if (iworker != null && iworker.isAlive())
			iworker.interrupt();
	}

	synchronized private boolean up() {
		// if not running, try auto restarting the worker
		if (!orunning && autoRestart && !starting) {
			// wake up neo
			if (oworker != null && oworker.isAlive())
				oworker.interrupt();

			// wait for thread to shut down
			while (oworker.isAlive())
				Thread.yield();

			// restart & wait
			start(lastTimeout);
			while (!orunning && autoRestart)
				Thread.yield();
		} else if (starting)
			while (starting)
				Thread.yield();

		return orunning;
	}

	protected boolean enqueueMessageWrite(T msg) throws InterruptedException {
		return up() && orunning && messageQueue.offer(msg);
	}

	private class InputWorker implements Runnable {
		@Override
		public void run() {
			MessageInput<T> in;

			// wait for output worker to setup connection
			while (starting)
				Thread.yield();
			if (!isConnected()) {
				irunning = false;
				return;
			}

			try {
				// fetch input
				in = endpoint.getMessageInput();

				// handle messages
				while (irunning && orunning) {
					handleMessage(in.read());
				}
			} catch (IOException e) {
				irunning = false;
				orunning = false;
				if (oworker != null && oworker.isAlive())
					oworker.interrupt();
			}
		}
	}

	private class OutputWorker implements Runnable {

		@Override
		public void run() {
			// setup connection
			MessageOutput<T> out = null;
			if (!endpoint.isConnected())
				try {
					endpoint.connect(lastTimeout);

					// fetch output
					out = endpoint.getMessageOutput();

					// set to running
					orunning = true;
				} catch (IOException e) {
					autoRestart = false;
					starting = false;
					return;
				} finally {
					starting = false;
				}

			// enter main loop
			try {
				while (orunning || !messageQueue.isEmpty()) {
					// write out messages
					out.write(messageQueue.take());
					if (Thread.interrupted())
						orunning = false;
				}
			} catch (IOException e) {
				if (e instanceof SocketException) {
					// if this happens, we may try a reconnect and re-enqueue the message,
					// so that it isn't lost
					// Log.d(LNET, "Socket error broke worker main loop", e);
				}
				// Log.d(LNET, "IO error broke worker main loop", e);
			} catch (InterruptedException e) {
			} finally {
				orunning = false;

				// disconnect
				if (endpoint.isConnected()) {
					try {
						endpoint.close();
					} catch (IOException e) {
						// connection shutdown failed
					}
				}
			}
		}
	}
}