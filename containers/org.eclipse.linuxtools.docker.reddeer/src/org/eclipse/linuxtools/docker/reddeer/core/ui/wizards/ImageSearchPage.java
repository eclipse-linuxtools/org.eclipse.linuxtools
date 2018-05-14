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
