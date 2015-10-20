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
import org.eclipse.linuxtools.internal.vagrant.core.VagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.tm.terminal.connector.ssh.launcher.SshLauncherDelegate;

public class SSHVMCommandHandler extends BaseVMCommandHandler {

	@Override
	void executeInJob(final IVagrantVM vm, IProgressMonitor monitor) {
		// org.eclipse.tm.terminal.connector.ssh.controls.SshWizardConfigurationPanel
		Map<String, Object> properties = new HashMap<>();
		properties.put("ssh.keep_alive", 300);
		properties.put("ip.port", 22);
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
		final String JSCH_ID = "org.eclipse.jsch.core";
		// org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY
		final String KEY = "PRIVATEKEY";
		String currentKeys = InstanceScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		if (currentKeys.isEmpty()) {
			currentKeys = DefaultScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		}
		InstanceScope.INSTANCE.getNode(JSCH_ID).put(KEY, currentKeys + "," + identityFile);
		VagrantConnection.getInstance().addToTrackedKeys(identityFile);
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
