/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverLibrary;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.ParseDevHelp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class LibHoverPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private final static String DEVHELP_DIR = "Libhover.Devhelp.Directory"; //$NON-NLS-1$
	private final static String GENERATE = "Libhover.Devhelp.Generate.lbl"; //$NON-NLS-1$
	private final static String REGENERATE_MSG = "Libhover.Devhelp.Regenerate.msg"; //$NON-NLS-1$
	private final static String TITLE = "Libhover.Devhelp.Preference.title"; //$NON-NLS-1$

	private Button generateButton;

	public LibHoverPreferencePage() {
		super(GRID);
		setPreferenceStore(DevHelpPlugin.getDefault().getPreferenceStore());
	}

	private static class DevhelpStringFieldEditor extends DirectoryFieldEditor {
		public DevhelpStringFieldEditor(String name, String labelText,
				Composite parent) {
			super(name, labelText, parent);
			setFilterPath(new File(DevHelpPlugin.getDefault().getPreferenceStore().getString(name)));
		}

	}

	private synchronized void regenerate() {
		generateButton.setEnabled(false);
		Job k = new Job(LibHoverMessages.getString(REGENERATE_MSG)) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
				ParseDevHelp.DevHelpParser p =
					new ParseDevHelp.DevHelpParser(ps.getString(PreferenceConstants.DEVHELP_DIRECTORY));
				LibHoverInfo hover = p.parse(monitor);
				// Update the devhelp library info if it is on library list
				Collection<LibHoverLibrary> libs = LibHover.getLibraries();
				for (LibHoverLibrary l : libs) {
					if (l.getName().equals("devhelp")) { //$NON-NLS-1$
						l.setHoverinfo(hover);
						break;
					}
				}
				try {
					// Now, output the LibHoverInfo for caching later
					IPath location = LibhoverPlugin.getDefault().getStateLocation().append("C"); //$NON-NLS-1$
					File ldir = new File(location.toOSString());
					ldir.mkdir();
					location = location.append("devhelp.libhover"); //$NON-NLS-1$
					try (FileOutputStream f = new FileOutputStream(
							location.toOSString());
							ObjectOutputStream out = new ObjectOutputStream(f)) {
						out.writeObject(hover);
					}
					monitor.done();
				} catch(IOException e) {
					monitor.done();
					return new Status(IStatus.ERROR, DevHelpPlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
				}


				return Status.OK_STATUS;
			}

		};
		k.setUser(true);
		k.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						generateButton.setEnabled(true);
					}
				});
			}
		});
		k.schedule();
	}

	@Override
	protected void contributeButtons(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		generateButton = new Button(parent, SWT.NONE);
		generateButton.setFont(parent.getFont());
		generateButton.setText(LibHoverMessages.getString(GENERATE));
		generateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				regenerate();
			}
		});
		generateButton.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				generateButton = null;
			}
		});
		GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        int widthHint = convertHorizontalDLUsToPixels(
                IDialogConstants.BUTTON_WIDTH);
        gd.widthHint = Math.max(widthHint, generateButton.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x);

		generateButton.setLayoutData(gd);
    }

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(
				new LabelFieldEditor(
						getFieldEditorParent(),
						LibHoverMessages.getString(TITLE)));
		addField(
				new DevhelpStringFieldEditor(
						PreferenceConstants.DEVHELP_DIRECTORY,
						LibHoverMessages.getString(DEVHELP_DIR),
						getFieldEditorParent()));


	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

}