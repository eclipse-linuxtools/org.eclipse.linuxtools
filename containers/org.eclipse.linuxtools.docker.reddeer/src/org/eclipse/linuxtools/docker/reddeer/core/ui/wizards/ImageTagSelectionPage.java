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

package org.eclipse.linuxtools.docker.reddeer.core.ui.wizards;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

import java.util.List;

import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.jface.wizard.WizardPage;
import org.jboss.reddeer.swt.api.TableItem;
import org.jboss.reddeer.swt.impl.button.CancelButton;
import org.jboss.reddeer.swt.impl.button.FinishButton;
import org.jboss.reddeer.swt.impl.table.DefaultTable;

public class ImageTagSelectionPage extends WizardPage {

	public ImageTagSelectionPage() {
		super();
		new WaitUntil(new ShellWithTextIsAvailable("Search and pull a Docker image"), TimePeriod.NORMAL);
	}

	public void finish() {
		new FinishButton().click();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	public List<TableItem> getTags() {
		return new DefaultTable().getItems();
	}

	public boolean tagsContains(String tagName) {
		for (TableItem item : getTags()) {
			if (tagName.equals(item.getText())) {
				return true;
			}
		}
		return false;
	}

	public void selectTag(String tag) {
		for (TableItem item : getTags()) {
			if (tag.contains(item.getText())) {
				item.select();
			}
		}
	}

	public void cancel() {
		new CancelButton().click();
	}

}
