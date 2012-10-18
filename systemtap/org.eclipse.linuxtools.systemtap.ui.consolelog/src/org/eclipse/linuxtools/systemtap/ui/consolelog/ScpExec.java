package org.eclipse.linuxtools.systemtap.ui.consolelog;

import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.ErrorMessage;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobbler;
import com.jcraft.jsch.*;
import java.io.*;
import java.util.ArrayList;

public class ScpExec implements Runnable {
	
	private Session session;
	private Channel channel;
//	private String moduleName;
  
	public ScpExec(String cmd[], String moduleName) {
	//	this.moduleName = moduleName;
		this.command = "";
		
		try{
			
		this.command = cmd[0];
		
		for (int i = 1; i<cmd.length; i++)
		{
			this.command = this.command + " " + cmd[i];
		}
		
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Starts the <code>Thread</code> that the new <code>Process</code> will run in.
	 * This must be called in order to get the process to start running.
	 */
	public void start() {
		if(init()) {
			Thread t = new Thread(this, command);
			t.start();
		} else {
			stop();
		//	returnVal = Integer.MIN_VALUE;
		}
	}

	/**
	 * This transfers any listeners which may have been added
	 * to the command before the process has been constructed
	 * properly to the process itself.
	 * @since 1.2
	 */
	protected void transferListeners(){
		int i;
		for(i=0; i<inputListeners.size(); i++)
			inputGobbler.addDataListener(inputListeners.get(i));
		for(i=0; i<errorListeners.size(); i++)
			errorGobbler.addDataListener(errorListeners.get(i));
	}

	protected boolean init()
	{
	  String user=ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.SCP_USER);
      String host=ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.HOST_NAME);
      try{
      JSch jsch=new JSch();
      
      session=jsch.getSession(user, host, 22);

      session.setPassword(ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.SCP_PASSWORD));
      
      java.util.Properties config = new java.util.Properties();
                      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config); 
      session.connect();
      channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

      // get I/O streams for remote scp
    // channel.
      channel.setInputStream(null,true);
      channel.setOutputStream(System.out,true);
      channel.setExtOutputStream(System.err,
    		  true);
    //  InputStream in=channel.getInputStream();
   
		errorGobbler = new StreamGobbler(channel.getExtInputStream());            
		inputGobbler = new StreamGobbler(channel.getInputStream());

		this.transferListeners();
		return true;

      }catch(Exception e)
      {
    	  e.printStackTrace();
    	  new ErrorMessage("Error in connection", "File Transfer failed.\n See stderr for more details").open();
    	  return false;
      }
    }
	
    public void run() {
      try {
 //     Channel channel=session.openChannel("exec");
  //    ((ChannelExec)channel).setCommand(cmd);

      // get I/O streams for remote scp
      
  //    channel.setInputStream(null,true);
  //    channel.setOutputStream(System.out,true);
  //    channel.setExtOutputStream(System.err,
   // 		  true);
   //   InputStream in=channel.getInputStream();
      channel.connect();
      
  	  errorGobbler.start();
	  inputGobbler.start();

     // Thread.sleep(1000);
      //  while (true) {
		while(!stopped) {
        if (session.isConnected() == false) {
    	                          throw new
    	  RuntimeException("Connection Timed Out");
    	                      }

    /*	   while (in.available() > 0) {
                 int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
    	                              break;
    	                          }
                   output = output + new String(tmp, 0, i);
    	                      }*/
    	    if (channel.isClosed() || (channel.getExitStatus() != -1) ) 
    	         {
    	    		stop();
    	                          break;
    	                      }

        }

    }
    catch(Exception e){
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

  static int checkAck(InputStream in) throws IOException{
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c;
      do {
	c=in.read();
	sb.append((char)c);
      }
      while(c!='\n');
      if(b==1){ // error
	//System.out.print(sb.toString());
      }
      if(b==2){ // fatal error
	//System.out.print(sb.toString());
      }
    }
    return b;
  }

  
   protected boolean stopped = false;
	private boolean disposed = false;
	protected StreamGobbler inputGobbler = null;
	protected StreamGobbler errorGobbler = null;
	protected ArrayList<IGobblerListener> inputListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
	protected ArrayList<IGobblerListener> errorListeners = new ArrayList<IGobblerListener>();	//Only used to allow adding listeners before creating the StreamGobbler
	//private int returnVal = Integer.MAX_VALUE;
	private String command;

	
	public static final int ERROR_STREAM = 0;
	public static final int INPUT_STREAM = 1;

}
