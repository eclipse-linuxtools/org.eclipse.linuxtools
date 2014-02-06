/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.structures.Messages;

/**
 * A class containing all properties relating to a remote run of a SystemTap
 * script, such as user name and password.
 * @since 3.0
 * @author Andrew Ferrazzutti
 */
public class RemoteScriptOptions {

	private String userName = null;
	private String password = null;
	private String hostName = null;

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getHostName() {
		return hostName;
	}

	public RemoteScriptOptions(String userName, String password, String hostName) {
		if (userName == null || password == null || hostName == null) {
			throw new IllegalArgumentException(Messages.RemoteScriptOptions_invalidArguments);
		}
		this.userName = userName;
		this.password = password;
		this.hostName = hostName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RemoteScriptOptions) {
			return false;
		}
		RemoteScriptOptions other = (RemoteScriptOptions) obj;
		return this.userName.equals(other.userName)
				&& this.password.equals(other.password)
				&& this.hostName.equals(other.hostName);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
