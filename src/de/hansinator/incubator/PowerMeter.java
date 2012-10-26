package de.hansinator.incubator;

import java.util.EventListener;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class PowerMeter extends LAPDevice {

	private final Object lock = new Object();

	private double watts = 0.0;

	private final boolean[] prec = new boolean[] { false, false, false, false };

	private long power = 0;

	private volatile PowerUpdateListener listener;

	public interface PowerUpdateListener extends EventListener {
		void onUpdate(double watts);
	}

	public PowerMeter(MessageBus<LAPMessage> bus) {
		this(bus, 0x05);
	}

	public PowerMeter(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(deviceAddress, 0x3c, 0x00, 0x3c, bus);
	}

	public void setListener(PowerUpdateListener listener) {
		synchronized (lock) {
			this.listener = listener;
		}
	}

	@Override
	public boolean onMessageReceived(LAPMessage msg) {
		final byte[] pl = msg.getPayload();
		if ((msg.getSrcAddr() == srcAddr) && (msg.getSrcPort() == srcPort)) {
			switch (pl[0]) {
			case 1:
				prec[0] = true;
				power += (((long) pl[4] & 0xFF) << 24) | (((long) pl[3] & 0xFF) << 16) | (((long) pl[2] & 0xFF) << 8)
						| ((long) pl[1] & 0xFF);
				break;
			case 2:
				prec[1] = true;
				power += (((long) pl[4] & 0xFF) << 24) | (((long) pl[3] & 0xFF) << 16) | (((long) pl[2] & 0xFF) << 8)
						| ((long) pl[1] & 0xFF);
				break;
			case 3:
				prec[2] = true;
				power += (((long) pl[4] & 0xFF) << 24) | (((long) pl[3] & 0xFF) << 16) | (((long) pl[2] & 0xFF) << 8)
						| ((long) pl[1] & 0xFF);
				break;
			default:
			}
			if (prec[0] && prec[1] && prec[2]) {
				watts = power * 0.00256299972534;
				power = 0;
				prec[0] = prec[1] = prec[2] = false;
				synchronized (lock) {
					if (listener != null)
						listener.onUpdate(watts);
				}
			}
		}
		return true;
	}
}
