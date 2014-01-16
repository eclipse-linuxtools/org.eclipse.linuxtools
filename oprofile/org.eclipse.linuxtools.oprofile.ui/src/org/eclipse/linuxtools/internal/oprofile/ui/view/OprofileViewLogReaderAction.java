/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Keith Seitz <keiths@redhat.com> - much of the code in the LogReader class
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Log reader action. Reads /var/lib/oprofile/samples/oprofiled.log and displays
 *  it in a nice dialog. Although the log is of dubious utility, it might be useful
 *  to some.
 */
public class OprofileViewLogReaderAction extends Action {
	public OprofileViewLogReaderAction() {
		super(OprofileUiMessages.getString("view.actions.logreader.label")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		LogReader log = new LogReader();

		try {
			new ProgressMonitorDialog(activeShell).run(true, false, log);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//open custom log dialog
	    OprofiledLogDialog odlg = new OprofiledLogDialog(activeShell, log.getLogContents());
	    odlg.open();
	}
}

/**
 * A Runnable to read oprofiled's logfile
 */
class LogReader implements Runnable, IRunnableWithProgress {
	private static long lastModified = -1;
	private static String contents = null;
	private IRemoteFileProxy proxy;

	@Override
	public void run() {
		try {
			proxy = RemoteProxyManager.getInstance().getFileProxy(
					Oprofile.OprofileProject.getProject());
			IFileStore fileStore = proxy.getResource(Oprofile.getLogFile());
			if (fileStore.fetchInfo().exists()) {
				long modified = fileStore.fetchInfo().getLastModified();

				// only reread it if it has been modified since the last run
				if (modified != lastModified) {
					lastModified = modified;
					contents = "";
				}
				try (InputStream is = fileStore.openInputStream(EFS.NONE,
						new NullProgressMonitor());
						BufferedReader bi = new BufferedReader(
								new InputStreamReader(is))) {
					String line;
					while ((line = bi.readLine()) != null) {
						contents += line + "\n"; //$NON-NLS-1$
					}
					bi.close();
				}
			}
		} catch (FileNotFoundException e) {
			// The file doesn't exist or was erased. Try again next time.
			contents = OprofileUiMessages
					.getString("oprofiled.logreader.error.fileNotFound"); //$NON-NLS-1$
		} catch (IOException e) {
			// Error reading log. Try again next time.
			lastModified = 0;
			contents = OprofileUiMessages
					.getString("oprofiled.logreader.error.io"); //$NON-NLS-1$
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public String getLogContents() {
		return contents;
	}


	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		this.run();
	}
}

/**
 * A custom dialog box to display the oprofiled log file.
 */
class OprofiledLogDialog extends MessageDialog {
	//string to contain the log file
	String textContent = null;

	final int GRID_WIDTH = 350;
	final int GRID_HEIGHT = 400;

	public OprofiledLogDialog (Shell parentShell, String dialogMessage) {
		super(parentShell, OprofileUiMessages.getString("oprofiled.logreader.dialog.title"), null, null, MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL }, 0); //$NON-NLS-1$
		textContent = dialogMessage;
	}

	@Override
    protected Control createCustomArea(Composite parent) {
		Composite area = new Composite(parent, 0);
		Layout layout = new GridLayout(1, true);
		GridData gd = new GridData(GRID_WIDTH, GRID_HEIGHT);

		area.setLayout(layout);
		area.setLayoutData(gd);

		Text txt = new Text(area, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		txt.setText(textContent);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return area;
    }
}
