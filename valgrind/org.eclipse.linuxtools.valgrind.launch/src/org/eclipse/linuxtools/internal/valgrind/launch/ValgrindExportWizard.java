/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class ValgrindExportWizard extends Wizard implements IExportWizard {

	protected ValgrindExportWizardPage exportPage;

	@Override
	public boolean performFinish() {
		final File[] logs = exportPage.getSelectedFiles();
		final IPath outputPath = exportPage.getOutputPath();

		IProgressService ps = PlatformUI.getWorkbench().getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					if (logs.length > 0) {
						File outputDir = outputPath.toFile();
						monitor.beginTask(NLS.bind(Messages.getString("ValgrindExportWizard.Export_task"), outputPath.toOSString()), logs.length); //$NON-NLS-1$
						FileChannel inChan = null;
						FileChannel outChan = null;
						try {
							for (File log : logs) {
								monitor.subTask(NLS.bind(Messages.getString("ValgrindExportWizard.Export_subtask"), log.getName())); //$NON-NLS-1$

								File outLog = new File(outputDir, log.getName());
								inChan = new FileInputStream(log).getChannel();
								outChan = new FileOutputStream(outLog).getChannel();

								outChan.transferFrom(inChan, 0, inChan.size());

								inChan.close();
								outChan.close();

								monitor.worked(1);
							}
						} catch (IOException e) {
							throw new InvocationTargetException(e);
						} finally {
							try {
								if (inChan != null && inChan.isOpen()) {
									inChan.close();
								}
								if (outChan != null && outChan.isOpen()) {
									outChan.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
							monitor.done();
						}
					}
				}

			});

		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindExportWizard.Export_fail"), e); //$NON-NLS-1$
			ErrorDialog.openError(getShell(), ExportWizardConstants.WIZARD_TITLE, null, status);
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
		}

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(ExportWizardConstants.WIZARD_WINDOW_TITLE);
		exportPage = getWizardPage();
		exportPage.setDescription(ExportWizardConstants.WIZARD_DESCRIPTION);
		addPage(exportPage);
	}

	protected ValgrindExportWizardPage getWizardPage() {
		return new ValgrindExportWizardPage(Messages.getString("ValgrindExportWizard.Page_name"), ExportWizardConstants.WIZARD_TITLE, null); //$NON-NLS-1$
	}

}
