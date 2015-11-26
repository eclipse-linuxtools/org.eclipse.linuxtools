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
package org.eclipse.linuxtools.internal.vagrant.ui.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.vagrant.core.EnumVMStatus;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;

public class VMPropertyTester extends PropertyTester {

	public static final String IS_RUNNING = "isRunning"; //$NON-NLS-1$
	public static final String IS_SHUTOFF = "isShutoff"; //$NON-NLS-1$
	public static final String IS_PAUSED = "isPaused"; //$NON-NLS-1$
	public static final String IS_REMOVABLE = "isRemovable"; //$NON-NLS-1$
	public static final String IS_UNKNOWN = "isUnknown"; //$NON-NLS-1$

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof IVagrantVM) {
			final IVagrantVM vm = (IVagrantVM) receiver;
			switch (property) {
			case IS_RUNNING:
				return checkIfStateMatchesExpectation(vm,
						EnumVMStatus.RUNNING, expectedValue);
			case IS_SHUTOFF:
				return checkIfStateMatchesExpectation(vm,
						EnumVMStatus.SHUTOFF, expectedValue);
			case IS_UNKNOWN:
				return checkIfStateMatchesExpectation(vm,
						EnumVMStatus.UNKNOWN, expectedValue);
			case IS_PAUSED:
				return checkIfStateMatchesExpectation(vm,
						EnumVMStatus.PAUSED, expectedValue);
			case IS_REMOVABLE:
				return checkIfStateMatchesExpectation(vm,
						EnumVMStatus.SHUTOFF, expectedValue)
						|| checkIfStateMatchesExpectation(vm,
								EnumVMStatus.UNKNOWN, expectedValue);
			}
		}
		return false;
	}

	/**
	 * Checks if the status of the given {@link IDockerContainer}
	 * 
	 * @param container the given {@link IDockerContainer}
	 * @param expectedStatus the {@link IDockerContainer} status to check 
	 * @param expectedMatch the container status result to check
	 * @return {@code true} if the current status matches the expectation, {@code false} otherwise.
	 */
	private boolean checkIfStateMatchesExpectation(final IVagrantVM vm, final EnumVMStatus expectedStatus, final Object expectedMatch) {
		if(expectedMatch == null) {
			return false;
		}
		final EnumVMStatus containerStatus = EnumVMStatus.fromStatusMessage(vm.state());
		return expectedMatch.equals((containerStatus == expectedStatus));
	}

}
