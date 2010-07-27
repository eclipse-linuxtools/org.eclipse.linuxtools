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

import java.util.LinkedList;

import org.eclipse.tm.tcf.protocol.IChannel;


public abstract class RemoteLaunchStep {
	
	private LinkedList<RemoteLaunchStep> steps;
	private IChannel channel;
	
	public RemoteLaunchStep(LinkedList<RemoteLaunchStep> steps, IChannel channel) {
		this.steps = steps;
		this.channel = channel;
		steps.add(this);
	}
	
	public abstract void start() throws Exception;
	
	public void done() {
		if (channel.getState() != IChannel.STATE_OPEN) {
			return;
		}
        try {
        	if (!steps.isEmpty()) {
        		steps.removeFirst().start();
        	}
        }
        catch (Throwable x) {
            channel.terminate(x);
        }
	}

}
