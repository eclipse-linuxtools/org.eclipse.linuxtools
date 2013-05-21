/*******************************************************************************
 * Copyright (c) 2011 IBM corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corp. - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

/**
 * This package layers on top of the IRemoteProxyManager and its related classes.
 * It provides some convenient methods that can be used for writing launcher tabs and
 * launch delegates.
 *
 * @author Corey Ashford
 * @since 1.1
 *
 */
public class RemoteConnection {

	private IRemoteProxyManager rmtProxyMgr;
	private IRemoteCommandLauncher rmtCmdLauncher;
	private IRemoteFileProxy rmtFileProxy;

	public RemoteConnection(URI uri)
			throws RemoteConnectionException {
		try {
			rmtProxyMgr = RemoteProxyManager.getInstance();
			rmtCmdLauncher = rmtProxyMgr.getLauncher(uri);
			rmtFileProxy = rmtProxyMgr.getFileProxy(uri);
		} catch (CoreException e) {
			throw new RemoteConnectionException(
					RemoteMessages.RemoteConnection_failed, e);
		}
	}

	/**
	 * Copy a data from a path (can be a file or directory) from the remote host
	 * to the local host.
	 *
	 * @param remotePath
	 * @param localPath
	 * @throws CoreException
	 */
	private void copyFileFromRemoteHost(String remotePath, String localPath,
			IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IFileSystem localFS = EFS.getLocalFileSystem();
			IFileStore localFile = localFS.getStore(Path.fromOSString(localPath));
			IFileStore rmtFile = rmtFileProxy.getResource(remotePath);
			rmtFile.copy(localFile, EFS.OVERWRITE, progress);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/***
	 * Upload files to remote target directory. This method is recursive. If a
	 * local directory is specified as the input, then all folders and files are
	 * uploaded to the remote target directory. The remote target directory must
	 * exist prior to calling this method or else failure will occur.
	 *
	 * A RemoteConnectionException is thrown on any failure condition.
	 *
	 * @param localPath
	 *            - the local path to file
	 * @param remotePath
	 *            - the remote path
	 * @param monitor
	 *            - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void upload(IPath localPath, IPath remotePath,
			IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			copyFileToRemoteHost(localPath.toOSString(),
					remotePath.toOSString(), monitor);
		} catch (CoreException e) {
			throw new RemoteConnectionException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Create a folder on the remote system. A RemoteConnectionException is
	 * thrown if any failure occurs.
	 *
	 * @param remoteFolderPath
	 *            - path of the remote folder to create
	 * @param monitor
	 *            - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void createFolder(IPath remoteFolderPath, IProgressMonitor monitor)
			throws RemoteConnectionException {
		IFileStore remoteFolder = rmtFileProxy.getResource(remoteFolderPath.toString());
		try {
			remoteFolder.mkdir(0, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new RemoteConnectionException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Break up a buffer from ConsoleOutputStream and return it as an array
	 * of lines
	 * @param buffer typically from ConsoleOutputStream object's readBuffer() method
	 * @return array of lines as Strings with no line terminators
	 */
	public static String[] getLines(String buffer) /* throws RemoteConnectionException */ {
		// Count the number of newlines in the buffer.
		int numLines = 0;

		for (char c : buffer.toCharArray()) {
			if (c == '\n') {
				numLines++;
			}
		}
		String lines[] = new String[numLines];

		int line = 0;
		int startOfString = 0;
		for (int endOfString = 0; endOfString < buffer.length(); endOfString++) {
			if (buffer.charAt(endOfString) == '\n') {
				lines[line++] = new String(buffer.toCharArray(), startOfString, endOfString
						- startOfString);
				startOfString = endOfString + 1;
			}
		}
		return lines;
	}

	/**
	 * Remote delete function. This method is recursive. If a remote directory
	 * is specified, the remote directory and all its contents are removed. A
	 * RemoteConnectionException is thrown if failure occurs for any reason.
	 *
	 * @param remotePath
	 *            - the remote path of the file or folder to be deleted
	 * @param monitor
	 *            - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void delete(IPath remotePath, IProgressMonitor monitor)
			throws RemoteConnectionException {
		try {
			IFileStore remoteFile = rmtFileProxy.getResource(
					remotePath.toString());
			remoteFile.delete(0, monitor);
		} catch (CoreException e) {
			throw new RemoteConnectionException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Download a file or folder from a remote system. The method is recursive.
	 * If a remote folder is specified, all the contents of the folder are
	 * downloaded, including folders, and placed under the directory specified
	 * by the localPath variable. It is assumed that any remote non-binary file
	 * is UTF-8. A RemoteConnectionException is thrown if any failure occurs.
	 *
	 * @param remotePath
	 *            - path to remote file or folder
	 * @param localPath
	 *            - local directory target
	 * @param monitor
	 *            - progress monitor
	 * @throws RemoteConnectionException
	 */
	public void download(IPath remotePath, IPath localPath,
			IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			copyFileFromRemoteHost(remotePath.toString(), localPath.toString(),
					monitor);
		} catch (CoreException e) {
			throw new RemoteConnectionException(e.getLocalizedMessage(), e);
		}
	}

	private void copyFileToRemoteHost(String localPath, String remotePath,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IFileSystem localFS = EFS.getLocalFileSystem();
			IFileStore localFile = localFS.getStore(Path.fromOSString(localPath));
			IFileStore rmtFile = rmtFileProxy.getResource(remotePath);
			localFile.copy(rmtFile, EFS.OVERWRITE, progress);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private static final String ENV_CMD = "/bin/env"; //$NON-NLS-1$
	private static final String WHICH_CMD = "which"; //$NON-NLS-1$
	private static final String PATH_ENV_VAR = "PATH"; //$NON-NLS-1$
	private static final String SEPARATOR = ":"; //$NON-NLS-1$

	/**
	 * Returns the current environment from the remote host.  This method returns
	 * the environment variables as a map to make lookups and replacements simpler.
	 * @return Map&lt;String,String&gt; with the environment variable names as keys.
	 * @throws CoreException
	 */
	public Map<String,String> getEnv() throws CoreException {
		IPath envPath = Path.fromOSString(ENV_CMD);
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		String empty[] = new String[0];

		rmtCmdLauncher.execute(envPath, empty, empty, null, new NullProgressMonitor());
		rmtCmdLauncher.waitAndRead(stdout, stderr, new NullProgressMonitor());
		Map<String,String> env = new HashMap<String, String>();
		String envLines[] = getLines(stdout.toString());
		// Skip the first line, which is just env command being issued
		for (int idx = 1; idx < envLines.length; idx++) {
			String keyAndVal[] = envLines[idx].split("=", 2);
			// If there's a full "<env_var>=<value>|<null>" expression, add this var to the map
			// Note: <value> may be an empty string.
			if (keyAndVal.length == 2)
				env.put(keyAndVal[0], keyAndVal[1]);
			else if (keyAndVal.length == 1)
				env.put(keyAndVal[0], null);
		}
		return env;
	}

	/**
	 * Translates an environment variable map back into an array usable by
	 * IRemoteCommandLauncher.execute().
	 * @param envMap environment variable map
	 * @return array of Strings usable in IRemoteCommandLauncher.execute()
	 */
	public static String[] envMapToEnvArray(Map<String, String> envMap) {
		String envArray[] = new String[envMap.size()];
		int idx = 0;
		for (Map.Entry<String, String> entry : envMap.entrySet()) {
			envArray[idx++] = entry.getKey() + "=" + entry.getValue();
		}
		return envArray;
	}

	/**
	 * Run 'which <b>command</b>' on the remote machine to find where the executable resides.
	 * Prepend the $PATH variable with the value of <b>toolsPath</b> so that <i>which</i> may find a
	 * particular version of the command, if it exists.
	 * @param command the command or tool to locate on the remote system
	 * @param toolsPath contains one or more colon-separated paths in which to search for
	 * for the command, in addition to the default locations in $PATH.
	 * @return location of command, if found.
	 * @throws CoreException
	 */
	public IPath whichCommand(String command, String toolsPath) throws CoreException {
		String args[] = new String[1];
		Map<String,String> envMap = getEnv();

		IPath whichPath = Path.fromOSString(WHICH_CMD);
		args[0] = command;

		if (envMap.containsKey(PATH_ENV_VAR)) {
			String pathVal = envMap.get(PATH_ENV_VAR);
			envMap.put(PATH_ENV_VAR, toolsPath + SEPARATOR + pathVal);
		} else {
			envMap.put(PATH_ENV_VAR, toolsPath);
		}
		String envArray[] = envMapToEnvArray(envMap);

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		rmtCmdLauncher.execute(whichPath, args, envArray, null, new NullProgressMonitor());
		rmtCmdLauncher.waitAndRead(stdout, stderr, new NullProgressMonitor());
		String outputLines[] = getLines(stdout.toString());
		// The first line of the read buffer is the command that was executed, in this case
		// "which <command>", so use the second line, index=1
		return Path.fromOSString(outputLines[1]);
	}

	public IRemoteCommandLauncher getRmtCmdLauncher() {
		return rmtCmdLauncher;
	}

	public IRemoteFileProxy getRmtFileProxy() {
		return rmtFileProxy;
	}

}
