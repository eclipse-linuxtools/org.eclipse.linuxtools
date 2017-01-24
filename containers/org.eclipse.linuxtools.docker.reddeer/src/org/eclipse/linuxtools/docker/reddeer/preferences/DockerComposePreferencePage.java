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

public class DockerComposePreferencePage extends PreferencePage {

	public DockerComposePreferencePage() {
		super("Docker", "Docker Compose");
	}

	public void setPathToDockerCompose(String path) {
		new LabeledText("Docker Compose ").setText(path);
	}

	public String getPathToDockerCompose() {
		return new LabeledText("Docker Compose ").getText();
	}
}
