package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;

import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.ErrorMessage;



/**
 * A class to represent a response packet. A response packet prefaces all data
 * sent from the data manager, even the DM is not sending any data (ie shutting
 * down now).
 * 
 * @author patrickm
 * 
 */
public class DMResponse extends DMPacket {

	// packet size
	public static final int packetsize = 24; // bytes

	// data sources
	public static final int STDOUT = 1;
	public static final int STDERR = 2;
	public static final int SUBSCRIPTION_MGR = 3;
	public static final int DATA_MGR = 4;
	public static final int MAILBOX = 5;

	// response specific
	private int returnCode;
	private int source;
	private boolean valid;

	// for adding to ClientSession's buffer
	private byte[] bacopy;
	
	/**
	 * Construct a response object from a stream of bytes. If there are any
	 * errors, the valid flag is set to false.
	 * 
	 * @param ba
	 *            The array of bytes from the data manager, of length
	 *            packetsize.
	 */
	public DMResponse(final byte[] ba) {

		if (ba.length != packetsize) {
			// usually happens when reading garbage data
			// ignore for now, but can be useful
//			System.err.println("Invalid packet length: " + ba.length);
			valid = false;
			return;
		}

		bacopy = ba;

		final ByteArrayInputStream bais = new ByteArrayInputStream(ba);
		final DataInputStream dis = new DataInputStream(bais);
		int headerID = 0;

		try {
			headerID = dis.readInt();
			source = dis.readInt();
			scriptID = dis.readInt();
			clientID = dis.readInt();
			returnCode = dis.readInt();
			size = dis.readInt();
			// void ptr?
		} catch (final EOFException eofe) {
			valid = false;
			new ErrorMessage("Response packet error!", "See stderr for more details").open();
			System.err.println("response packet Error: " + eofe.getMessage());
		} catch (final IOException ioe) {
			valid = false;
			new ErrorMessage("Response packet error!", "See stderr for more details").open();
			System.err.println("response packet Error: " + ioe.getMessage());
		}

		valid = (headerID == BEGINSTR);
		return;

	}

	/**
	 * The "source" of this packet, unused right now.
	 * 
	 * @return An integer corresponding to the data manager, the subscription
	 *         manager, or the mailbox, etc.
	 */
	public int getsource() {
		return source;
	}

	/**
	 * The return code. This does not apply to all responses.
	 * 
	 * @return An integer: zero on success, non zero otherwise.
	 */
	public int getreturnCode() {
		return returnCode;
	}

	/**
	 * A packet is marked invalid if the size is wrong, there was any errors
	 * converting the stream of bytes to individual integers, or the header's
	 * magic number is wrong.
	 * 
	 * @return True if the packet appears to be sane, false otherwise.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * For debugging, dump the packet contents to a formatted string
	 */
	@Override
	public String toString() {
		return "ID String:\t" + BEGINSTR + "\nSource:\t" + source
				+ "\nScript #:\t" + scriptID + "\nClient ID:\t" + clientID
				+ "\nData Size:\t" + size + "\nReturn Code:\t" + returnCode
				+ "\n";
	}

	/**
	 * Hackish function to retrieve the original byte array. <ay be removed in
	 * the future.
	 * 
	 * @return The original array of bytes this packet was constructed from.
	 */
	public byte[] tobytes() {
		return bacopy;
	}
}
