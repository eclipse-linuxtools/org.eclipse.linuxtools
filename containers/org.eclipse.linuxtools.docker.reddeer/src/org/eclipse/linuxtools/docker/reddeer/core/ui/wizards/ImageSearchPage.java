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

import java.util.List;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class ImageSearchPage extends WizardPage {

	public ImageSearchPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
		new DefaultShell("Search and pull a Docker image");
	}

	public void finish() {
		new FinishButton().click();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	public void searchImage(String imageName) {
		new LabeledText("Image:").setText(imageName);
		new PushButton("Search").click();
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);
	}

	public void searchImage() {
		new PushButton("Search").click();
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);
	}

	public List<TableItem> getSearchResults() {
		return new DefaultTable().getItems();
	}

	public boolean searchResultsContains(String imageName) {
		for (TableItem item : getSearchResults()) {
			if (imageName.contains(item.getText())) {
				return true;
			}
		}
		return false;
	}

	public void next() {
		new NextButton().click();
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);
	}

	public void selectImage(String imageName) {
		for (TableItem item : getSearchResults()) {
			if (imageName.contains(item.getText())) {
				item.select();
			}
		}
	}

}
