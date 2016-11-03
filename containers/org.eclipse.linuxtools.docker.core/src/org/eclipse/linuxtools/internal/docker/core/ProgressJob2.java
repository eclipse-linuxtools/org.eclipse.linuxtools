/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ProgressJob2 extends ProgressJob {

	private int percentageDone = 0;
	private String statusMessage = ""; //$NON-NLS-1$

	private Object lockObject = new Object();

	private String jobName;

	public ProgressJob2(String name, String jobName) {
		super(name, jobName);
		this.jobName = jobName;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
		boolean done = false;

		while (!done) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			// if work percentage has changed...add new amount
			String status = getStatusMessage();
			if (status != null && !status.isEmpty()) {
				monitor.subTask(statusMessage);
			}
			// if we are 100% or more done, then we are done
			if (percentageDone >= 100) {
				done = true;
			}
			// otherwise, sleep and then loop again
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				done = true;
			}
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	private String getStatusMessage() {
		synchronized (lockObject) {
			return statusMessage;
		}
	}

	public void setStatusMessage(String statusMessage) {
		synchronized (lockObject) {
			this.statusMessage = statusMessage;
		}
	}

	@Override
	public void setPercentageDone(int percentage) {
		synchronized (lockObject) {
			if (percentage > percentageDone) {
				percentageDone = percentage;
			}
		}
	}

}
