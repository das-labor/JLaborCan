package de.hansinator.message.lmp;

import de.hansinator.message.MessageObject;

/**
 * An abstract Labor Message Protocol message. This type of message is used for point to point
 * connections to labor devices via various channels such as uart, usb or a tcp tunnel.
 * 
 * @author hansinator
 */
public abstract class LMPMessage extends MessageObject {

	/** 
	 * the maximum payload length
	 */
	public static final int DATA_MAX_LENGTH = 20;

	/**
	 * the header length
	 */
	protected final static int HEADER_LEN = 2;

	/**
	 * command byte
	 */
	protected final byte cmd;

	
	/**
	 * Construct a logical LMP message
	 * 
	 * @param cmd
	 * @param data
	 */
	protected LMPMessage(byte cmd, byte data[]) {
		super(data);
		this.cmd = cmd;
	}

	/**
	 * Get the command byte.
	 * 
	 * @return command byte
	 */
	public byte getCommand() {
		return cmd;
	}
}
