/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerContainer;

/**
 * @author xcoulon
 *
 */
public class ContainerPropertyTester extends PropertyTester {

	/** Property name to check if a given {@link IDockerContainer} is running. */
	public static final String IS_RUNNING = "isRunning";

	/** Property name to check if a given {@link IDockerContainer} is stopped (exited). */
	public static final String IS_STOPPED = "isStopped";

	/** Property name to check if a given {@link IDockerContainer} is paused. */
	public static final String IS_PAUSED = "isPaused";

	/**
	 * Property name to check if a given {@link IDockerContainer} can be
	 * deleted.
	 */
	public static final String IS_REMOVABLE = "isRemovable";

	/**
	 * Property name to check if a given {@link IDockerContainer} is unknown.
	 */
	public static final String IS_UNKNOWN = "isUnknown";

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof IDockerContainer) {
			final IDockerContainer container = (IDockerContainer) receiver;
			switch (property) {
			case IS_RUNNING:
				return checkIfStateMatchesExpectation(container, EnumDockerStatus.RUNNING, expectedValue);
			case IS_STOPPED:
				return checkIfStateMatchesExpectation(container,
						EnumDockerStatus.STOPPED, expectedValue);
			case IS_UNKNOWN:
				return checkIfStateMatchesExpectation(container,
						EnumDockerStatus.UNKNOWN, expectedValue);
			case IS_PAUSED:
				return checkIfStateMatchesExpectation(container, EnumDockerStatus.PAUSED, expectedValue);
			case IS_REMOVABLE:
				return checkIfStateMatchesExpectation(container,
						EnumDockerStatus.STOPPED, expectedValue)
						|| checkIfStateMatchesExpectation(container,
								EnumDockerStatus.UNKNOWN, expectedValue);
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
	private boolean checkIfStateMatchesExpectation(final IDockerContainer container, final EnumDockerStatus expectedStatus, final Object expectedMatch) {
		if(expectedMatch == null) {
			return false;
		}
		final EnumDockerStatus containerStatus = EnumDockerStatus.fromStatusMessage(container.status());
		return expectedMatch.equals((containerStatus == expectedStatus));
	}

	/**
	 * Check if the given IStructuredSelection containing IDockerContainer
	 * elements are all in the specified state
	 * @param ss an IStructuredSelection of IDockerContainer elements
	 * @param state the state of the IDockerContainer to test
	 * @return true if all IDockerContainer elements match the specified
	 * and false otherwise.
	 */
	private static boolean checkState(IStructuredSelection ss, EnumDockerStatus state) {
		if (ss.toList().isEmpty()) {
			return false;
		}
		for (Object o : ss.toList()) {
			IDockerContainer c = (IDockerContainer) o;
			if (EnumDockerStatus.fromStatusMessage(c.status()) != state) {
				return false;
			}
		}
		return true;
	}

	public static boolean isStopped(IStructuredSelection ss) {
		return checkState(ss, EnumDockerStatus.STOPPED);
	}

	public static boolean isRunning(IStructuredSelection ss) {
		return checkState(ss, EnumDockerStatus.RUNNING);
	}

	public static boolean isPaused(IStructuredSelection ss) {
		return checkState(ss, EnumDockerStatus.PAUSED);
	}

}
