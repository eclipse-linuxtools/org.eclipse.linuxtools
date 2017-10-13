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
import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class ImageRunSelectionPage extends WizardPage {

	public ImageRunSelectionPage(ReferencedComposite referencedComposite) {
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

	public void next() {
		new NextButton().click();
	}

	public void setContainerName(String name) {
		new LabeledText("Container Name:").setText(name);
	}

	public void setEntrypoint(String Entrypoint) {
		new LabeledText("Entrypoint:").setText(Entrypoint);
	}

	public void setCommand(String command) {
		new LabeledText("Command:").setText(command);
	}

	public void setPublishAllExposedPorts(boolean checked) {
		new CheckBox("Publish all exposed ports to random ports on the host interfaces").toggle(checked);
	}

	public void setPublishAllExposedPorts() {
		setPublishAllExposedPorts(true);
	}

	public void setKeepSTDINOpen(boolean checked) {
		new CheckBox("Keep STDIN open to Console even if not attached (-i)").toggle(checked);
	}

	public void setKeepSTDINOpen() {
		setKeepSTDINOpen(true);
	}

	public void setAllocatePseudoTTY(boolean checked) {
		new CheckBox("Allocate pseudo-TTY from Console (-t)").toggle(checked);
	}

	public void setAllocatePseudoTTY() {
		setAllocatePseudoTTY(true);
	}

	public void setAutomaticalyRemove(boolean checked) {
		new CheckBox("Automatically remove the container when it exits (--rm)").toggle(checked);
	}

	public void setAutomaticalyRemove() {
		setAutomaticalyRemove(true);
	}

	public void setGiveExtendedPrivileges(boolean checked) {
		new CheckBox("Give extended privileges to this container (--privileged)").toggle(checked);
	}

	public void setGiveExtendedPrivileges() {
		setGiveExtendedPrivileges(true);
	}
	
	public void setUnconfined(boolean checked) {
		new CheckBox("Use unconfined seccomp profile (--securityOpt seccomp=unconfined)").toggle(checked);
	}
	
	public void setUnconfined() {
		setUnconfined(true);
	}

	public void setBasicSecurity(boolean checked) {
		new CheckBox("Add basic security (--readonly --tmpfs /run --tmpfs /tmp --cap-drop=all)").toggle(checked);
	}
	
	public void setBasicSecurity() {
		setBasicSecurity(true);
	}

	public void addExposedPort(String containerPort, String hostAddress, String hostPort) {
		new PushButton(0, new WithTextMatcher("Add...")).click();
		new DefaultShell("Exposing a Container Port");
		new LabeledText("Container port:").setText(containerPort);
		new LabeledText("Host address:").setText(hostAddress);
		new LabeledText("Host port:").setText(hostPort);
		new OkButton().click();
	}

	public void addLinkToContainer(String containerName, String alias) {
		new PushButton(1, new WithTextMatcher("Add...")).click();
		new DefaultShell("Container Linking");
		new LabeledCombo("Container:").setText(containerName);
		new LabeledText("Alias:").setText(alias);
		new OkButton().click();
	}

}
