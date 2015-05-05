/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class ProgressJob extends Job {

	private int percentageDone = 0;
	private int percentageChange = 0;

	private Object lockObject = new Object();

	private String jobName;

	public ProgressJob(String name, String jobName) {
		super(name);
		this.jobName = jobName;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(jobName, 100);
		boolean done = false;

		while (!done) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			// if work percentage has changed...add new amount
			int change = getPercentageChange();
			if (change > 0) {
				monitor.worked(change);
				setPercentageChange(0);
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

	private int getPercentageChange() {
		synchronized (lockObject) {
			return percentageChange;
		}
	}

	private void setPercentageChange(int percentChange) {
		synchronized (lockObject) {
			this.percentageChange = percentChange;
		}
	}

	public void setPercentageDone(int percentage) {
		synchronized (lockObject) {
			if (percentage > percentageDone) {
				percentageChange = percentage - percentageDone;
				percentageDone = percentage;
			}
		}
	}

}
