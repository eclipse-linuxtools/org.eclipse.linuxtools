/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.io.IOException;

public abstract class AbstractKillableThread extends Thread {
	protected boolean kill;
	protected boolean stop;
	protected boolean mayInterrupt;

	/**
	 * Kill this thread.
	 */
	public void kill() {
		kill = true;
		// System.out.println("killing logging thread");
		this.interrupt();
	}

	public void requestStop() {
		stop = true;
	}

	/**
	 * Local routine to run. It will be run again on interruption if mayInterupt
	 * is TRUE.
	 * 
	 * @throws InterruptedException
	 *             interrupted exception
	 * @throws IOException
	 *             I/O exception
	 */
	public abstract void execute() throws InterruptedException, IOException;

	@Override
	public void run() {
		boolean finished = false;
		while (!finished) {
			try {
				execute();
				finished = true;
			} catch (InterruptedException e) {
				if (kill || !mayInterrupt)
					finished = true;
				// otherwise..continue
			} catch (IOException e) {
				finished = true;
				// failed to close output stream..just ignore
			}
		}
		// System.out.println("thread complete");
	}
}
