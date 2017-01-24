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
import org.jboss.reddeer.swt.impl.button.CheckBox;

/**
 * 
 * 
 * @author jkopriva@redhat.com
 */

public class LoggingPreferencePage extends PreferencePage {

	public LoggingPreferencePage() {
		super("Docker", "Docker Machine");
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
