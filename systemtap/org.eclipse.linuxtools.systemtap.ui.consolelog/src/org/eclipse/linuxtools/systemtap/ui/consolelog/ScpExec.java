package org.eclipse.linuxtools.systemtap.ui.consolelog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ScpExec implements Runnable {

	private Session session;
	private Channel channel;

	public ScpExec(String cmds[], String moduleName) {
		this.command = cmds[0];
		for (String cmd:cmds) {
			this.command = this.command + " " + cmd; //$NON-NLS-1$
		}
	}

	/**
	 * Starts the <code>Thread</code> that the new <code>Process</code> will run in.
	 * This must be called in order to get the process to start running.
	 */
	public void start() {
		if(init().isOK()) {
			Thread t = new Thread(this, command);
			t.start();
		} else {
			stop();
		}
	}

	/**
	 * This transfers any listeners which may have been added
	 * to the command before the process has been constructed
	 * properly to the process itself.
	 * @since 1.2
	 */
	protected void transferListeners(){
		for(IGobblerListener listener :inputListeners) {
			inputGobbler.addDataListener(listener);
		}
		for(IGobblerListener listener: errorListeners) {
			errorGobbler.addDataListener(listener);
		}
	}

	protected IStatus init() {
		String user = ConsoleLogPlugin.getDefault().getPreferenceStore()
				.getString(ConsoleLogPreferenceConstants.SCP_USER);
		String host = ConsoleLogPlugin.getDefault().getPreferenceStore()
				.getString(ConsoleLogPreferenceConstants.HOST_NAME);
		try {
			JSch jsch = new JSch();

			session = jsch.getSession(user, host, 22);

			session.setPassword(ConsoleLogPlugin.getDefault()
					.getPreferenceStore()
					.getString(ConsoleLogPreferenceConstants.SCP_PASSWORD));

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no"); //$NON-NLS-1$//$NON-NLS-2$
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("exec"); //$NON-NLS-1$
			((ChannelExec) channel).setCommand(command);

			channel.setInputStream(null, true);
			channel.setOutputStream(System.out, true);
			channel.setExtOutputStream(System.err, true);

			errorGobbler = new StreamGobbler(channel.getExtInputStream());
			inputGobbler = new StreamGobbler(channel.getInputStream());

			this.transferListeners();
			return Status.OK_STATUS;

		} catch (JSchException e) {
			IStatus status = new Status(IStatus.ERROR, ConsoleLogPlugin.PLUGIN_ID, Messages.ScpExec_FileTransferFailed, e);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.ScpExec_Error, e.getMessage(), status);
			return status; 
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, ConsoleLogPlugin.PLUGIN_ID, Messages.ScpExec_FileTransferFailed, e);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.ScpExec_Error, e.getMessage(), status);
			return status;
		}
	}

	public void run() {
		try {
			channel.connect();

			errorGobbler.start();
			inputGobbler.start();

			while (!stopped) {
				if (session.isConnected() == false) {
					throw new RuntimeException(Messages.ScpExec_ConnTimedOut);
				}

				if (channel.isClosed() || (channel.getExitStatus() != -1)) {
					stop();
					break;
				}
			}

		} catch (JSchException e) {
			e.printStackTrace();
		}
	}

    /* Stops the process from running and stops the <code>StreamGobblers</code> from monitering
	 * the dead process.
	 */
	public synchronized void stop() {

		if(!stopped) {
			stopped = true;
			if(null != errorGobbler)
				errorGobbler.stop();
			if(null != inputGobbler)
				inputGobbler.stop();
            channel.disconnect();
            session.disconnect();
		}
	}

	/**
	 * Method to check whether or not the process in running.
	 * @return The execution status.
	 */
	public boolean isRunning() {
		return !stopped;
	}

	/**
	 * Method to check if this class has already been disposed.
	 * @return Status of the class.
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Registers the provided <code>IGobblerListener</code> with the InputStream
	 * @param listener A listener to monitor the InputStream from the Process
	 */
	public void addInputStreamListener(IGobblerListener listener) {
		if(null != inputGobbler)
			inputGobbler.addDataListener(listener);
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

	/**
	 * Disposes of all internal components of this class. Nothing in the class should be
	 * referenced after this is called.
	 */
	public void dispose() {
		if(!disposed) {
			stop();
			disposed = true;
			inputListeners = null;
			errorListeners = null;

			if(null != inputGobbler)
				inputGobbler.dispose();
			inputGobbler = null;

			if(null != errorGobbler)
				errorGobbler.dispose();
			errorGobbler = null;
		}
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
		}
		return b;
	}

   protected boolean stopped = false;
   private boolean disposed = false;
   protected StreamGobbler inputGobbler = null;
   protected StreamGobbler errorGobbler = null;
   protected ArrayList<IGobblerListener> inputListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
   protected ArrayList<IGobblerListener> errorListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
   private String command;

   public static final int ERROR_STREAM = 0;
   public static final int INPUT_STREAM = 1;

}
