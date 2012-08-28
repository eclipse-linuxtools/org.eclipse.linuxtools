/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - Initial implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.profiling.launch.remote;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.profiling.launch.remote.RemoteCommandShellOperation;
import org.eclipse.linuxtools.internal.profiling.launch.remote.RemoteLaunchConstants;
import org.eclipse.linuxtools.internal.profiling.launch.remote.RemoteMessages;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;

public class RemoteConnection {
	
	private ILaunchConfiguration config;
	private IHost rseHost;
	private IRemoteFileSubSystem fs;
	private IRemoteCmdSubSystem rcs;
	private String hostName;

	public RemoteConnection(ILaunchConfiguration config) throws RemoteConnectionException {
		this.config = config;
		initialize();
	}

	public RemoteConnection(String hostName) throws RemoteConnectionException {
		this.config = null;
		this.hostName = hostName; 
		initialize();
	}
	
	// To run a remote command we use the RemoteCmdSubSystem, however,
	// we cannot just call runCommand() on the subsystem as there is
	// no way for us to tell when the command has completed.  To handle this,
	// we use a special wrapper class to call the command and use echo statements
	// to tell when the command has completed.
	private static class RemoteCommand extends RemoteCommandShellOperation {

		private boolean finished;
		
		public RemoteCommand(IRemoteCmdSubSystem cmdSubSystem, IRemoteFile pwd) {
			super(cmdSubSystem, pwd);
		}

		public boolean isFinished() {
			return finished;
		}
		
		@Override
		public void handleCommandFinished(String cmd) {
			finished = true;
		}

		@Override
		public void handleOutputChanged(String command, Object output) {
			// do nothing
		}
		
		@Override
		public void finish() {
			super.finish();
			if (_remoteCmdShell != null && _cmdSubSystem != null && !_remoteCmdShell.isActive()) {
				try
				{
					_cmdSubSystem.removeShell(_remoteCmdShell);
				}
				catch (Exception e)
				{
				}
			}

		}
	}
	
	private void initialize() throws RemoteConnectionException {
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e2) {
			throw new RemoteConnectionException(RemoteMessages.RemoteLaunchDelegate_error_interrupted, e2);
		}
		ISystemRegistry registry = SystemStartHere.getSystemRegistry();
		if (hostName == null) {
			if (config != null) {
				try {
					hostName = config.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_HOSTID, RemoteLaunchConstants.DEFAULT_REMOTE_HOSTID);
				} catch (CoreException e1) {
					throw new RemoteConnectionException(RemoteMessages.RemoteLaunchDelegate_error_launch_failed, e1);
				}
			} else {
				throw new RemoteConnectionException(RemoteMessages.RemoteLaunchDelegate_error_no_host, null);
			}
		}

		if (hostName == null)
			throw new RemoteConnectionException(RemoteMessages.RemoteLaunchDelegate_error_no_host, null);
		
		IHost[] hosts = registry.getHosts();
		for (int i = 0; i < hosts.length; ++i) {
			IHost host = hosts[i];
			if (host.getName().equals(hostName)) {
				rseHost = host;
				break;
			}
		}
		
		if (rseHost == null)
			throw new RemoteConnectionException(RemoteMessages.RemoteLaunchDelegate_error_no_host, null);
		
		// Get pertinent remote RSE services
		ISubSystem[] subSystems = registry.getSubSystems(rseHost);
		for (int i = 0; i < subSystems.length; ++i) {
			ISubSystem subSystem = subSystems[i];
			if (subSystem instanceof IRemoteFileSubSystem)
				fs = (IRemoteFileSubSystem)subSystem;
			else if (subSystem instanceof IRemoteCmdSubSystem) {
				rcs = (IRemoteCmdSubSystem)subSystem;
			}
		}
		
		try {
			fs.connect(new NullProgressMonitor(), false);
		} catch (Exception e) {
			throw new RemoteConnectionException(RemoteMessages.RemoteLaunchDelegate_error_no_fs, e);
		}

	}
	
	/**
	 * Return the id for this remote connection.
	 * 
	 * @return id
	 */
	public String getId() {
		return rseHost.getName();
	}
	
	/***
	 * Upload files to remote target directory.  This method is recursive.  If a local directory is
	 * specified as the input, then all folders and files are uploaded to the remote target
	 * directory.  The remote target directory must exist prior to calling this method or 
	 * else failure will occur.
	 * 
	 * A RemoteConnectionException is thrown on any failure condition.
	 * 
	 * @param localPath - the local path to file
	 * @param remotePath - the remote path
	 * @param monitor - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void upload(IPath localPath, IPath remotePath, IProgressMonitor monitor) throws RemoteConnectionException {
		File f = localPath.toFile();
		uploadRecursive(f, remotePath, monitor);
	}

	private void uploadRecursive(File f, IPath remotePath, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			if (f.isDirectory()) {
				File[] children = f.listFiles();
				for (int i = 0; i < children.length; ++i) {
					File child = children[i];
					if (child.isDirectory()) {
					    IPath remoteChildPath = remotePath.append(child.getName());
						createFolder(remoteChildPath, monitor);
						uploadRecursive(child, remoteChildPath, monitor);
					}
				}
			} else {
				fs.upload(f.getAbsolutePath().toString(), "UTF-8", remotePath.append(f.getName()).toString(), "UTF-8", monitor);
			}
		} catch (SystemMessageException e1) {
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}
	}
	

	/**
	 * Create a folder on the remote system.  A RemoteConnectionException is thrown if any failure
	 * occurs.
	 * 
	 * @param remoteFolderPath - path of the remote folder to create
	 * @param monitor - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void createFolder(IPath remoteFolderPath, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			IRemoteFile rf = fs.getRemoteFileObject(remoteFolderPath.toString(), monitor);
			fs.createFolder(rf, new NullProgressMonitor());
		} catch (SystemMessageException e1) {
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}
	}
	
	/**
	 * Run a command on the remote system.
	 * 
	 * @param command - the command to run remotely
	 * @param remoteWorkingDir - the working directory on the remote system
	 * @param monitor - progress monitor
	 * @return An array of String representing the command line output, if any.
	 * @throws RemoteConnectionException
	 */
	public int runCommand(String command, IPath remoteWorkingDir, ArrayList<String> output, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			IRemoteFile rf = fs.getRemoteFileObject(remoteWorkingDir.toString(), monitor);
			RemoteCommand rc = new RemoteCommand(rcs, rf);
			IRemoteCommandShell shell = rc.run();
			rc.sendCommand(command);
			while (!rc.isFinished() && !monitor.isCanceled()) {
				// wait until complete before continuing
				Thread.sleep(100);
			}
			rc.finish(); 
			for (int i = 0; i < shell.getSize(); ++i) {
				Object outputLine = shell.getOutputAt(i);
				output.add(outputLine.toString());
			}
			return rc.getReturnCode();
		} catch (Exception e1) {
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}
	}
	
	/**
	 * Remote delete function.  This method is recursive.  If a remote directory is specified,
	 * the remote directory and all its contents are removed.  A RemoteConnectionException is
	 * thrown if failure occurs for any reason.
	 * 
	 * @param remotePath - the remote path of the file or folder to be deleted
	 * @param monitor - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void delete(IPath remotePath, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			IRemoteFile rf = fs.getRemoteFileObject(remotePath.toString(), monitor);
			deleteRecursive(rf, monitor);
		} catch (SystemMessageException e1) {
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}
	}
	
	private void deleteRecursive(IRemoteFile rf, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			if (rf.isDirectory()) {
				IRemoteFile[] children = fs.list(rf, IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
				for (int i = 0; i < children.length; ++i) {
					IRemoteFile remotelogfile = children[i];
					deleteRecursive(remotelogfile, monitor);
				}
			}
			fs.delete(rf,  monitor);
		} catch (SystemMessageException e1) {
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}
	}

	/**
	 * Download a file or folder from a remote system.  The method is recursive.  If a remote folder
	 * is specified, all the contents of the folder are downloaded, including folders, and placed
	 * under the directory specified by the localPath variable.  It is assumed that any remote non-binary
	 * file is UTF-8.  A RemoteConnectionException is thrown if any failure occurs.
	 * 
	 * @param remotePath - path to remote file or folder
	 * @param localPath - local directory target
	 * @param monitor - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void download(IPath remotePath, IPath localPath, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			IRemoteFile rf = fs.getRemoteFileObject(remotePath.toString(), monitor);
			downloadRecursive(rf, localPath, monitor);
		} catch (SystemMessageException e1) {
			// TODO Auto-generated catch block
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}

	}

	private void downloadRecursive(IRemoteFile rf, IPath localPath, IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			if (rf.isDirectory()) {
				IPath dirPath = localPath;
				IRemoteFile[] children = fs.list(rf, IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
				for (int i = 0; i < children.length; ++i) {
					IRemoteFile remotelogfile = children[i];
					if (remotelogfile.isDirectory()) {
						dirPath = localPath.append(remotelogfile.getName());
						File f = dirPath.toFile();
						f.mkdir();
					}
					downloadRecursive(remotelogfile, dirPath, monitor);
				}
			} else {
				fs.download(rf, localPath.append(rf.getName()).toString(), "UTF-8", monitor);
			}
		} catch (SystemMessageException e1) {
			throw new RemoteConnectionException(e1.getLocalizedMessage(), e1);
		}
	}
	
	
	
}
