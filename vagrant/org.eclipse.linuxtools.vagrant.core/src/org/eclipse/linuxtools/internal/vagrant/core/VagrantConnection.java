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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.eclipse.linuxtools.vagrant.core.VagrantException;
import org.osgi.framework.Version;

public class VagrantConnection implements IVagrantConnection, Closeable {

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
	public void close() throws IOException {
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
			String [] res = call(new String[] { "global-status" });
			List<String> vmIDs = new LinkedList<>();
			List<IVagrantVM> containers = new LinkedList<>();
			Map<String, List<String>> sshConfig = new HashMap<>();
			for (int i = 0; i < res.length; i++) {
				String[] items = res[i].split(" ");
				if (items.length == 6 && i >= 2) {
					vmIDs.add(items[0]);
				}
			}
			List<String> args = new LinkedList<>(Arrays.asList(new String [] { "ssh-config" }));
			args.addAll(vmIDs);
			res = call(args.toArray(new String[0]));
			for (int i = 0; i < res.length; i++) {
				String[] items = res[i].trim().split(" ");
				if (items[0].equals("HostName")) {
					List<String> tmp = new ArrayList<>();
					tmp.add(items[1]);
					sshConfig.put(vmIDs.get(i / 11), tmp);
				} else if (items[0].equals("User")
						|| items[0].equals("IdentityFile")) {
					sshConfig.get(vmIDs.get(i / 11)).add(items[1]);
				}
			}

			args = new LinkedList<>(Arrays.asList(new String [] {"--machine-readable", "status"}));
			args.addAll(vmIDs);
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
					containers.add(new VagrantVM(vmIDs.get((i / 5)), name,
							provider, state, state_desc, new File("/dev/null"),
							sshConfig.get(vmIDs.get((i / 5))).get(0),
							sshConfig.get(vmIDs.get((i / 5))).get(1),
							sshConfig.get(vmIDs.get((i / 5))).get(2)));
				}
			}
			this.containersLoaded = true;
			synchronized (containerLock) {
				this.vms = containers;
			}
			removeKeysFromInnactiveVMs();
			notifyContainerListeners(this.vms);
		}
		return this.vms;
	}

	/**
	 * If a key stored in preferences comes from a non-running VM
	 * or came from a Vagrant VM (tracked) but is not longer
	 * associated with one, then it is safe to remove it.
	 */
	private void removeKeysFromInnactiveVMs() {
		final String JSCH_ID = "org.eclipse.jsch.core";
		// org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY
		final String KEY = "PRIVATEKEY";
		String newKeys = "";
		String keys = InstanceScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		if (keys.isEmpty()) {
			keys = DefaultScope.INSTANCE.getNode(JSCH_ID).get(KEY, "");
		}
		for (String key : keys.split(",")) {
			for (IVagrantVM vm : vms) {
				if (key.equals(vm.identityFile())
						&& !vm.state().equals(EnumVMStatus.RUNNING)) {
					newKeys = keys.replaceAll("(,)?" + key + "(,)?", "");
					removeFromTrackedKeys(key);
					break;
				}
			}
			if (isTrackedKey(key)) {
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
	public void addBox(String name, String location) throws VagrantException, InterruptedException {
		call(new String [] {"--machine-readable", "box", "add", name, location});
	}

	@Override
	public void destroyVM(String id) throws VagrantException, InterruptedException {
		call(new String[] { "--machine-readable", "destroy", id });
	}

	@Override
	public void haltVM(String id) throws VagrantException, InterruptedException {
		call(new String[] { "--machine-readable", "halt", id });
	}

	@Override
	public void startVM(String id)
			throws VagrantException, InterruptedException {
	}

	@Override
	public void removeBox(String name) throws VagrantException, InterruptedException {
		call(new String[] { "--machine-readable", "box", "remove", name });
	}

	@Override
	public String getName() {
		return "System Vagrant Connection";
	}

	private static String[] call(String[] args) {
		List<String> result = new ArrayList<>();
		try {
			List<String> cmd = new ArrayList<>();
			cmd.add(VG);
			cmd.addAll(Arrays.asList(args));
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
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

}
