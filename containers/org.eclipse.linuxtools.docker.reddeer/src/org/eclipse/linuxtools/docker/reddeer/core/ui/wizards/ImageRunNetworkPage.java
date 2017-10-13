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

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class ImageRunNetworkPage extends WizardPage {

	public ImageRunNetworkPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
		new WaitUntil(new ShellIsAvailable("Run a Docker Image"), TimePeriod.LONG);
	}

	public void finish() {
		if (new FinishButton().isEnabled()) {
			new FinishButton().click();
			new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		} else {
			throw new RuntimeException("Image cannot be run! (Duplicate name?)");
		}
	}
	
	public void setDefaultNetworkMode() {
		new RadioButton("Default").click();
	}
	
	public void setBridgeNetworkMode() {
		new RadioButton("Bridge").click();
	}

	public void setHostNetworkMode() {
		new RadioButton("Host").click();
	}
	
	public void setNoneNetworkMode() {
		new RadioButton("None").click();
	}

	public void setContainerNetworkMode(String containerName) {
		new RadioButton("Container:").click();
		new LabeledCombo("").setText(containerName);
	}
	
	public void setOtherNetworkMode(String other) {
		new RadioButton("Other").click();
		new LabeledText("").setText(other);
	}

}
