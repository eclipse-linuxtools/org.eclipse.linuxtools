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

package org.eclipse.linuxtools.docker.reddeer.ui;

import org.jboss.reddeer.swt.impl.ctab.DefaultCTabFolder;
import org.jboss.reddeer.swt.api.CTabFolder;
import org.jboss.reddeer.swt.api.CTabItem;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

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
