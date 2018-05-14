/*******************************************************************************
 * Copyright (c) 2011, 2018 Tasktop Technologies and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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