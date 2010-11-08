/********************************************************************************
 * Copyright (c) 2008 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * Otavio Ferranti - Eldorado Research Institute - Bug 255255 [tml][proctools] Add extension points 
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.network.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.OperationCode;

/**
 * @author Ot�vio Ferranti
 */
public class TelnetWrapper {

	final public String PROMPT = "[###]:"; //$NON-NLS-1$
	final private String[] LOGIN_TOKENS = {"login:",  //$NON-NLS-1$
										   "Login:"}; //$NON-NLS-1$
	final private String[] PASSWORD_TOKENS = {"password:",  //$NON-NLS-1$
			                                  "Password:"}; //$NON-NLS-1$
	final private String[] LOGIN_FAILED_TOKENS = {"Login incorrect", //$NON-NLS-1$
			                                      "Access denied"}; //$NON-NLS-1$
	final private String CHANGE_PROMPT = "export PS1='[\\043\\043\\043]:'\n"; //$NON-NLS-1$

	// private ILogger logger = null;
	private TelnetClient client = null;
	private InputStream inStream = null;
	private PrintStream outStream = null;
	private StringBuffer lastResponse = null;
	private int maxResponseDataLength = 1024;
	
	private int MAX_LENGTH_PASSWORD_TOKENS = 32;
	private int MAX_LENGTH_LOGIN_FAILED = 32;
	private int MAX_LENGTH_LOGIN_INITSCREEN = 2048;
	private int MAX_LENGTH_CHANGE_PROMPT = 128;
	
	/**
	 * Constructor
	 */
	public TelnetWrapper() {
		if (null == this.client) {
			this.client = new TelnetClient();
			try {
				this.client.addOptionHandler(new TerminalTypeOptionHandler(
						"VT100", false, false, true, false)); //$NON-NLS-1$
				this.client.addOptionHandler(new SuppressGAOptionHandler(
						true, true, true, true));
				this.client.addOptionHandler(new EchoOptionHandler(
						false, false, false, false));
			} catch (InvalidTelnetOptionException itoe) { }
			// logger = LinuxToolsPlugin.getLogger();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#connect(java.lang.String, int)
	 */
	public OperationCode connect(String host, int port) throws IOException {
		OperationCode retVal;

		this.client.connect(host, port);
					
		inStream = client.getInputStream();
		outStream = new PrintStream (client.getOutputStream());
		
		// sendData(user+"\n");
		if(null == readUntilTokens(LOGIN_TOKENS)) {
			retVal = OperationCode.SUCCESS;
		} else {
			retVal = OperationCode.LOGIN_REQUIRED;
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#disconnect()
	 */
	public void disconnect() throws IOException {
		if (null != client) {
			client.disconnect();
			// System.out.println("Connection closed ...");
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#login(java.lang.String, java.lang.String)
	 */
	public OperationCode login(String user, String password) throws IOException {
		OperationCode retVal = OperationCode.UNEXPECTED_RESULT;
		
		sendData(user+"\n"); //$NON-NLS-1$
		setResponseLength(MAX_LENGTH_PASSWORD_TOKENS);
		if (null != readUntilTokens(PASSWORD_TOKENS)) {
			sendData(password+"\n"); //$NON-NLS-1$
			
			setResponseLength(MAX_LENGTH_LOGIN_FAILED);
			if (null != readUntilTokens(LOGIN_FAILED_TOKENS)) {
				if(null != readUntilTokens(LOGIN_TOKENS)) {
					retVal = OperationCode.LOGIN_FAILED;
				} else {
					retVal = OperationCode.UNEXPECTED_RESULT;
				}
				return retVal;
			} else {
				setResponseLength(MAX_LENGTH_LOGIN_INITSCREEN);
				int toBeSkipped = inStream.available();
				inStream.skip(toBeSkipped);
			}
		}
		try {
			Thread.sleep (2000);
		} catch (InterruptedException ite) {
			
		}
		setResponseLength(MAX_LENGTH_CHANGE_PROMPT);			
		sendData(CHANGE_PROMPT);
		if (null != readUntilTokens(PROMPT)) {
			retVal = OperationCode.SUCCESS;
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#getLastResponde()
	 */
	public StringBuffer getLastResponde() {
		return lastResponse;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#sendCommand(java.lang.String)
	 */
	public String sendCommand(String command) throws IOException {
		sendData (command);
		return readUntilTokens (PROMPT);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#sendData(java.lang.String)
	 */
	public void sendData(String out) {
		if (null != outStream) {
			outStream.print(out);
			outStream.flush();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IConnectionWrapper#setResponseLength(int)
	 */
	public void setResponseLength(int maxLength) {
		maxResponseDataLength = maxLength;
	}
	
	/**
	 * @param token
	 * @return
	 * @throws IOException
	 */
	private String readUntilTokens(String token)throws IOException {
		return readUntilTokens(new String[] {token});
	}

	/**
	 * @param tokenArray
	 * @return
	 * @throws IOException
	 */
	private String readUntilTokens(final String []tokenArray) throws IOException {
		String matchedString = null;
		boolean tokenFound = false;
		StringBuffer readData = new StringBuffer();
		
		HashMap<String,Integer> hashMap = new HashMap<String,Integer>();
	
		for (int i = 0; i < tokenArray.length; i++) {
			hashMap.put(tokenArray[i], new Integer(0));
		}
		
		for (int i = 0; i < this.maxResponseDataLength; i++) {

			int aux = inStream.read();

			char ch = 0;
			if (aux == 0 || aux == 1) {
				continue;
			} else {
				ch = (char) aux;
			}
			readData.append(ch);
			
			// if (ch != 1 && ch != 0)
			//	 logger.info(ch);

			for (int j = 0; j < tokenArray.length; j++) {
				String token = tokenArray[j];
				Integer rank = hashMap.get(token);
				if (ch == token.charAt(rank)) {
					rank ++;
					if (rank >= token.length()) {
						// MATCHED
						readData.setLength(readData.length() - token.length());
						matchedString = token;
						tokenFound = true;
					}
				} else {
					rank = 0;
				}
				hashMap.put(token, rank);
			}
			if (tokenFound) {
				break;
			}
		}
		this.lastResponse = readData;
		return matchedString;
	}
}
