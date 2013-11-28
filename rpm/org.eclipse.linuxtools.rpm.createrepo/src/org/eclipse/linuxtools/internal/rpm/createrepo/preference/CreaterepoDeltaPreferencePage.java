/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.preference;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Keep track of delta specific options and pass it onto the createrepo command
 * when executing.
 */
public class CreaterepoDeltaPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	private Composite parent;
	private BooleanFieldEditor bfeEnableDeltas;
	private IntegerFieldEditor ifeMaxNumDeltas;
	private IntegerFieldEditor ifeMaxSizeDeltas;
	private Group optionsGroup;
	private Composite enableContainer;

	public CreaterepoDeltaPreferencePage() {
		super(GRID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
		setDescription(Messages.CreaterepoDeltaPreferencePage_description);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#checkState()
	 */
	@Override
	public void checkState() {
		super.checkState();
		// catches the setValidRange rule
		if (!isValid()) {
			bfeEnableDeltas.setEnabled(false, enableContainer);
		} else {
			bfeEnableDeltas.setEnabled(true, enableContainer);
			ifeMaxNumDeltas.setEnabled(bfeEnableDeltas.getBooleanValue(), optionsGroup);
			ifeMaxSizeDeltas.setEnabled(bfeEnableDeltas.getBooleanValue(), optionsGroup);
			setErrorMessage(null);
			setValid(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		parent = getFieldEditorParent();
		GridLayout layout = new GridLayout();

		enableContainer = new Composite(parent, SWT.SHADOW_NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2,1).indent(0, 20).applyTo(enableContainer);
		bfeEnableDeltas = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE,
				Messages.CreaterepoDeltaPreferencePage_booleanEnableLabel, enableContainer);
		bfeEnableDeltas.load();
		bfeEnableDeltas.setPropertyChangeListener(this);
		addField(bfeEnableDeltas);

		optionsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.CreaterepoDeltaPreferencePage_groupLabel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(optionsGroup);

		// max deltas.
		ifeMaxNumDeltas = new IntegerFieldEditor(CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
				Messages.CreaterepoDeltaPreferencePage_maxNumberOfDeltas, optionsGroup);
		ifeMaxNumDeltas.setValidRange(0, Integer.MAX_VALUE);
		ifeMaxNumDeltas.load();
		addField(ifeMaxNumDeltas);

		// max delta size. stored in megabytes for convenience to user
		ifeMaxSizeDeltas = new IntegerFieldEditor(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
				Messages.CreaterepoDeltaPreferencePage_maxDeltaSize, optionsGroup);
		ifeMaxSizeDeltas.setValidRange(0, Integer.MAX_VALUE);
		ifeMaxSizeDeltas.load();
		addField(ifeMaxSizeDeltas);

		// spaces around the group
		layout = (GridLayout) optionsGroup.getLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
	}

}
