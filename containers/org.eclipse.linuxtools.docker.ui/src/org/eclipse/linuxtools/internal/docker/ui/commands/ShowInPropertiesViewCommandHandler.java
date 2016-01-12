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

package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;

/**
 * Command handler to open the selection in the Properties View.
 */
public class ShowInPropertiesViewCommandHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IDockerContainer> containers = CommandUtils
				.getSelectedContainers(activePart);
		if (containers == null || containers.isEmpty()) {
			return null;
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					final PropertySheet propertySheet = (PropertySheet) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage()
							.showView("org.eclipse.ui.views.PropertySheet");
					final PropertyShowInContext showInContext = new PropertyShowInContext(
							activePart,
							HandlerUtil.getCurrentSelection(event));
					propertySheet.show(showInContext);
					// final TabbedPropertySheetPage tabbedPropertySheetPage =
					// activePart
					// .getAdapter(TabbedPropertySheetPage.class);
					// tabbedPropertySheetPage.setFocus();
				} catch (PartInitException e) {
					Activator.logErrorMessage(
							CommandMessages.getString(
									"command.showIn.propertiesView.failure"), //$NON-NLS-1$
							e);

				}
			}
		});
		return null;
	}
}
