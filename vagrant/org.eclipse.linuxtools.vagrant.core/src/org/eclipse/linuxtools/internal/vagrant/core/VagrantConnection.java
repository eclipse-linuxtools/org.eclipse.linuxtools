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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
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
	private static VagrantConnection client;
	private final Object imageLock = new Object();
	private final Object containerLock = new Object();

	private List<IVagrantVM> vms;
	private boolean containersLoaded = false;
	private List<IVagrantBox> boxes;
	private boolean boxesLoaded = false;
	private Set<String> trackedKeys = new HashSet<>();

	ListenerList vmListeners;
	ListenerList boxListeners;

	private VagrantConnection() {
		// Add the box/vm refresh manager to watch the containers list
		VagrantBoxRefreshManager vbrm = VagrantBoxRefreshManager.getInstance();
		VagrantVMRefreshManager vvrm = VagrantVMRefreshManager.getInstance();
		addBoxListener(vbrm);
		addVMListener(vvrm);
	}

	public static IVagrantConnection getInstance() {
		if (client == null) {
			client = new VagrantConnection();
		}
		return client;
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

		List<String> completed = new ArrayList<String>();
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

		Collections.sort(containers, new Comparator<IVagrantVM>() {
			@Override
			public int compare(IVagrantVM o1, IVagrantVM o2) {
				return o1.name().compareTo(o2.name());
			}
		});

		this.containersLoaded = true;
		synchronized (containerLock) {
			this.vms = containers;
		}
		removeKeysFromInnactiveVMs();
		notifyContainerListeners(this.vms);
	}

	private VagrantVM createVagrantVM(String vmid, String vmDir) {
		List<String> args = new LinkedList<>(
				Arrays.asList(new String[] { "ssh-config" }));
		args.add(vmid);

		List<String> sshConfig = null;

		// Run and handle ssh-config for this vm
		String[] res = call(args.toArray(new String[0]));
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
		res = call(args.toArray(new String[0]));
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
	public Process up(File vagrantDir, String provider) {
		if (provider != null) {
			return rtCall(new String[] { "up", "--provider", provider },
					vagrantDir);
		} else {
			return rtCall(new String[] { "up" }, vagrantDir);
		}
	}

	@Override
	public void addBox(String name, String location) {
		call(new String [] {"--machine-readable", "box", "add", name, location});
	}

	@Override
	public void destroyVM(String id) {
		call(new String[] { "destroy", "-f", id });
	}

	@Override
	public void haltVM(String id) {
		call(new String[] { "--machine-readable", "halt", id });
	}

	@Override
	public void startVM(String id) {
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
		List<String> result = new ArrayList<>();
		try {
			List<String> cmd = new ArrayList<>();
			cmd.add(VG);
			cmd.addAll(Arrays.asList(args));
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]),
					null, vagrantDir);
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

	private static Process rtCall(String[] args, File vagrantDir) {
		try {
			List<String> cmd = new ArrayList<>();
			cmd.add(VG);
			cmd.addAll(Arrays.asList(args));
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]),
					null, vagrantDir);
			return p;
		} catch (IOException e) {
		}
		return null;
	}

}
