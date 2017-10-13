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
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class ImageRunResourceVolumesVariablesPage extends WizardPage {

	public ImageRunResourceVolumesVariablesPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	public void finish() {
		new FinishButton().click();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}
	
	public void next() {
		new NextButton().click();
	}

	public void addDataVolumeNoExternalMount(String containerPath) {
		new PushButton(0, new WithTextMatcher("Add...")).click();
		new LabeledText("Container path:").setText(containerPath);
		new RadioButton("No external mount").click();
		new OkButton().click();
	}

	public void addDataVolumeToHost(String containerPath, String path) {
		addDataVolumeToHost(containerPath, path, false);
	}

	public void addDataVolumeToHost(String containerPath, String path, boolean readOnly) {
		new PushButton(0, new WithTextMatcher("Add...")).click();
		new LabeledText("Container path:").setText(containerPath);
		new RadioButton("Mount a host directory or host file").click();
		new LabeledText("Path:").setText(path);
		new CheckBox("Read-only access").toggle(readOnly);
		;
		new OkButton().click();
	}

	public void addDataVolumeToContainer(String containerPath, String containerName) {
		new PushButton(0, new WithTextMatcher("Add...")).click();
		new LabeledText("Container path:").setText(containerPath);
		new RadioButton("Mount a data volume container").click();
		new DefaultCombo("Container").setText(containerName);
		new OkButton().click();
	}

	public void addEnviromentVariable(String name, String value) {
		new PushButton(1, new WithTextMatcher("Add...")).click();
		new LabeledText("Name:").setText(name);
		new LabeledText("Value:").setText(value);
		new OkButton().click();
	}

	public void addLabel(String name, String value) {
		new PushButton(2, new WithTextMatcher("Add...")).click();
		new LabeledText("Name:").setText(name);
		new LabeledText("Value:").setText(value);
		new OkButton().click();
	}

	public void setResourceLimitation(String CPU, String memoryLimit) {
		new CheckBox("Enable resource limitations").click();

		switch (CPU) {
		case "Low":
			new RadioButton("Low").click();
			break;
		case "Medium":
			new RadioButton("Medium").click();
			break;
		case "High":
			new RadioButton("High").click();
			break;
		default:
			new RadioButton("Medium").click();
		}

		new LabeledText("Memory limit:").setText(memoryLimit);
	}

	public void setEntrypoint(String Entrypoint) {
		new LabeledText("Entrypoint:").setText(Entrypoint);
	}

	public void setCommand(String command) {
		new LabeledText("Command:").setText(command);
	}

	public void setPublishAllExposedPorts() {
		new CheckBox("Publish all exposed ports to random ports on the host interfaces").click();
	}

	public void setKeepSTDINOpen() {
		new CheckBox("Keep STDIN open to Console even if not attached (-i)").click();
	}

	public void setAllocatePseudoTTY() {
		new CheckBox("Allocate pseudo-TTY from Console (-t)").click();
	}

	public void setAutomaticalyRemove() {
		new CheckBox("Automatically remove the container when it exits (--rm)").click();
	}

}
