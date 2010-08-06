/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.DoneMkDir;
import org.eclipse.tm.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IStreams;

public class ValgrindRemoteLaunchDelegate extends
ValgrindLaunchConfigurationDelegate {

	private IChannel channel;
	private SubMonitor monitor;
	private IFileSystem fsService;
	private IProcesses procService;
	private Queue<RemoteLaunchStep> launchSteps;
	private Throwable ex;
	private IPath localOutputDir;
	private IPath remoteBinFile;
	private IPeer peer;

	public void launch(final ILaunchConfiguration config, String mode,
			final ILaunch launch, IProgressMonitor m) throws CoreException {
		if (m == null) {
			m = new NullProgressMonitor();
		}
		launchSteps = new ConcurrentLinkedQueue<RemoteLaunchStep>();
		
		// Clear process as we wait on it to be instantiated
		process = null;

		monitor = SubMonitor
		.convert(
				m,
				Messages.ValgrindRemoteLaunchDelegate_task_name, 10);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		this.config = config;
		this.launch = launch;
		try {			
			// remove any output from previous run
			ValgrindUIPlugin.getDefault().resetView();
			// reset stored launch data
			getPlugin().setCurrentLaunchConfiguration(null);
			getPlugin().setCurrentLaunch(null);

			// Open TCF Channel
			Map<String, IPeer> peers = Protocol.getLocator().getPeers();
			String peerID = config.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_PEERID, RemoteLaunchConstants.DEFAULT_REMOTE_PEERID);
			peer = peers.get(peerID);
			
			if (peer == null) {
				abort(NLS.bind(Messages.ValgrindRemoteLaunchDelegate_error_no_peers, peerID), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
			else {
				channel = peer.openChannel();
				channel.addChannelListener(new IChannel.IChannelListener() {

					public void onChannelOpened() {
						try {
							IStreams streamService = channel.getRemoteService(IStreams.class);
							if (streamService == null) {
								abort(Messages.ValgrindRemoteLaunchDelegate_error_no_streams, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
							}

							fsService = channel.getRemoteService(IFileSystem.class);
							if (fsService == null) {
								abort(Messages.ValgrindRemoteLaunchDelegate_error_no_fs, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
							}

							procService = channel.getRemoteService(IProcesses.class);
							if (procService == null) {
								abort(Messages.ValgrindRemoteLaunchDelegate_error_no_proc, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
							}

							command = new ValgrindRemoteCommand(channel, launchSteps);
							
							// Retrieve user-defined Valgrind binary location
							final IPath valgrindLocation = Path.fromOSString(config.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, RemoteLaunchConstants.DEFAULT_REMOTE_VALGRINDLOC));

							monitor.worked(1);

							// Copy binary using FileSystem service
							final IPath exePath = CDebugUtils.verifyProgramPath(config);
							final IPath remoteDir = Path.fromOSString(config.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_DESTDIR, RemoteLaunchConstants.DEFAULT_REMOTE_DESTDIR));
							remoteBinFile = remoteDir.append(exePath.lastSegment());
							
							IPath remoteLogDir = Path.fromOSString(config.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, RemoteLaunchConstants.DEFAULT_REMOTE_OUTPUTDIR));
							outputPath = remoteLogDir.append("eclipse-valgrind-" + System.currentTimeMillis());

							try {
								new RemoteLaunchStep(launchSteps, channel, "FileSystem Write Binary") { //$NON-NLS-1$
									@Override
									public void start() throws Exception {
										writeFileToRemote(exePath, remoteBinFile, this);					
									}
								};
								
								new RemoteLaunchStep(launchSteps, channel, "FileSystem Log Mkdir") { //$NON-NLS-1$
									@Override
									public void start() throws Exception {
										fsService.mkdir(outputPath.toOSString(), new FileAttrs(0, 0, 0, 0, 0, 0, 0, null), new DoneMkDir() {
											public void doneMkDir(IToken token,
													FileSystemException error) {
												if (error != null) {
													disconnect(error);
												}
												else {
													done();
												}
											}
										});
									}
								};

								
								String[] arguments = getProgramArgumentsArray(config);

								// Start process using Processes service
								startRemoteProcess(config, launch,
										valgrindLocation, remoteBinFile,
										arguments, remoteDir.toFile(), remoteLogDir);
								
								// Begin executing launch steps
								launchSteps.remove().start();
							} catch (Throwable e) {
								disconnect(e);
							}
						} catch (CoreException e) {
							disconnect(e);
						}
					}

					public void onChannelClosed(Throwable error) {
						channel.removeChannelListener(this);
						if (error != null) {
							ex = error;
						}
					}

					public void congestionLevel(int level) {
					}
				});
			}

			while (process == null || !process.isTerminated()) {
				Thread.sleep(100);
			}
			
			cleanup(null);
			
			// Begin executing launch steps
			try {
				launchSteps.remove().start();
			} catch (Exception e) {
				disconnect(e);
			}
			
			// Wait for TCF connection to close
			while (channel.getState() != IChannel.STATE_CLOSED) {
				Thread.sleep(100);
			}

			if (ex != null) {
				abort(Messages.ValgrindRemoteLaunchDelegate_error_launch_failed, ex, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
			
			// store these for use by other classes
			getPlugin().setCurrentLaunchConfiguration(config);
			getPlugin().setCurrentLaunch(launch);

			// parse Valgrind logs
			IValgrindMessage[] messages = parseLogs(localOutputDir);

			// create launch summary string to distinguish this launch
			launchStr = createLaunchStr();

			// create view
			ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
			// set log messages
			ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
			view.setMessages(messages);
			monitor.worked(1);

			// pass off control to extender
			dynamicDelegate.handleLaunch(config, launch, localOutputDir, monitor.newChild(2));
			
			// initialize tool-specific part of view
			dynamicDelegate.initializeView(view.getDynamicView(), launchStr, monitor.newChild(1));

			// refresh view
			ValgrindUIPlugin.getDefault().refreshView();

			// show view
			ValgrindUIPlugin.getDefault().showView();
			monitor.worked(1);
		} catch (IOException e) {
			abort("Error starting process", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			m.done();
		}
	}

	private void cleanup(final Throwable t) {
		// Delete binary
		new RemoteLaunchStep(launchSteps, channel, "FileSystem Remove Binary") { //$NON-NLS-1$
			@Override
			public void start() throws Exception {
				fsService.remove(remoteBinFile.toOSString(), new IFileSystem.DoneRemove() {
					
					public void doneRemove(IToken token, FileSystemException error) {
						done();
					}
				});
			}
		};
		
		// Copy log files from remote
		new RemoteLaunchStep(launchSteps, channel, "FileSystem Open Log Dir") { //$NON-NLS-1$
			@Override
			public void start() throws Exception {
				fsService.opendir(outputPath.toOSString(), new IFileSystem.DoneOpen() {
					
					public void doneOpen(IToken token, FileSystemException error,
							IFileHandle handle) {
						if (error != null) {
							closeChannel();
						}
						else {
							readDir(handle);
						}
					}

					private void readDir(final IFileHandle handle) {
						fsService.readdir(handle, new IFileSystem.DoneReadDir() {
							
							public void doneReadDir(IToken token, FileSystemException error,
									DirEntry[] entries, boolean eof) {
								if (error != null) {
									closeChannel();
								}
								else {
									for (DirEntry entry : entries) {
										final IPath remotePath = outputPath.append(entry.filename);
										final IPath localPath = localOutputDir.append(entry.filename);
										
										if (t == null) { // We aren't just cleaning up after an error
											// Copy each log file
											new RemoteLaunchStep(launchSteps, channel, "FileSystem Write Log") { //$NON-NLS-1$
												@Override
												public void start() throws Exception {
													writeFileToLocal(remotePath, localPath, this);
												}
											};
										}
										
										// Delete log file on remote
										new RemoteLaunchStep(launchSteps, channel, "FileSystem Delete Log") { //$NON-NLS-1$
											@Override
											public void start() throws Exception {
												fsService.remove(remotePath.toOSString(), new IFileSystem.DoneRemove() {
													public void doneRemove(IToken token, FileSystemException error) {
														done();
													}
												});
											}
										};
									}
									
									if (!eof) {
										readDir(handle);
									}
									else {									
										// Close the log directory
										new RemoteLaunchStep(launchSteps, channel, "FileSystem Close Log Dir") { //$NON-NLS-1$
											@Override
											public void start() throws Exception {
												fsService.close(handle, new IFileSystem.DoneClose() {
													public void doneClose(IToken token, FileSystemException error) {
														done();
													}
												});
											}
										};
										
										// Delete the remote log directory
										new RemoteLaunchStep(launchSteps, channel, "FileSystem Rmdir Log Dir") { //$NON-NLS-1$
											@Override
											public void start() throws Exception {
												fsService.rmdir(outputPath.toOSString(), new IFileSystem.DoneRemove() {
													public void doneRemove(IToken token, FileSystemException error) {
														done();
													}
												});
											}
										};
										
										closeChannel();
										
										done();
									}
								}
							}
						});
					}
				});
			}
		};
	}

	protected String createLaunchStr() {
		return config.getName()
		+ " [" + getPlugin().getToolName(toolID) + " on " + peer.getID() + "] " + process.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	private void disconnect(Throwable t) {
		if (fsService != null) {
			// Delete files, don't try to copy
			cleanup(t);
		}
		if (channel.getState() != IChannel.STATE_CLOSED) {
			channel.terminate(t);
		}
	}

	private void writeFileToRemote(IPath localFile, IPath remoteFile, final RemoteLaunchStep step) throws CoreException, FileNotFoundException {
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
					disconnect(error);
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
									disconnect(error);
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
					disconnect(x);
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
								disconnect(error);
							}
							else {
								step.done();
							}
						}
					});
				}
				catch (Throwable x) {
					disconnect(x);
				}
			}
		});
	}
	
	private void writeFileToLocal(IPath remoteFile, IPath localFile, final RemoteLaunchStep step) throws CoreException, FileNotFoundException {
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
					disconnect(error);
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
								disconnect(error);
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
									disconnect(e);
								}
							}
						}
					}));
				}
				catch (Throwable x) {
					disconnect(x);
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
								disconnect(error);
							}
							else {
								step.done();
							}
						}
					});
				}
				catch (Throwable x) {
					disconnect(x);
				}
			}
		});
	}

	private void startRemoteProcess(final ILaunchConfiguration config,
			final ILaunch launch, final IPath valgrindLocation,
			final IPath exePath, final String[] arguments, final File workDir, IPath logDir)
	throws Exception {
		
		// create/empty local output directory
		IValgrindOutputDirectoryProvider provider = getPlugin().getOutputDirectoryProvider();
		localOutputDir = provider.getOutputPath();
		createDirectory(localOutputDir);

		// tool that was launched
		toolID = getTool(config);
		// ask tool extension for arguments
		dynamicDelegate = getDynamicDelegate(toolID);
		String[] opts = getValgrindArgumentsArray(config);

		// set the default source locator if required
		setDefaultSourceLocator(launch, config);

		ArrayList<String> cmdLine = new ArrayList<String>(
				1 + arguments.length);
		cmdLine.add(valgrindLocation.toOSString());
		cmdLine.addAll(Arrays.asList(opts));
		cmdLine.add(exePath.toOSString());
		cmdLine.addAll(Arrays.asList(arguments));
		final String[] commandArray = (String[]) cmdLine
		.toArray(new String[cmdLine.size()]);
		boolean usePty = config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
				ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
		monitor.worked(1);

		// TODO Get remote environment
		@SuppressWarnings("unchecked")
		Map<String, String> env = (Map<String, String>) config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, String>) null);

		// check for cancellation
		if (monitor.isCanceled()) {
			disconnect(null);
		}
		// call Valgrind
		command.execute(commandArray, env, workDir, valgrindLocation.toOSString(), usePty);

		new RemoteLaunchStep(launchSteps, channel, "Create IProcess") { //$NON-NLS-1$
			@Override
			public void start() throws Exception {
				monitor.worked(3);
				process = createNewProcess(launch, command.getProcess(),
						commandArray[0]);
				// set the command line used
				process.setAttribute(IProcess.ATTR_CMDLINE,
						command.getCommandLine());
				done();
			}
		};
	}

	private void closeChannel() {
		new RemoteLaunchStep(launchSteps, channel, "Close Channel") { //$NON-NLS-1$
			@Override
			public void start() throws Exception {
				if (channel.getState() != IChannel.STATE_CLOSED) {
					channel.terminate(null);
				}
				done();
			}
		};
	}
}
