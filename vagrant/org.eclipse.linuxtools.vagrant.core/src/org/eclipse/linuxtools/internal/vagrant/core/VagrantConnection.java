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
package org.eclipse.linuxtools.internal.vagrant.core;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.vagrant.core.EnumVMStatus;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantBoxListener;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.IVagrantVMListener;
import org.osgi.framework.Version;

public class VagrantConnection implements IVagrantConnection, Closeable {

	private static final String JSCH_ID = "org.eclipse.jsch.core";
	private static final String KEY = "PRIVATEKEY";
	private static final String VG = "vagrant"; //$NON-NLS-1$
	private final Object imageLock = new Object();
	private final Object containerLock = new Object();

	private List<IVagrantVM> vms;
	private boolean containersLoaded = false;
	private List<IVagrantBox> boxes;
	private boolean boxesLoaded = false;
	private Set<String> trackedKeys = new HashSet<>();

	ListenerList vmListeners;
	ListenerList boxListeners;

	public VagrantConnection() {
		// Add the box/vm refresh manager to watch the containers list
		VagrantBoxRefreshManager vbrm = VagrantBoxRefreshManager.getInstance();
		VagrantVMRefreshManager vvrm = VagrantVMRefreshManager.getInstance();
		addBoxListener(vbrm);
		addVMListener(vvrm);
	}

	@Override
	public void close() {
	}

	@Override
	public void addVMListener(IVagrantVMListener listener) {
		if (vmListeners == null)
			vmListeners = new ListenerList(ListenerList.IDENTITY);
		vmListeners.add(listener);
	}

	@Override
	public void removeVMListener(IVagrantVMListener listener) {
		if (vmListeners != null)
			vmListeners.remove(listener);
	}

	public void notifyContainerListeners(List<IVagrantVM> list) {
		if (vmListeners != null) {
			Object[] listeners = vmListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IVagrantVMListener) listeners[i]).listChanged(this, list);
			}
		}
	}

	@Override
	public void addBoxListener(IVagrantBoxListener listener) {
		if (boxListeners == null)
			boxListeners = new ListenerList(ListenerList.IDENTITY);
		boxListeners.add(listener);
	}

	@Override
	public void removeBoxListener(IVagrantBoxListener listener) {
		if (boxListeners != null)
			boxListeners.remove(listener);
	}

	public void notifyBoxListeners(List<IVagrantBox> list) {
		if (boxListeners != null) {
			Object[] listeners = boxListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IVagrantBoxListener) listeners[i]).listChanged(this, list);
			}
		}
	}

	@Override
	public List<IVagrantVM> getVMs(boolean force) {
		if (force || !isVMsLoaded()) {
			refreshVMs();
		}
		return this.vms;
	}

	protected void refreshVMs() {
		String[] res = call(new String[] { "global-status" });
		List<String> vmIDs = new LinkedList<>();
		List<String> vmDirs = new LinkedList<>();
		final List<IVagrantVM> containers = new LinkedList<>();
		for (int i = 0; i < res.length; i++) {
			String[] items = res[i].split("\\s+");
			if (items.length == 5 && i >= 2) {
				vmIDs.add(items[0]);
				vmDirs.add(items[items.length - 1]);
			}
		}

		Collections.sort(containers,
				(o1, o2) -> o1.name().compareTo(o2.name()));

		List<String> completed = new ArrayList<>();
		if (!vmIDs.isEmpty()) {
			Iterator<String> vmIterator = vmIDs.iterator();
			Iterator<String> vmDirIterator = vmDirs.iterator();
			while (vmIterator.hasNext()) {
				final String vmid = vmIterator.next();
				final String vmDir = vmDirIterator.next();
				new Thread("Checking ssh-config for vm " + vmid) {
					@Override
					public void run() {
						try {
							VagrantVM ret = createVagrantVM(vmid, vmDir);
							if (ret != null) {
								containers.add(ret);
							}
						} finally {
							// Ensure this gets called no matter what
							completed.add(vmid);
						}
					}
				}.start();
			}
		}

		// Should set a max timeout?

		// Wait for completed to have same count as vmIDs
		while (completed.size() < vmIDs.size()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				// Ignore
			}
		}

		this.containersLoaded = true;
		synchronized (containerLock) {
			this.vms = containers;
		}
		removeKeysFromInnactiveVMs();
		notifyContainerListeners(this.vms);
	}

	private VagrantVM createVagrantVM(String vmid, String vmDir) {

		Map<String, String> env = EnvironmentsManager.getSingleton()
				.getEnvironment(new File(vmDir));

		List<String> args = new LinkedList<>(
				Arrays.asList(new String[] { "ssh-config" }));
		args.add(vmid);

		List<String> sshConfig = null;

		// Run and handle ssh-config for this vm
		String[] res = call(args.toArray(new String[0]), new File(vmDir), env);
		for (int i = 0; i < res.length; i++) {
			String[] items = res[i].trim().split(" ");
			if (items[0].equals("HostName")) {
				List<String> tmp = new ArrayList<>();
				tmp.add(items[1]);
				sshConfig = tmp;
			} else if (items[0].equals("User") || items[0].equals("Port")
					|| items[0].equals("IdentityFile")) {
				sshConfig.add(items[1]);
			}
		}

		// Run and handle status for this vm
		VagrantVM vm = null;
		args = new LinkedList<>(
				Arrays.asList(new String[] { "--machine-readable", "status" }));
		args.add(vmid);
		res = call(args.toArray(new String[0]), new File(vmDir), env);
		String name, provider, state, state_desc;
		name = provider = state = state_desc = "";
		for (int i = 0; i < res.length; i++) {
			String[] items = res[i].split(",");
			if (items[2].equals("provider-name")) {
				name = items[1];
				provider = items[3];
			} else if (items[2].equals("state")) {
				state = items[3];
			} else if (items[2].equals("state-human-long")) {
				state_desc = items[3];
				if (sshConfig == null || sshConfig.isEmpty()) {
					// VM exists but ssh is not configured
					vm = new VagrantVM(vmid, name, provider, state, state_desc,
							new File(vmDir), null, null, 0, null);
				} else {
					vm = new VagrantVM(vmid, name, provider, state, state_desc,
							new File(vmDir), sshConfig.get(0), sshConfig.get(1),
							Integer.parseInt(sshConfig.get(2)),
							sshConfig.get(3));
				}
			}
		}
		return vm;
	}

	/**
	 * If a key stored in preferences comes from a non-running VM
	 * or came from a Vagrant VM (tracked) but is not longer
	 * associated with one, then it is safe to remove it.
	 */
	private void removeKeysFromInnactiveVMs() {
		// org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY
		String newKeys = "";
		String keys = InstanceScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		if (keys.isEmpty()) {
			keys = DefaultScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		}
		boolean vmFound = false;
		for (String key : keys.split(",")) {
			for (IVagrantVM vm : vms) {
				if (key.equals(vm.identityFile())) {
					vmFound = true;
					if (!EnumVMStatus.RUNNING.equals(EnumVMStatus.fromStatusMessage(vm.state()))) {
						newKeys = keys.replaceAll("(,)?" + key + "(,)?", "");
						removeFromTrackedKeys(key);
						break;
					}
				}
			}
			if (!vmFound && isTrackedKey(key)) {
				newKeys = keys.replaceAll("(,)?" + key + "(,)?", "");
				removeFromTrackedKeys(key);
			}
		}
		if (!newKeys.isEmpty() && !newKeys.equals(keys)) {
			InstanceScope.INSTANCE.getNode(JSCH_ID).put(KEY, newKeys);
		}
	}

	@Override
	public void addToTrackedKeys(String key) {
		trackedKeys.add(key);
	}

	private void removeFromTrackedKeys(String key) {
		trackedKeys.remove(key);
	}

	private boolean isTrackedKey(String key) {
		return trackedKeys.contains(key);
	}

	@Override
	public List<IVagrantVM> getVMs() {
		return getVMs(false);
	}

	@Override
	public boolean isVMsLoaded() {
		return containersLoaded;
	}

	@Override
	public boolean isBoxesLoaded() {
		return boxesLoaded;
	}

	@Override
	public List<IVagrantBox> getBoxes() {
		return getBoxes(false);
	}

	@Override
	public List<IVagrantBox> getBoxes(boolean force) {
		if (force || !isBoxesLoaded()) {
			String [] res = call(new String[] { "--machine-readable", "box", "list" });
			List<IVagrantBox> images = new LinkedList<>();
			String name = "";
			String provider = "";
			String version = "0";
			for (int i = 0; i < res.length; i++) {
				String[] items = res[i].split(",");
				if (items[2].equals("box-name")) {
					name = items[3];
				} else if (items[2].equals("box-provider")) {
					provider = items[3];
				} else if (items[2].equals("box-version")) {
					version = items[3];
					images.add(new VagrantBox(name, provider, Version.parseVersion(version)));
					name = "";
					provider = "";
					version = "0";
				}
			}
			this.boxesLoaded = true;
			synchronized (imageLock) {
				this.boxes = images;
			}
			notifyBoxListeners(this.boxes);
		}
		return this.boxes;
	}

	@Override
	public void init(File vagrantDir) {
		call(new String [] {"init"}, vagrantDir);
	}

	@Override
	public void up(File vagrantDir, String provider) {
		up(vagrantDir, provider,
				EnvironmentsManager.getSingleton().getEnvironment(vagrantDir));
	}


	private void up(File vagrantDir, String provider,
			Map<String, String> environment) {
		if (provider != null) {
			rtCall(new String[] { "up", "--provider", provider },
					vagrantDir, environment);
		} else {
			rtCall(new String[] { "up" }, vagrantDir, environment);
		}
	}

	@Override
	public void addBox(String name, String location) {
		call(new String [] {"--machine-readable", "box", "add", name, location});
	}

	@Override
	public void destroyVM(IVagrantVM vm) {
		call(new String[] { "destroy", "-f", vm.id() }, vm.directory(),
				EnvironmentsManager.getSingleton()
						.getEnvironment(vm.directory()));
	}

	@Override
	public void haltVM(IVagrantVM vm) {
		call(new String[] { "--machine-readable", "halt", vm.id() },
				vm.directory(), EnvironmentsManager.getSingleton()
						.getEnvironment(vm.directory()));
	}

	@Override
	public void startVM(IVagrantVM vm) {
		up(vm.directory(), vm.provider(), EnvironmentsManager.getSingleton()
				.getEnvironment(vm.directory()));
	}

	@Override
	public void removeBox(String name) {
		call(new String[] { "--machine-readable", "box", "remove", name });
	}

	@Override
	public String getName() {
		return "System Vagrant Connection";
	}

	private static String[] call(String[] args) {
		return call(args, null);
	}

	private static String[] call(String[] args, File vagrantDir) {
		return call(args, vagrantDir, null);
	}

	private static String[] call(String[] args, File vagrantDir,
			Map<String, String> env) {
		String[] envp = (env == null ? null
				: EnvironmentsManager.convertEnvironment(env));

		List<String> result = new ArrayList<>();
		try {
			List<String> cmd = new ArrayList<>();
			cmd.add(VG);
			cmd.addAll(Arrays.asList(args));
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]),
					envp, vagrantDir);
			BufferedReader buff = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			if (p.waitFor() == 0) {
				String line;
				while ((line = buff.readLine()) != null) {
					result.add(line);
				}
			} else {
				return new String[0];
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return result.toArray(new String[0]);
	}

	private static void rtCall(String[] args, File vagrantDir,
			Map<String, String> environment) {

		// org.eclipse.core.externaltools.internal.IExternalToolConstants
		final String EXTERNAL_TOOLS = "org.eclipse.ui.externaltools.ProgramLaunchConfigurationType"; //$NON-NLS-1$
		final String UI_PLUGIN_ID = "org.eclipse.ui.externaltools"; //$NON-NLS-1$
		final String ATTR_LOCATION = UI_PLUGIN_ID + ".ATTR_LOCATION"; //$NON-NLS-1$
		final String ATTR_TOOL_ARGUMENTS = UI_PLUGIN_ID + ".ATTR_TOOL_ARGUMENTS"; //$NON-NLS-1$
		final String ATTR_WORKING_DIRECTORY = UI_PLUGIN_ID + ".ATTR_WORKING_DIRECTORY"; //$NON-NLS-1$

		String arguments = Arrays.asList(args).stream().map(u -> u.toString())
				.collect(Collectors.joining(" ")); //$NON-NLS-1$
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(EXTERNAL_TOOLS);
		try {
			// TODO: worth handling 'vagrant' (not on PATH) as an alias ?
			String vagrantPath = findVagrantPath();
			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, VG);
			wc.setAttribute(ATTR_LOCATION, vagrantPath);
			wc.setAttribute(ATTR_TOOL_ARGUMENTS, arguments);
			wc.setAttribute(ATTR_WORKING_DIRECTORY, vagrantDir.getAbsolutePath());
			wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
					environment);
			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		} catch (CoreException e1) {
			Activator.log(e1);
		}
	}

	/**
	 * Find the location of 'vagrant' on the system by looking under the
	 * environment PATH.
	 *
	 * @return The location of 'vagrant' as a string if it exists under
	 * the PATH, or null if it could not be found.
	 */
	public static String findVagrantPath() {
		final String envPath = System.getenv("PATH"); //$NON-NLS-1$
		if (envPath != null) {
			for (String dir : envPath.split(File.pathSeparator)) {
				Path vgPath = Paths.get(dir, VG);
				if (vgPath.toFile().exists()) {
					return vgPath.toString();
				}
			}
		}
		return null;
	}

}
