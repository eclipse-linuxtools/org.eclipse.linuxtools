/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.ui.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.vagrant.ui.wizards.WizardMessages;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.connector.ssh.launcher.SshLauncherDelegate;

public class SSHVMCommandHandler extends BaseVMCommandHandler {

	private static final String JSCH_ID = "org.eclipse.jsch.core";
	private static final String KEY = "PRIVATEKEY";
	@Override
	void executeInJob(final IVagrantVM vm, IProgressMonitor monitor) {
		if (vm.ip() == null || vm.port() == 0 || vm.user() == null) {
			Display.getDefault()
			.syncExec(() -> MessageDialog.openError(Display.getCurrent()
					.getActiveShell(),
					WizardMessages.getString("SSHVMCommandHandler.sshError"), //$NON-NLS-1$
					WizardMessages.getString("SSHVMCommandHandler.invalidCredentials"))); //$NON-NLS-1$
			return;
		}
		// org.eclipse.tm.terminal.connector.ssh.controls.SshWizardConfigurationPanel
		Map<String, Object> properties = new HashMap<>();
		properties.put("ssh.keep_alive", 300);
		properties.put("ip.port", vm.port());
		properties.put("delegateId", "org.eclipse.tm.terminal.connector.ssh.launcher.ssh");
		properties.put("selection", null);
		properties.put("ssh.password", "");
		if (!vm.identityFile().isEmpty()) {
			setupKeyPreferences(vm.identityFile());
		}
		properties.put("tm.terminal.connector.id", "org.eclipse.tm.terminal.connector.ssh.SshConnector");
		properties.put("ip.host", vm.ip());
		properties.put("ssh.user", vm.user());
		properties.put("encoding", null);
		properties.put("timeout", 0);
		SshLauncherDelegate delegate = new SshLauncherDelegate();
		delegate.execute(properties, null);
	}

	private void setupKeyPreferences(String identityFile) {
		// org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY
		String currentKeys = InstanceScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		if (currentKeys.isEmpty()) {
			currentKeys = DefaultScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		}
		InstanceScope.INSTANCE.getNode(JSCH_ID).put(KEY, currentKeys + "," + identityFile);
		VagrantService.getInstance().addToTrackedKeys(identityFile);
	}

	@Override
	String getJobName(final List<IVagrantVM> selectedVMs) {
		return "SSH'ing...";
	}

	@Override
	String getTaskName(final IVagrantVM vm) {
		return "SSH'ing into " + vm.id() + "( " + vm.ip() + ")";
	}
}
