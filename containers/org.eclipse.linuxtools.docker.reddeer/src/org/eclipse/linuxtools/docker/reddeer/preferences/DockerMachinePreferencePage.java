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
import org.jboss.reddeer.swt.impl.text.LabeledText;

/**
 * 
 * 
 * @author jkopriva@redhat.com
 */

public class DockerMachinePreferencePage extends PreferencePage {

	public DockerMachinePreferencePage() {
		super("Docker", "Docker Machine");
	}

	public void setDockerMachine(String pathToDockerMachine) {
		new LabeledText("Docker Machine").setText(pathToDockerMachine);
	}

	public void setVMDriver(String pathToVMDriver) {
		new LabeledText("VM Driver").setText(pathToVMDriver);
	}

}
