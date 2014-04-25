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

import java.net.URI;

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

    private IRemoteFileProxy rmtFileProxy;

    public RemoteConnection(URI uri)
            throws RemoteConnectionException {
        try {
            IRemoteProxyManager rmtProxyMgr = RemoteProxyManager.getInstance();
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
     * @param monitor
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

    public IRemoteFileProxy getRmtFileProxy() {
        return rmtFileProxy;
    }

}
