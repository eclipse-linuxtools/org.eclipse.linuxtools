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
