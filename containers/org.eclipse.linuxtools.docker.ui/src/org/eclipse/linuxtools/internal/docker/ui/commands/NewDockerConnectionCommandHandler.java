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

package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author xcoulon
 *
 */
public class NewDockerConnectionCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		CommandUtils.openWizard(new NewDockerConnection(),
				HandlerUtil.getActiveShell(event));
		return null;
	}
	
}
