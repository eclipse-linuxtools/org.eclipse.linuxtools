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

package org.eclipse.linuxtools.docker.reddeer.ui;

import org.eclipse.reddeer.swt.api.CTabFolder;
import org.eclipse.reddeer.swt.api.CTabItem;
import org.eclipse.reddeer.swt.impl.ctab.DefaultCTabFolder;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;

public class DockerTerminal extends WorkbenchView {

	public DockerTerminal() {
		super("Terminal");
	}

	public CTabItem getPage(String pageLabel) {
		CTabFolder tabFolder = new DefaultCTabFolder();
		CTabItem tabItem = tabFolder.getSelection();
		tabItem.activate();
		return tabItem;
	}

	public String getTextFromPage(String tabName) {
		return getPage(tabName).getText();
	}

}
