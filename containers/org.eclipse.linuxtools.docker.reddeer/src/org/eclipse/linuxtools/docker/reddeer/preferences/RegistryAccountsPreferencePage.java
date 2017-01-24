/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.docker.reddeer.preferences;

import org.jboss.reddeer.jface.preference.PreferencePage;
import org.jboss.reddeer.swt.api.Table;
import org.jboss.reddeer.swt.impl.button.OkButton;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.table.DefaultTable;
import org.jboss.reddeer.swt.impl.text.LabeledText;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class RegistryAccountsPreferencePage extends PreferencePage {

	public static final String SERVER_ADDRESS = "Server Address:";
	public static final String USERNAME = "Username:";
	public static final String EMAIL = "Email:";
	public static final String PASSWORD = "Password:";

	public RegistryAccountsPreferencePage() {
		super("Docker", "Registry Accounts");
	}

	public void addRegistry(String serverAddress, String email, String userName, String password) {
		new PushButton("Add").click();
		new LabeledText(SERVER_ADDRESS).setText(serverAddress);
		new LabeledText(USERNAME).setText(userName);
		new LabeledText(EMAIL).setText(email);
		new LabeledText(PASSWORD).setText(password);
		new OkButton().click();
	}

	public void editRegistry(String serverAddress, String email, String userName, String password) {
		Table table = new DefaultTable();
		if (table.containsItem(serverAddress)) {
			table.select(serverAddress);
			new PushButton("Edit").click();
			new LabeledText(SERVER_ADDRESS).setText(serverAddress);
			new LabeledText(USERNAME).setText(userName);
			new LabeledText(EMAIL).setText(email);
			new LabeledText(PASSWORD).setText(password);
			new PushButton("OK").click();
		}
	}

	public void removeRegistry(String serverAddress) {
		Table table = new DefaultTable();
		if (table.containsItem(serverAddress)) {
			table.select(serverAddress);
			new PushButton("Remove").click();
		}
	}

	public void removeAllRegistries() {
		Table table = new DefaultTable();
		for (int i = 0; i < table.rowCount(); i++) {
			table.select(0);
			new PushButton("Remove").click();
		}
	}

}
