/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;

public class RemoteUtils {
	static public void writeFileToRemote(final IFileSystem fsService, final IRemoteCaller caller,
			IPath localFile, IPath remoteFile, final RemoteLaunchStep step) throws CoreException, FileNotFoundException {
		final InputStream inp = new FileInputStream(localFile.toOSString());
		int flags = IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC;
		fsService.open(remoteFile.toOSString(), flags, new FileAttrs(IFileSystem.ATTR_PERMISSIONS, 0, 0, 0, 
				IFileSystem.S_IRUSR | IFileSystem.S_IWUSR | IFileSystem.S_IXUSR, 0, 0, null), new IFileSystem.DoneOpen() {

			IFileHandle handle;
			long offset = 0;
			final Set<IToken> cmds = new HashSet<IToken>();
			final byte[] buf = new byte[0x1000];

			public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
				this.handle = handle;
				if (error != null) {
					caller.onError(error);
				}
				else {
					writeNext();
				}
			}

			private void writeNext() {
				try {
					while (cmds.size() < 8) {
						int rd = inp.read(buf);
						if (rd < 0) {
							close();
							break;
						}
						cmds.add(fsService.write(handle, offset, buf, 0, rd, new IFileSystem.DoneWrite() {

							public void doneWrite(IToken token, FileSystemException error) {
								cmds.remove(token);
								if (error != null) {
									caller.onError(error);
								}
								else {
									writeNext();
								}
							}
						}));
						offset += rd;
					}
				}
				catch (Throwable x) {
					caller.onError(x);
				}
			}

			private void close() {
				if (cmds.size() > 0) {
					return;
				}
				try {
					inp.close();
					fsService.close(handle, new IFileSystem.DoneClose() {

						public void doneClose(IToken token, FileSystemException error) {
							if (error != null) {
								caller.onError(error);
							}
							else {
								step.done();
							}
						}
					});
				}
				catch (Throwable x) {
					caller.onError(x);
				}
			}
		});
	}
	
	static public void writeFileToLocal(final IFileSystem fsService, final IRemoteCaller caller, 
			IPath remoteFile, IPath localFile, final RemoteLaunchStep step) throws CoreException, FileNotFoundException {
		final OutputStream out = new FileOutputStream(localFile.toOSString());
		int flags = IFileSystem.TCF_O_READ;
		fsService.open(remoteFile.toOSString(), flags, null, new IFileSystem.DoneOpen() {

			IFileHandle handle;
			long offset = 0;
			Set<IToken> cmds = new HashSet<IToken>();
			static final int BUF_LENGTH = 0x1000;

			public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
				this.handle = handle;
				if (error != null) {
					caller.onError(error);
				}
				else {
					readNext();
				}
			}

			private void readNext() {
				try {
					cmds.add(fsService.read(handle, offset, BUF_LENGTH, new IFileSystem.DoneRead() {

						public void doneRead(IToken token, FileSystemException error, byte[] data,
								boolean eof) {
							cmds.remove(token);
							if (error != null) {
								caller.onError(error);
							}
							else {
								try {
									out.write(data);
									offset += data.length;
									if (eof) {
										close();
									}
									else {
										readNext();
									}
								} catch (IOException e) {
									caller.onError(e);
								}
							}
						}
					}));
				}
				catch (Throwable x) {
					caller.onError(x);
				}
			}

			private void close() {
				if (cmds.size() > 0) {
					return;
				}
				try {
					out.close();
					fsService.close(handle, new IFileSystem.DoneClose() {

						public void doneClose(IToken token, FileSystemException error) {
							if (error != null) {
								caller.onError(error);
							}
							else {
								step.done();
							}
						}
					});
				}
				catch (Throwable x) {
					caller.onError(x);
				}
			}
		});
	}

}
