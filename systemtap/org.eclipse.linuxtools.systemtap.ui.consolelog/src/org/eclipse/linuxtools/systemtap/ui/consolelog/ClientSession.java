package org.eclipse.linuxtools.systemtap.ui.consolelog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.ErrorMessage;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.DMRequest;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.DMResponse;




/**
 * Singleton thread. Maintains the communication between the
 * data manager and the client (this).
 *  
 * @author patrickm
 *
 */
public final class ClientSession extends Thread {
	
	private static ClientSession instance = null;
	private static int portnumber, clientID;
	private static boolean connected;
	private static String hostname;
	private static int scriptnumber;
	private static InputStream in;
	private static TreeMap<Integer, LinkedBlockingQueue<byte[]>> mbox;

	private ClientSession () {
		// only happens once
		
		hostname = ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.HOST_NAME);
		portnumber = ConsoleLogPlugin.getDefault().getPreferenceStore().getInt(ConsoleLogPreferenceConstants.PORT_NUMBER);
		mbox = new TreeMap<Integer, LinkedBlockingQueue<byte[]>> ();
		connected = createConnection ();
		scriptnumber = 15;
		if (connected) this.start();
	}
	
	/**
	 * Send a request packet to the data manager. Synchronized, only
	 * one request can be sent at a time.
	 * 
	 * @param req	The filled in request packet to send over the socket.
	 * @return	true if successfully sent, false otherwise.
	 */
	public synchronized boolean sendRequest (DMRequest req) {
		
		try {
			// open perma-socket:
			Socket tmp = new Socket(hostname, portnumber);
			OutputStream out = tmp.getOutputStream();
			
			out.write(req.getData());
			out.flush();
			out.flush();
			out.close();
			
	    	return true;

		} catch (final UnknownHostException uhe) {
			new ErrorMessage("Unknown host!", "Check if server is running").open();
			System.err.println("Unknown host: " + uhe.getMessage());
			connected = false;
			return false;
		} catch (final IOException ioe) {
			new ErrorMessage("Unable to send request!", "Check if server is running").open();
			System.err.println("Req I/O error " + ioe.getMessage());
			connected = false;
			return false;
		}
	}

	/**
	 * Read a response from the data manager and return the response packet.
	 * 
	 * @return	A response packet. Possibly invalid/incomplete.
	 */
	public DMResponse recvResponse (int scriptnum) {
		
		if (!mbox.containsKey(scriptnum)) {
			return null;
		}
		try {
			DMResponse dm =  new DMResponse (mbox.get(scriptnum).take());
		
			return dm;
		} catch (InterruptedException ie) {
			return null;
		}
		
	}

	/**
	 * Read size bytes from the socket and return the result as a String. The
	 * size is most likely from DMResponse.packetsize .
	 * 
	 * @param size	The number of bytes to read from the open socket.
	 * @return	A String of size characters long, or null if there was an error.
	 */
	public byte[] recvData (int scriptnum, int size) {
		if (!mbox.containsKey(scriptnum)) {
	
			return null;
		}
		
		try {
			// maybe check that the sizes match?
			return mbox.get(scriptnum).take();
		
		} catch (InterruptedException ie) {
//			subscription.interrupt() was probably called 
			System.err.println("Interruptedrecvdata");
			return null;
		}
	}
	
	public boolean addSubscription (int scriptnum) {
		if (mbox.containsKey(scriptnum))
		{
			return false;
		}
		mbox.put(scriptnum, new LinkedBlockingQueue<byte[]>());
		return true;
	}
	
	public boolean delSubscription (int scriptnum) 
	{
		if (!mbox.containsKey(scriptnum))
			return false;
	
		
		mbox.get(scriptnum).clear();
		mbox.remove(scriptnum);
		return true;
	}

	public static synchronized ClientSession getInstance () {
		// synchronized so everybody has the same session
		if (instance == null)
			instance = new ClientSession ();
		else
		{
			if(!isConnected())
			{
				connected=instance.createConnection();
		    	scriptnumber = 15;
		    	if (connected) instance.start();
			}
		}
		return instance;
	}
	
    public static int getNewScriptId()
    {
      return scriptnumber++;	
    }
	
	public static boolean isConnected () {
		return connected;
	}
	
	public int getcid () {
		return clientID;
	}
	
	public void run () {
		
		while (!Thread.interrupted()) {
			
			final byte[] headBuffer = new byte[DMResponse.packetsize];
			final byte[] bodyBuffer;
			DMResponse header;
			
			try {
	//		    Boolean first = new Boolean(true);
				in.read(headBuffer, 0, headBuffer.length);
				header = new DMResponse (headBuffer);
		        if(header.isValid())
		        {
		        	bodyBuffer = new byte[header.getsize()];
					in.read(bodyBuffer, 0, bodyBuffer.length);
					if(header.getsource() == DMResponse.SUBSCRIPTION_MGR)
					{
						if(!mbox.containsKey(header.getscriptID()))
							mbox.put(header.getscriptID (), new LinkedBlockingQueue<byte[]> ());
						mbox.get(header.getscriptID()).put(header.tobytes());
						mbox.get(header.getscriptID()).put(bodyBuffer);
					
					}
					else
					{
						if(!mbox.containsKey(header.getscriptID()))
							mbox.put(header.getscriptID (), new LinkedBlockingQueue<byte[]> ());
						mbox.get(header.getscriptID()).put(header.tobytes());
						mbox.get(header.getscriptID()).put(bodyBuffer);
				
					}	
				}
		       else {
					// either the header was not valid,
					// or the scriptid has not been seen before..

				}
				
			} catch (InterruptedException ie) {
				// probably shutting down
				System.err.println ("Interrupted: " + ie.getMessage());
				break;

			} catch (IOException ioe) {
				new ErrorMessage("I/O Error Check host!", "See stderr for more details").open();
				System.err.println ("i/o error: " + ioe.getMessage());
				return;
			
			} catch (Exception e) {
				new ErrorMessage("Check if DMD is running", "See stderr for more details").open();
				System.err.println ("Server terminated unexpectedly?," + e.getMessage());
				return;
			}
			
		
			
		} // while
		this.destroyConnection();
		
	}
	
	/**
     * Grab hostname and port settings from the settings, attempt to open
     * socket, and finally attempt to open a connection to the data manager
     * and store a clientID. If anything fails the connection is left in an
     * unknown state (see stderr).
     * 
     * @return	true if everything succeeded, false otherwise.
     */
	public boolean createConnection () {

		final DMRequest ccpacket = new DMRequest (DMRequest.CREATE_CONN, 0, 0, 0);
		final byte buffer[] = new byte[DMResponse.packetsize];
		final DMResponse respacket;
		hostname = ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.HOST_NAME);
		portnumber = ConsoleLogPlugin.getDefault().getPreferenceStore().getInt(ConsoleLogPreferenceConstants.PORT_NUMBER);

		OutputStream out = null;
		try {

			Socket tmp = new Socket(hostname, portnumber);
			out = tmp.getOutputStream();
			in = tmp.getInputStream();
			out.write(ccpacket.getData());
			out.flush();
			in.read(buffer, 0, buffer.length);
			respacket = new DMResponse (buffer);

		} catch (final UnknownHostException uhe) {
			new ErrorMessage("Unknown host!", "See stderr for more details").open();
			System.err.println("Unknown host: " + uhe.getMessage());
			return false;
		} catch (final IOException ioe) {
			new ErrorMessage("Connection I/O error!", "See stderr for more details").open();
			System.err.println("Con I/O error: " + ioe.getMessage());
			//ioe.printStackTrace();
			return false;
		}

		if (!respacket.isValid())
		{
			return false;}


			clientID = respacket.getclientID();
			return true;
	}
	
	/**
     * Send a disconnection request to the data manager, and attempt to
     * close the open socket to the data manager. If anything fails the
     * connection is left in a unknown state (see stderr).
     * 
     * @return	true if successfully destroyed, false otherwise
     */
	public boolean destroyConnection () {

    	final DMRequest dcpacket = new DMRequest (DMRequest.DESTROY_CONN, 0, clientID, 0);
    	
    	try {
    		sendRequest (dcpacket);
	    	in.close();
	    	connected = false;
	    	return true;
    	} catch (final IOException e) {
    		new ErrorMessage("Connection close error!", "See stderr for more details").open();
    		System.err.println("Close error: " + e.getMessage());
    		return false;
    	}
    	
// Old code that actually checks the response and whatnot,
// removed because we can hang waiting for a response from
// (for example) a recently deceased Data Manager 
//    	if (!sendRequest (dcpacket))
//    		rc = false;
//    
//    	final DMResponse respacket = recvResponse ();
//    	
//    	if (respacket.getreturnCode() != 0)
//    		rc = false;
//    	
//    	try {
//	    	in.close();
//    	} catch (final IOException e) {
//    		System.err.println("Close error: " + e.getMessage());
//    		rc = false;
//    	}
//
//    	return rc;
	}
}
