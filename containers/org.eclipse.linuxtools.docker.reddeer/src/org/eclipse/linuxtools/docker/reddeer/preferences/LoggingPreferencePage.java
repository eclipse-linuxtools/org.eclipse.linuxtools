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
import org.eclipse.reddeer.swt.impl.button.CheckBox;

public class LoggingPreferencePage extends PreferencePage {

	public LoggingPreferencePage(ReferencedComposite referenced) {
		super(referenced, "Docker", "Docker Machine");
	}

	// This constructor no longer works
	public LoggingPreferencePage() {
		super(null, "Docker", "Docker Machine");
	}
	
	public void setAutomaticallyLog(boolean toggle) {
		CheckBox cb = new CheckBox("Automatically log when Container starts");
		cb.toggle(toggle);
	}

	public void setAutomaticallyLog() {
		setAutomaticallyLog(true);
	}

	public void setRequestTimestamp(boolean toggle) {
		CheckBox cb = new CheckBox("Request time stamp for logs");
		cb.toggle(toggle);
	}

	public void setRequestTimestamp() {
		setRequestTimestamp(true);
	}

}
