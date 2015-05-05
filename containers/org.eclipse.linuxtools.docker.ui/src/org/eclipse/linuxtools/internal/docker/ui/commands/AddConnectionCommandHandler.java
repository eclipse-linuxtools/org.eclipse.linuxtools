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
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @author xcoulon
 *
 */
public class AddConnectionCommandHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if(activePart instanceof CommonNavigator) {
			final boolean connectionAdded = openWizard(new NewDockerConnection(), HandlerUtil.getActiveShell(event));
			if(connectionAdded) {
				final CommonViewer viewer = ((CommonNavigator)activePart).getCommonViewer();
				viewer.refresh();
			}
		}
		// return must be null, javadoc says.
		return null;
	}
	
	public static boolean openWizard(final IWizard wizard, final Shell shell) {
		WizardDialog wizardDialog = new WizardDialog(shell, wizard);
		wizardDialog.create();
		return wizardDialog.open() == Dialog.OK;
	}

}
