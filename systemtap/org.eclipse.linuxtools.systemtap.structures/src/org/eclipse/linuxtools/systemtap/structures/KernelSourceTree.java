/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.systemtap.structures.Localization;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

public class KernelSourceTree {
	public TreeNode getTree() {
		return kernelTree;
	}

	/**
	 * Builds the kernel tree from file parameter direct and stores the excluded string array.
	 *
	 * @param direct The file to include into the tree.
	 * @param excluded The string array to store as excluded.
	 */
	public void buildKernelTree(String direct, String[] excluded) {
		if (direct == null || direct.isEmpty()){
			kernelTree = null;
			return;
		}
		try {
			URI locationURI = new URI(direct);
			IRemoteFileProxy proxy = RemoteProxyManager.getInstance().getFileProxy(locationURI);
			this.buildKernelTree(locationURI, excluded, proxy, null);
		} catch (URISyntaxException e) {
			kernelTree = null;
		} catch (CoreException e) {
			kernelTree = null;
		}
	}

	/**
	 * Builds the kernel tree from file parameter direct and stores the excluded string array.
	 *
	 * @param direct The file to include into the tree.
	 * @param excluded The string array to store as excluded.
	 * @param proxy The proxy to be used to get the remote files
	 * @param monitor a progress monitor for this operation. Can be null.
	 * @throws CoreException
	 *
	 * @since 1.1
	 */
	public void buildKernelTree(URI locationURI, String[] excluded, IRemoteFileProxy proxy, IProgressMonitor monitor) throws CoreException {
		if (excluded != null){
			this.excluded = Arrays.copyOf(excluded, excluded.length);
		}
		IFileStore fs = proxy.getResource(locationURI.getPath());
		if (fs == null) {
			kernelTree = null;
		} else {
			kernelTree = new TreeNode(fs, fs.getName(), false);
			addLevel(kernelTree, monitor);
		}
	}

	/**
	 * Adds a level to the kernel source tree.
	 *
	 * @param top The top of the tree to add a level to.
	 * @throws CoreException
	 */
	private void addLevel(TreeNode top, IProgressMonitor monitor) throws CoreException {
		boolean add;
		TreeNode current;
		IFileStore fs = (IFileStore)top.getData();
		IFileStore[] fsList = null;
		fsList = fs.childStores(EFS.NONE, new NullProgressMonitor());
		if (monitor != null) {
			monitor.beginTask(Localization.getString("ReadingKernelSourceTree"), 100); //$NON-NLS-1$
		}
		CCodeFileFilter filter = new CCodeFileFilter();
		for (IFileStore fsChildren : fsList) {
			add = true;
			boolean isDir = fsChildren.fetchInfo().isDirectory();
			if (!filter.accept(fsChildren.getName(), isDir)) {
				continue;
			}

			for(int j=0; j<excluded.length; j++) {
				if(fsChildren.getName().equals(excluded[j].substring(0, excluded[j].length()-1)) && isDir) {
					add = false;
					break;
				}
			}
			if(add) {
				current = new TreeNode(fsChildren, fsChildren.getName(), !isDir);
				top.add(current);
				if(isDir) {
					addLevel(top.getChildAt(top.getChildCount()-1), null);
					if(0 == current.getChildCount()) {
						top.remove(top.getChildCount()-1);
					}
				}
			}
			if (monitor != null) {
				monitor.worked(1);
			}
		}
		top.sortLevel();
	}

	public void dispose() {
		kernelTree = null;
	}

	private TreeNode kernelTree;
	private String[] excluded;
}
