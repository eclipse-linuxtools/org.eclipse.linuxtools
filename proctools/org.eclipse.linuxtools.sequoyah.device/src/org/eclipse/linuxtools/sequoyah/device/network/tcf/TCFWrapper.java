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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.network.tcf;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.OperationCode;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

public class TCFWrapper {

	private StringBuffer lastResponse = null;
	private IHost rseHost = null;
	
	public void connect(String host, int port) throws IOException {
		ISystemRegistry rseSystemRegistry = RSECorePlugin.getTheSystemRegistry();
		IRSECoreRegistry rseCoreRegistry = RSECorePlugin.getTheCoreRegistry();
		IRSESystemType tcfSystemType = rseCoreRegistry.getSystemTypeById("org.eclipse.tm.tcf.rse.systemType");
		try {
			rseHost = rseSystemRegistry.createHost(tcfSystemType, host, host, host);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disconnect() throws IOException {
		// TODO Auto-generated method stub

	}

	public StringBuffer getLastResponde() {
		return lastResponse;
	}

	public OperationCode login(String user, String password) throws IOException {
		// TODO Auto-generated method stub
		return OperationCode.SUCCESS;
	}

	public String fetchFile(String path, String fileName) throws IOException {
		IRemoteFileSubSystem remoteFileSubSystem =
				RemoteFileUtility.getFileSubSystem(rseHost);
		InputStream inputStream = null;

		try {
			remoteFileSubSystem.connect(new NullProgressMonitor(), false);
			inputStream = remoteFileSubSystem.getInputStream(path, fileName,
					false, new NullProgressMonitor());
		} catch (SystemMessageException sme) {
			// TODO Auto-generated catch block
			sme.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lastResponse = new StringBuffer();

		byte[] b = new byte[16];
		char[] c = new char[16];
		while (inputStream.read(b) >= 0) {
			for (int i = 0; i < b.length; i++) {
				c[i] = (char) b[i];
			}
			lastResponse.append(c);
		}
		return "OK"; 
	}

	public void sendData(String out) {
		// TODO Auto-generated method stub

	}

	public void setResponseLength(int maxLength) {
		// TODO Auto-generated method stub		
	}
}
