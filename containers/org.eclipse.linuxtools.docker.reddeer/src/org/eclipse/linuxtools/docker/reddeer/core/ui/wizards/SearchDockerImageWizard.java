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
package org.eclipse.linuxtools.docker.reddeer.core.ui.wizards;

import org.eclipse.reddeer.jface.wizard.WizardDialog;

public class SearchDockerImageWizard  extends WizardDialog {
	
	public SearchDockerImageWizard() {
		super("Search and pull a Docker image");
	}

}