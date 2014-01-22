/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ssh.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Vector;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import org.eclipse.linuxtools.ssh.proxy.Activator;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SSHFileStore extends FileStore {
	private URI uri;
	private Path path;
	private SSHFileProxy proxy;
	private static final int USER_READ = 256;
	private static final int USER_WRITE = 128;
	private static final int USER_EXEC = 64;
	private static final int GROUP_READ = 32;
	private static final int GROUP_WRITE = 16;
	private static final int GROUP_EXEC = 8;
	private static final int OTHER_READ = 4;
	private static final int OTHER_WRITE = 2;
	private static final int OTHER_EXEC = 1;

	public SSHFileStore(URI uri, SSHFileProxy proxy) {
		this.uri = uri;
		this.proxy = proxy;
		this.path = new Path(uri.getPath());
	}

	@Override
	public String[] childNames(int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask(Messages.SSHFileStore_childNamesMonitor, 100);
			ChannelSftp channel = proxy.getChannelSftp();
			monitor.worked(25);
			Vector<?> v = channel.ls(uri.getPath());
			monitor.worked(50);
			LinkedList<String> childs = new LinkedList<>();

			boolean isDir = false;
			for (int i=0; i < v.size(); i++) {
				ChannelSftp.LsEntry entry  = (ChannelSftp.LsEntry) v.get(i);
				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) //$NON-NLS-1$ //$NON-NLS-2$
					childs.add(entry.getFilename());
				else
					isDir = true;
			}
			if (!isDir)
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					MessageFormat.format(Messages.SSHFileStore_childNamesFailedDirectory, getName())));

			monitor.worked(100);
			monitor.done();
			return childs.toArray(new String[0]);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_childNamesFailed + e.getMessage()));

		}
	}

	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask(Messages.SSHFileStore_childInfoMonitor, 100);
			ChannelSftp channel = proxy.getChannelSftp();
			monitor.worked(25);
			Vector<?> v = channel.ls(uri.getPath());
			monitor.worked(50);
			LinkedList<IFileInfo> childs = new LinkedList<>();

			boolean isDir = false;
			for (int i=0; i < v.size(); i++) {
				ChannelSftp.LsEntry entry  = (ChannelSftp.LsEntry) v.get(i);
				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) //$NON-NLS-1$ //$NON-NLS-2$
					childs.add(createFileInfo(entry.getAttrs()));
				else
					isDir = true;
			}
			if (!isDir)
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					MessageFormat.format(Messages.SSHFileStore_childInfoFailedDirectory, getName())));

			monitor.worked(100);
			monitor.done();
			return childs.toArray(new IFileInfo[0]);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_childInfoFailed + e.getMessage()));
		}
	}

	@Override
	public IFileInfo fetchInfo() {
		try {
			return fetchInfo(EFS.NONE, new NullProgressMonitor());
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public IFileStore[] childStores(int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask(Messages.SSHFileStore_childStoresMonitor, 100);
			ChannelSftp channel = proxy.getChannelSftp();
			monitor.worked(25);
			Vector<?> v = channel.ls(uri.getPath());
			monitor.worked(50);
			LinkedList<IFileStore> childs = new LinkedList<>();

			boolean isDir = false;
			for (int i=0; i < v.size(); i++) {
				ChannelSftp.LsEntry entry  = (ChannelSftp.LsEntry) v.get(i);
				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) //$NON-NLS-1$ //$NON-NLS-2$
					childs.add(createFileStore(path.append(entry.getFilename()).toString()));
				else
					isDir = true;
			}
			if (!isDir)
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					MessageFormat.format(Messages.SSHFileStore_childStoresFailedDirectory, getName())));

			monitor.worked(100);
			monitor.done();
			return childs.toArray(new IFileStore[0]);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_childStoresFailed + e.getMessage()));
		}
	}

	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask(Messages.SSHFileStore_rmMonitor, 100);
			ChannelSftp channel = proxy.getChannelSftp();
			monitor.worked(25);

			if (channel.lstat(uri.getPath()).isDir())
				channel.rmdir(uri.getPath());
			else
				channel.rm(uri.getPath());
			monitor.worked(100);
			monitor.done();
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_rmFailed + e.getMessage()));
		}
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask(Messages.SSHFileStore_attrMonitor, 100);
			ChannelSftp channel = proxy.getChannelSftp();
			monitor.worked(25);

			SftpATTRS attrs = channel.stat(uri.getPath());
			monitor.worked(100);
			monitor.done();
			return createFileInfo(attrs);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_attrFailed + e.getMessage()));
		}
	}

	@Override
	public IFileStore getChild(String name) {
		String strPath = path.append(name).toString();
		return createFileStore(strPath);
	}

	@Override
	public String getName() {
		String name = path.lastSegment();
		if (name == null)
			name = ""; //$NON-NLS-1$
		return name;
	}

	@Override
	public IFileStore getParent() {
		if (path.isRoot())
			return null;
		String strPath = path.removeLastSegments(1).toString();
		return createFileStore(strPath);
	}

	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask(Messages.SSHFileStore_mkdirMonitor, 100);
		ChannelSftp channel = proxy.getChannelSftp();
		monitor.worked(25);

		IPath new_path = Path.ROOT;
		if ((options & EFS.SHALLOW) == 0) {
			for (String segment : path.segments()) {
				new_path = new_path.append(segment);
				try {
					channel.stat(new_path.toString());
				} catch (SftpException e) {
					//Path doesn't exist
					createDir(channel, new_path.toString());
				}
			}
		} else
			createDir(channel, uri.getPath());

		monitor.worked(100);
		monitor.done();
		return this;
	}

	private void createDir(ChannelSftp channel, String dir) throws CoreException {
		try {
			channel.mkdir(dir);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_mkdirFailed + e.getMessage()));
		}
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			ChannelSftp channel = proxy.getChannelSftp();
			return channel.get(uri.getPath(), new ProgressMonitor(monitor, Messages.SSHFileStore_getInputStreamMonitor));
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_getInputStreamFailed + e.getMessage()));
		}
	}

	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			ChannelSftp channel = proxy.getChannelSftp();
			int mode = ChannelSftp.OVERWRITE;
			if ((options & EFS.APPEND) != 0)
				mode = ChannelSftp.APPEND;
			return channel.put(uri.getPath(), new ProgressMonitor(monitor, Messages.SSHFileStore_getOutputStreamMonitor), mode);
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_getOutputStreamFailed + e.getMessage()));
		}

	}

	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask(Messages.SSHFileStore_putInfoMonitor, 100);
			ChannelSftp channel = proxy.getChannelSftp();
			monitor.worked(25);
			SftpATTRS attrs = channel.stat(uri.getPath());
			updateSftpATTRS(attrs, info);
			channel.setStat(uri.getPath(), attrs);
			monitor.worked(100);
			monitor.done();
		} catch (SftpException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SSHFileStore_putInfoFailed + e.getMessage()));
		}
	}

	@Override
	public URI toURI() {
		return this.uri;
	}

	private IFileStore createFileStore(String newPath) {
		try {
			return new SSHFileStore(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
											newPath, uri.getQuery(), uri.getFragment()), this.proxy);
		} catch (URISyntaxException e) {
			//This is not suppose to happen
			return null;
		}
	}

	private IFileInfo createFileInfo(SftpATTRS attrs) {
		FileInfo f = new FileInfo();
		f.setExists(true);
		f.setLastModified(attrs.getMTime());
		f.setLength(attrs.getSize());
		f.setName(getName());
		f.setDirectory(attrs.isDir());

		int p = attrs.getPermissions();
		if ((p & USER_READ) != 0)
			f.setAttribute(EFS.ATTRIBUTE_OWNER_READ, true);
		if ((p & USER_WRITE) != 0)
			f.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE, true);
		if ((p & USER_EXEC) != 0)
			f.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, true);

		if ((p & GROUP_READ) != 0)
			f.setAttribute(EFS.ATTRIBUTE_GROUP_READ, true);
		if ((p & GROUP_WRITE) != 0)
			f.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, true);
		if ((p & GROUP_EXEC) != 0)
			f.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, true);

		if ((p & OTHER_READ) != 0)
			f.setAttribute(EFS.ATTRIBUTE_OTHER_READ, true);
		if ((p & OTHER_WRITE) != 0)
			f.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, true);
		if ((p & OTHER_EXEC) != 0)
			f.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, true);

		return f;
	}

	private void updateSftpATTRS(SftpATTRS attrs, IFileInfo f) {
		int p = 0;
		if (f.getAttribute(EFS.ATTRIBUTE_OWNER_READ))
			p = p | USER_READ;
		if (f.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE))
			p = p | USER_WRITE;
		if (f.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE))
			p = p | USER_EXEC;

		if (f.getAttribute(EFS.ATTRIBUTE_GROUP_READ))
			p = p | GROUP_READ;
		if (f.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE))
			p = p | GROUP_WRITE;
		if (f.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE))
			p = p | GROUP_EXEC;

		if (f.getAttribute(EFS.ATTRIBUTE_OTHER_READ))
			p = p | OTHER_READ;
		if (f.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE))
			p = p | OTHER_WRITE;
		if (f.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE))
			p = p | OTHER_EXEC;
	}
}
