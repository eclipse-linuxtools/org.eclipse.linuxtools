/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat, Inc.
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
import org.eclipse.reddeer.swt.impl.text.LabeledText;

public class DockerMachinePreferencePage extends PreferencePage {

	public DockerMachinePreferencePage(ReferencedComposite referenced) {
		super(referenced, "Docker", "Docker Machine");
	}

	// Following constructor no longer works
	public DockerMachinePreferencePage() {
		super(null, "Docker", "Docker Machine");
	}

	public void setDockerMachine(String pathToDockerMachine) {
		new LabeledText("Docker Machine").setText(pathToDockerMachine);
	}

	public void setVMDriver(String pathToVMDriver) {
		new LabeledText("VM Driver").setText(pathToVMDriver);
	}

}
