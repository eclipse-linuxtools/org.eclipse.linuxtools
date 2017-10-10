/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.EnumSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;

public class NullOperationMonitor extends NullProgressMonitor implements IOperationMonitor {

	private EnumSet<OperationFlag> flags;

	public synchronized void addFlag(OperationFlag flag) {
		if (flags == null) {
			flags = EnumSet.of(flag);
		} else {
			flags.add(flag);
		}
	}

	public void clearBlocked() {
		// ignore			
	}

	public synchronized boolean hasFlag(OperationFlag flag) {
		if (flags != null) {
			return flags.contains(flag);
		}
		return false;
	}

	public IOperationMonitor newChild(int totalWork) {
		return this;
	}

	public IOperationMonitor newChild(int totalWork, int suppressFlags) {
		return this;
	}

	public synchronized void removeFlag(OperationFlag flag) {
		if (flags != null) {
			flags.remove(flag);
		}
	}

	public void setBlocked(IStatus reason) {
		// ignore			
	}

	public IOperationMonitor setWorkRemaining(int workRemaining) {
		return this;
	}

}