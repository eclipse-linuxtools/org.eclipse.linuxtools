/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.docker.reddeer.preferences;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.LabeledText;

public class RegistryAccountsPreferencePage extends PreferencePage {

	public static final String SERVER_ADDRESS = "Server Address:";
	public static final String USERNAME = "Username:";
	public static final String EMAIL = "Email:";
	public static final String PASSWORD = "Password:";

	public RegistryAccountsPreferencePage(ReferencedComposite referenced) {
		super(referenced, "Docker", "Registry Accounts");
	}

	// Following constructor no longer works
	public RegistryAccountsPreferencePage() {
		super(null, "Docker", "Registry Accounts");
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
