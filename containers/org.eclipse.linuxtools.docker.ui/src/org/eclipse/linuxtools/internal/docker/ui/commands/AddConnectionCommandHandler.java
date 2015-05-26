/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @author xcoulon
 *
 */
public class AddConnectionCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if(activePart instanceof CommonNavigator) {
			final boolean connectionAdded = CommandUtils.openWizard(new NewDockerConnection(), HandlerUtil.getActiveShell(event));
			if(connectionAdded) {
				final CommonViewer viewer = ((CommonNavigator)activePart).getCommonViewer();
				viewer.refresh();
			}
		}
		return null;
	}
	
}
