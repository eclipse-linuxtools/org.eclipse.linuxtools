package org.eclipse.linuxtools.systemtap.ui.consolelog;


//import com.trilead.ssh2.SCPClient;
//import com.trilead.ssh2.Connection;
//import com.jcraft.jsch.*;


import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.ErrorMessage;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.DMRequest;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.DMResponse;
import org.eclipse.linuxtools.systemtap.ui.structures.LoggingStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;

import com.jcraft.jsch.JSchException;



public class Subscription extends Thread {
	private int scriptid;
	private boolean running;
	private final ClientSession session;
	private final String filename;
	private final boolean isGuru;
	private boolean disposed = false;
	private PipedOutputStream pos = null;
	private PipedInputStream pis = null;
	private StreamGobbler inputGobbler = null;
	private StreamGobbler errorGobbler = null;
	private ArrayList<IGobblerListener> inputListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
	private ArrayList<IGobblerListener> errorListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
	private LoggingStreamDaemon logger;

	public Subscription(final int scriptid) {
		this.scriptid = scriptid;
		this.filename = null;
		this.running = false;
		this.session = ClientSession.getInstance();
		this.isGuru = false;

	}

	public Subscription(final String filename,boolean isGuru) {
		this.filename = filename;
		this.scriptid = -1;
		this.running = false;
		this.session = ClientSession.getInstance();
		this.isGuru = isGuru;
	}

	public boolean init() {
			// send subscription request packet
			// check if response is OK

		if (!ClientSession.isConnected()) {
			return false;
	     }


	  // BusyIndicator.showWhile(null, new Runnable() {
		//   public void run() {
        try{
		ScpClient scpclient = new ScpClient();
		 scpclient.transfer(filename,"/tmp/"+ filename.substring(filename.lastIndexOf('/')+1)); //$NON-NLS-1$
        } catch (IOException e) {
			e.printStackTrace();
		} catch (JSchException e) {
			e.printStackTrace();
		}
		scriptid = ClientSession.getNewScriptId();
		final DMRequest subreq = new DMRequest(DMRequest.SUBSCRIBE,scriptid, filename,session.getcid(), 0, isGuru);
		if (!session.sendRequest(subreq)) {
			//System.out.println("sent subscription");
		//	return false;
		}


		session.addSubscription(scriptid);
		// FIXME: horrible hack. I think there is some sort of deadlock issue
		// when starting up, either way this fixes it.
		try { Thread.sleep(500); }
		catch (InterruptedException ie) {}

		final DMResponse subrep = session.recvResponse(scriptid);
		if (subrep.isValid()) {
	    	scriptid = subrep.getscriptID();


			logger = new LoggingStreamDaemon();
			inputListeners.add(logger);

			try{
			pos = new PipedOutputStream();
	        pis = new PipedInputStream(pos);
	        pos.flush();
	    	}catch (IOException e) {
				new ErrorMessage("Could not subscribe!", "See stderr for more details").open();
			}

			inputGobbler = new StreamGobbler(pis);
			addInputStreamListener(logger);

			return true;
		}
		else {
			session.delSubscription(scriptid);
			new ErrorMessage("Could not subscribe!", "Response from Server not valid \n See stderr for more details").open();
			return false;
		}
	}

	/**
	 * Gather data from a previously started script and do stuff with it.
	 * Contains blocking reads.
	 *
	 */
	  @Override
	public void run () {
		running = true;
		DMResponse subrep = null;
		//long timeToRemove = 0;
		inputGobbler.start();
		while (!Thread.interrupted() && ClientSession.isConnected()) {
			subrep = session.recvResponse(scriptid);
			//timeToRemove = System.currentTimeMillis() - (1000 * ConsoleLogPlugin.getDefault().getPluginPreferences().getInt(ConsoleLogPreferenceConstants.SAVE_LENGTH));
			if (subrep == null) {
				// Interrupting this thread cause recvResponse to return
				// from its blocking read, leaving subrep null
				break;
			}

			if (subrep.isValid() && (subrep.getsource() == DMResponse.STDERR)) {
				// log the err output?, maybe pop up a dialog? ignore for now..
				final String outp = new String (session.recvData(scriptid, subrep.getsize()));
				final String[] lines = outp.trim().split("\n"); //$NON-NLS-1$
				for (final String str : lines) {
					inputGobbler.fireNewDataEvent(str + "\n"); //$NON-NLS-1$
				}
			return;
			}
			else if (subrep.isValid()) {

				final String outp = new String (session.recvData(scriptid, subrep.getsize()));

				final String[] lines = outp.trim().split("\n"); //$NON-NLS-1$
				for (final String str : lines) {
					inputGobbler.fireNewDataEvent(str + "\n"); //$NON-NLS-1$
				}

			}
		}

		final DMRequest unsub = new DMRequest (DMRequest.UNSUBSCRIBE, scriptid, session.getcid(), 0);
		if (!session.sendRequest (unsub))
			System.err.println ("Failed Unsubscribing: " + session.getcid());

		subrep = session.recvResponse(scriptid);
		delSubscription();

		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public String getScriptName(final int script) {
		return "table" + script; //$NON-NLS-1$
	}

	public String getOutput()
	{
		return logger.getOutput();
	}

	public boolean saveLog(File file) {
		return logger.saveLog(file);
	}



	public void dispose() {
		if(!disposed) {
			disposed = true;
			inputListeners = null;
			errorListeners = null;

			if(null != inputGobbler){
				inputGobbler.dispose();
				inputGobbler.stop();
			}
			inputGobbler = null;

			if(null != errorGobbler){
				errorGobbler.dispose();
				errorGobbler.stop();
			}
			errorGobbler = null;
		}
	}

	/**
	 * Registers the provided <code>IGobblerListener</code> with the InputStream
	 * @param listener A listener to monitor the InputStream from the Process
	 */
	public void addInputStreamListener(IGobblerListener listener) {
		if(null != inputGobbler)
		{
			inputGobbler.addDataListener(listener);
		}
		else
			inputListeners.add(listener);
	}

	/**
	 * Registers the provided <code>IGobblerListener</code> with the ErrorStream
	 * @param listener A listener to monitor the ErrorStream from the Process
	 */
	public void addErrorStreamListener(IGobblerListener listener) {
		if(null != errorGobbler)
			errorGobbler.addDataListener(listener);
		else
			errorListeners.add(listener);
	}

	/**
	 * Returns the list of everything that is listening the the InputStream
	 * @return List of all <code>IGobblerListeners</code> that are monitoring the stream.
	 */
	public ArrayList<IGobblerListener> getInputStreamListeners() {
		if(null != inputGobbler)
			return inputGobbler.getDataListeners();
		else
			return inputListeners;
	}

	/**
	 * Returns the list of everything that is listening the the ErrorStream
	 * @return List of all <code>IGobblerListeners</code> that are monitoring the stream.
	 */
	public ArrayList<IGobblerListener> getErrorStreamListeners() {
		if(null != errorGobbler)
			return errorGobbler.getDataListeners();
		else
			return errorListeners;
	}

	/**
	 * Removes the provided listener from those monitoring the InputStream.
	 * @param listener An </code>IGobblerListener</code> that is monitoring the stream.
	 */
	public void removeInputStreamListener(IGobblerListener listener) {
		if(null != inputGobbler)
			inputGobbler.removeDataListener(listener);
		else
			inputListeners.remove(listener);
	}

	/**
	 * Removes the provided listener from those monitoring the ErrorStream.
	 * @param listener An </code>IGobblerListener</code> that is monitoring the stream.
	 */
	public void removeErrorStreamListener(IGobblerListener listener) {
		if(null != errorGobbler)
			errorGobbler.removeDataListener(listener);
		else
			errorListeners.remove(listener);
	}

	public void delSubscription()
	{
		session.delSubscription(scriptid);
	}

}