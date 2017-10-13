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

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CancelButton;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

public class ImageTagSelectionPage extends WizardPage {

	public ImageTagSelectionPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
		new WaitUntil(new ShellIsAvailable("Search and pull a Docker image"), TimePeriod.DEFAULT);
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
