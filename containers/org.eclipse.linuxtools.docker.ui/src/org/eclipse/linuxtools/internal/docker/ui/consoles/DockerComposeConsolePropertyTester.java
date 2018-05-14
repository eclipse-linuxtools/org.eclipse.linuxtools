/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.console.MessageConsole;

/**
 * A {@link PropertyTester} for the {@code docker-compose}
 * {@link MessageConsole}.
 */
public class DockerComposeConsolePropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		return (receiver instanceof DockerComposeConsole);
	}

}
