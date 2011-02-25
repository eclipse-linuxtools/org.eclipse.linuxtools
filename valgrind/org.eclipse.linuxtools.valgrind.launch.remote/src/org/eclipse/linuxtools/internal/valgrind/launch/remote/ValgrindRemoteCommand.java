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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tm.tcf.services.IProcesses.ProcessesListener;
import org.eclipse.tm.tcf.services.IStreams;

public class ValgrindRemoteCommand extends ValgrindCommand {
	private IChannel channel;
	private Map<String, String> streamIds;
	private Queue<RemoteLaunchStep> launchSteps;

	public ValgrindRemoteCommand(IChannel channel, Queue<RemoteLaunchStep> launchSteps) {
		this.channel = channel;
		this.launchSteps = launchSteps;
		streamIds = new HashMap<String, String>();
	}

	@Override
	protected Process startProcess(final String[] commandArray, final Object env,
			final File workDir, final String binPath, boolean usePty) throws IOException {
		final IStreams streamsService = channel.getRemoteService(IStreams.class);
		final IProcesses procService = channel.getRemoteService(IProcesses.class);
		
		// Connect streams
		final IStreams.StreamsListener streamsListener = new IStreams.StreamsListener() {

			public void disposed(String stream_type, String stream_id) {
				streamIds.remove(stream_id);
			}

			public void created(String stream_type, String stream_id, String context_id) {
				streamIds.put(stream_id, context_id);
			}
		};
		
		new RemoteLaunchStep(launchSteps, channel, "Streams Subscribe") { //$NON-NLS-1$
			
			@Override
			public void start() throws Exception {
				// Register streams as they are created
				streamsService.subscribe(IProcesses.NAME, streamsListener, new IStreams.DoneSubscribe() {
					
					public void doneSubscribe(IToken token, Exception error) {
						if (error != null) {
							channel.terminate(error);
						}
						else {
							done();
						}
					}
				});
			}
		};
		
		new RemoteLaunchStep(launchSteps, channel, "Processes Start") { //$NON-NLS-1$
			
			@SuppressWarnings("unchecked")
			@Override
			public void start() throws Exception {
				// Create process
				procService.start(workDir.getAbsolutePath(), binPath, commandArray, (Map<String, String>) env, false, new IProcesses.DoneStart() {
					public void doneStart(IToken token, Exception error,
							final ProcessContext context) {
						if (error != null) {
							channel.terminate(error);
						}
						else {
							final ValgrindRemoteProcess remoteProcess = new ValgrindRemoteProcess(context, channel);
							process = remoteProcess;

							// Connect I/O streams
							final String stdinID = (String) context.getProperties().get(IProcesses.PROP_STDIN_ID);
							final String stdoutID = (String) context.getProperties().get(IProcesses.PROP_STDOUT_ID);
							final String stderrID = (String) context.getProperties().get(IProcesses.PROP_STDERR_ID);
							for (final String id : streamIds.keySet().toArray(new String[streamIds.size()])) {
								if (id.equals(stdinID)) {
									remoteProcess.connectOutputStream(stdinID);
								}
								else if (id.equals(stdoutID)) {
									remoteProcess.connectInputStream(stdoutID);
								}
								else if (id.equals(stderrID)) {
									// FIXME Not receiving stderr stream
									remoteProcess.connectErrorStream(stderrID);
								}
								else {
									disconnectStream(id);
								}
							}
							
							// Register as a listener to retrieve exit code
							ProcessesListener listener = new ProcessesListener() {					
								public void exited(String process_id, int exit_code) {
									if (process_id.equals(context.getID())) {
										// Disconnect input stream
										disconnectStream(stdinID);
										remoteProcess.setExitCode(exit_code);
										remoteProcess.setTerminated(true);
									}			
								}
							};
							procService.addListener(listener);
							
							done();
						}
					}
				});
			}
		};

		return process;
	}
	
	private void disconnectStream(String id) {
        streamIds.remove(id);
        if (channel.getState() != IChannel.STATE_OPEN) {
        	return;
        }
        
        IStreams streams = channel.getRemoteService(IStreams.class);
        streams.disconnect(id, new IStreams.DoneDisconnect() {
            public void doneDisconnect(IToken token, Exception error) {
                if (channel.getState() != IChannel.STATE_OPEN) {
                	return;
                }
                if (error != null) {
                	channel.terminate(error);
                }
            }
        });
    }

}
