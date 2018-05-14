/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat Inc.
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
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class FilterContainersWithLabelsCommandHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event)
			throws ExecutionException {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final boolean checked = !HandlerUtil
				.toggleCommandState(event.getCommand());
		if (activePart instanceof DockerContainersView) {
			final DockerContainersView containersView = (DockerContainersView) activePart;
			containersView.showContainersWithLabels(checked);
		}
		return null;
	}

}
