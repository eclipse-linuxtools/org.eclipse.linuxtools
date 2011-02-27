package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

/**
 * A class to represent a data packet. This abstract class just has all of the
 * common fields and associated get-methods.
 * 
 * @author patrickm
 * 
 */
public abstract class DMPacket {

	// TODO?: replace with java enums

	// header ID
	protected final int BEGINSTR = 0xa1b2c3d4;
	public int packetsize;

	// common packet fields
	protected int clientID;
	protected int size;
	protected int scriptID;
	protected String filename;

	/**
	 * Return the client ID number from this packet. This needs to be included
	 * in all outgoing packets (so the DM can tell who is sending what).
	 * 
	 * @return The clientID of this packet.
	 */
	public int getclientID() {
		return clientID;
	}
	/**
	 * Return the filename from this packet. This needs to be included
	 * in all outgoing packets (so the DM can tell who is sending what).
	 * 
	 * @return The filename.
	 */
	
	public String getfilename() {
		return filename;
	}

	/**
	 * Return the script number of this packet. Consult the data manager package
	 * for what these map to.
	 * 
	 * @return The script number of this packet.
	 */
	public int getscriptID() {
		return scriptID;
	}

	/**
	 * Return the size field of this packet. This is not the size of the packet
	 * but the size of the following stream. All request packets this will be
	 * zero, most response packets will be non negative.
	 * 
	 * @return The size in bytes of this packets "size" field
	 */
	public int getsize() {
		return size;
	}

}