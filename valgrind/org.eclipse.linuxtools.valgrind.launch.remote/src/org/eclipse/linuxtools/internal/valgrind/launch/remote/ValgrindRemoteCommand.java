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
import java.util.Queue;

import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.tm.tcf.protocol.IChannel;

public class ValgrindRemoteCommand extends ValgrindCommand implements IRemoteProcessListener {
	private IChannel channel;
	private Queue<RemoteLaunchStep> launchSteps;

	public ValgrindRemoteCommand(IChannel channel, Queue<RemoteLaunchStep> launchSteps) {
		this.channel = channel;
		this.launchSteps = launchSteps;
	}

	@Override
	protected Process startProcess(final String[] commandArray, final Object env,
			final File workDir, final String binPath, boolean usePty) throws IOException {
		RemoteCommand rc = new RemoteCommand(channel, launchSteps, this);
		return rc.startProcess(commandArray, env, workDir, binPath, usePty);
	}

	public void newProcess(Process p) {
		process = p;
	}
}
