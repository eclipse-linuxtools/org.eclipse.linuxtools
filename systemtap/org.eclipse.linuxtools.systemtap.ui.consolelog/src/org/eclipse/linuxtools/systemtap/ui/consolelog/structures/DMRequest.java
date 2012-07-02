package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.ErrorMessage;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;





/**
 * The request packet class. This class is used to construct a "packet", which
 * is just a stream of bytes with specific properties. Normally a DMRequest
 * object is constructed, and then its byte-stream is sent over a socket to the
 * data manager.
 * 
 * @author patrickm
 * 
 */
public class DMRequest extends DMPacket {

	// packet size
	public static final int packetsize = 28; // bytes

	// request types
	public static final int CREATE_CONN = 1;
	public static final int SUBSCRIBE = 2;
	public static final int UNSUBSCRIBE = 3;
	public static final int GET_STATUS = 4;
	public static final int SUBSCRIPTION_MODIFY = 5;
	public static final int DESTROY_CONN = 6;
	public static final int FILE = 7;

	// request specific
	private final int reqType;
	private final boolean isGuru;

	/**
	 * Construct a request packet. There is no error checking, if you construct
	 * a bad packet then send it to the data manager to see what is wrong with
	 * it.
	 * 
	 * @param reqType
	 *            One of the public REQ_* fields of this packet, the "request" /
	 *            command of this packet.
	 * @param scriptID
	 *            The script ID associated with this packet, right now only
	 *            valid for un/subscribe.
	 * @param clientID
	 *            The client ID of this packet. Every packet must have the
	 *            correct client ID, or 0 if they are requesting one.
	 * @param size
	 *            The size of the succeeding body. Always 0 for now, may change
	 *            in the future.
	 */
	public DMRequest(final int reqType, final int scriptID, final int clientID,
			final int size) {
		this.reqType = reqType;
		this.scriptID = scriptID;
		this.filename = "";
		this.clientID = clientID;
		this.size = size;
		this.isGuru = false;
	}
	
	public DMRequest(final int reqType, final int scriptnum,final String filename, final int clientID,
			final int size, final boolean isGuru) {
		this.reqType = reqType;
		this.filename = filename;
		this.scriptID = scriptnum;
		this.clientID = clientID;
		this.size = size;
		this.isGuru = isGuru;

	}


	/**
	 * Convert all of this packet's fields into a stream of bytes of length
	 * packetsize. Any errors would leave the packet in an unknown state, so
	 * null is returned.
	 * 
	 * @return A stream of bytes representing the packet or null.
	 */
	public byte[] getData() {
		// TODO: close these open streams, if needed
		if (reqType == SUBSCRIBE)
			return getFile();
		else
			return getMessage();
	}
	
	public int getReqtype()
	{
		return reqType;
	}

	private byte[] getMessage()
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final DataOutputStream dos = new DataOutputStream(baos);
	    char a[] = new char[20];
		for (int i = 0; i < 20 ; i++)
			  a[i] = ' ';
		String b= ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.SCP_USER);
		b.getChars(0, b.length(), a, 0);
		
		b = null;
		b = String.copyValueOf(a);
		
		//String b = String.copyValueOf(a, 0, 20);
        //System.out.println(a.toString() + " " + b + " " + b.length());
		try {
			dos.writeInt(BEGINSTR);
			dos.writeInt(reqType);
			dos.writeInt(scriptID);
			dos.writeInt(clientID);
			dos.writeInt(0);
			dos.writeInt(ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.SCP_USER).length());
			dos.writeBytes(b);
		//	System.out.println(a.length + " " + a.toString().length() + " " + filename.substring(filename.lastIndexOf('/')).length());
			dos.flush();
		} catch (final IOException ioe) {
			new ErrorMessage("Request packet error!", "See stderr for more details").open();
			System.err.println("Packet Error: " + ioe.getMessage());
			// baos.close();
			// dos.close();
			return null;
		}
		return baos.toByteArray();	
	}
	
	
	private byte[] getFile()
	{
		
		try {
	/*	int len = 0;
		File f = new File(filename);	
		FileReader fr = new FileReader(f);
		
		BufferedReader br = new BufferedReader(fr);
		StringBuilder sb = new StringBuilder();
		String line;
		while(null != (line=br.readLine())) {
			sb.append(line + "\n");
		}
		File tmpfile = new File("/home/anithra/eclipse/tmp.stp");
		tmpfile.createNewFile();
		FileWriter tmpfilewriter = new FileWriter(tmpfile);
		tmpfilewriter.write(sb.toString());
		tmpfilewriter.flush();
		tmpfilewriter.close();
		
	//	System.out.println("file:" + sb.toString());			
	//	BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
	//	byte[] buffer = new byte[in.available()];*/
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		final DataOutputStream dos = new DataOutputStream(baos);
     //      System.out.println("file:" + sb.length());
			dos.writeInt(BEGINSTR);
			dos.writeInt(reqType);
			dos.writeInt(scriptID);
			dos.writeInt(clientID);
			if(isGuru)
			dos.writeInt(1);
			else
		    dos.writeInt(0); 
			dos.writeInt(filename.length());
			dos.writeBytes(filename.substring(filename.lastIndexOf('/')));
			dos.flush();
			return baos.toByteArray();
			
		} catch (final Exception ioe) {
			new ErrorMessage("Request packet error!", "See stderr for more details").open();
			System.err.println("Packet Error: " + ioe.getMessage());
			// baos.close();
			// dos.close();
			return null;
		}
		
	}
	
	/**
	 * For debugging, dump the packet contents to a formatted string
	 */
	public String toString() {
		return "ID String:\t" + BEGINSTR + "\nRequest Type:\t" + reqType
				+ "\nScript #:\t" + scriptID + "\nFilename:\t" + filename + "\nClient ID:\t" + clientID
				+ "\nData Size:\t" + size + "\n";
	}
}
