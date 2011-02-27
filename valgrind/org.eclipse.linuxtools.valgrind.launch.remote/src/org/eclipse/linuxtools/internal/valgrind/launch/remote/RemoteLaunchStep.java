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

import java.util.Queue;

import org.eclipse.tm.tcf.protocol.IChannel;


public abstract class RemoteLaunchStep {
	
	private Queue<RemoteLaunchStep> steps;
	private IChannel channel;
	private String name;
//	private static int indent = 0;
	
	public RemoteLaunchStep(Queue<RemoteLaunchStep> steps, IChannel channel, String name) {
		this.steps = steps;
		this.channel = channel;
		this.name = name;

		steps.add(this);
	}
	
	public abstract void start() throws Exception;
	
	public void done() {
//		indent--;
//		printSpaces();
//		System.out.println("End: " + name);
		
		if (channel.getState() != IChannel.STATE_OPEN) {
			return;
		}
        try {
        	if (!steps.isEmpty()) {
        		RemoteLaunchStep step = steps.remove();
        		
//        		printSpaces();
//				indent++;
//        		System.out.println("Begin: " + step.name);
        		
				step.start();
        	}
        }
        catch (Throwable x) {
            channel.terminate(x);
        }
	}
	
	@Override
	public String toString() {
		return name;
	}

//	private void printSpaces() {
//		for (int i = 0; i < indent; i++) {
//			System.out.print("-");
//		}
//	}
}
