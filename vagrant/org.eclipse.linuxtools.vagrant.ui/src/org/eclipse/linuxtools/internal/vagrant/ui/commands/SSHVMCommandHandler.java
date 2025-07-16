/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jsch.internal.core.JSchCorePlugin;
import org.eclipse.linuxtools.internal.vagrant.core.VagrantConnection;
import org.eclipse.linuxtools.internal.vagrant.ui.Activator;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class SSHVMCommandHandler extends BaseVMCommandHandler {

	private static final String JSCH_ID = "org.eclipse.jsch.core"; //$NON-NLS-1$
	private static final String KEY = "PRIVATEKEY"; //$NON-NLS-1$
	@Override
	void executeInJob(final IVagrantVM vm, IProgressMonitor monitor) {
		if (vm.ip() == null || vm.port() == 0 || vm.user() == null) {
			Display.getDefault()
			.syncExec(() -> MessageDialog.openError(Display.getCurrent()
					.getActiveShell(),
					Messages.SSHVMCommandHandler_failed,
					Messages.SSHVMCommandHandler_bad_credentials));
			return;
		}
		// org.eclipse.terminal.connector.ssh.controls.SshWizardConfigurationPanel
		Map<String, Object> properties = new HashMap<>();
		properties.put("ssh.keep_alive", 300); //$NON-NLS-1$
		properties.put("ip.port", vm.port()); //$NON-NLS-1$
		properties.put("delegateId", //$NON-NLS-1$
				"org.eclipse.terminal.connector.ssh.launcher.ssh"); //$NON-NLS-1$
		properties.put("selection", null); //$NON-NLS-1$
		properties.put("ssh.password", ""); //$NON-NLS-1$ //$NON-NLS-2$
		// TODO: Do this through API later
		VagrantConnection conn = (VagrantConnection) VagrantService.getInstance();
		if (!vm.identityFile().isEmpty()
				&& !conn.isTrackedKey(vm.identityFile())) {
			setupKeyPreferences(vm.identityFile());
		}
		String connectorId = "org.eclipse.terminal.connector.ssh.SshConnector";
		properties.put("tm.terminal.connector.id", //$NON-NLS-1$
				connectorId); // $NON-NLS-1$
		properties.put("ip.host", vm.ip()); //$NON-NLS-1$
		properties.put("ssh.user", vm.user()); //$NON-NLS-1$
		properties.put("encoding", null); //$NON-NLS-1$
		properties.put("timeout", 0); //$NON-NLS-1$
		Activator.getLaunchDelegateManager().findLauncherDelegate(connectorId, true)
				.ifPresent(launcherDelegate -> launcherDelegate.execute(properties, null));
	}

	private void setupKeyPreferences(String identityFile) {
		// org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY
		String currentKeys = InstanceScope.INSTANCE.getNode(JSCH_ID).get(KEY, ""); //$NON-NLS-1$
		if (currentKeys.isEmpty()) {
			currentKeys = DefaultScope.INSTANCE.getNode(JSCH_ID).get(KEY, ""); //$NON-NLS-1$
		}
		InstanceScope.INSTANCE.getNode(JSCH_ID).put(KEY, currentKeys + ',' + identityFile);
		VagrantService.getInstance().addToTrackedKeys(identityFile);
		// Ensure keys get reloaded
		JSchCorePlugin.getPlugin().setNeedToLoadKeys(true);
	}

	@Override
	String getJobName(final List<IVagrantVM> selectedVMs) {
		return Messages.SSHVMCommandHandler_title;
	}

	@Override
	String getTaskName(final IVagrantVM vm) {
		return NLS.bind(Messages.SSHVMCommandHandler_msg, new String [] {vm.id(), vm.ip()});
	}
}
