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
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoChecksums;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoCompressionTypes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Keep track of the options to be passed to the command line when
 * executing the createrepo command.
 */
public class CreaterepoPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private Group generalGroup;
	private BooleanFieldEditor bfeIncludeChecksum;
	private BooleanFieldEditor bfeSQLDB;
	private BooleanFieldEditor bfeIgnoreSymlinks;
	private BooleanFieldEditor bfePrettyXML;
	private IntegerFieldEditor ifeSpawns;

	private Group updatesGroup;
	private BooleanFieldEditor bfeSameFilename;

	private Group changelogGroup;
	private IntegerFieldEditor ifeChangelogLimit;

	private RadioGroupFieldEditor rgfeChecksums;
	private RadioGroupFieldEditor rgfeCompressionTypes;

	public CreaterepoPreferencePage() {
		super(GRID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
		setDescription(Messages.CreaterepoPreferencePage_description);
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
		Composite parent = getFieldEditorParent();
		GridData data = new GridData();
		// general group
		generalGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		generalGroup.setText(Messages.CreaterepoPreferencePage_generalGroupLabel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(generalGroup);

		// generate unique metadata filenames
		bfeIncludeChecksum = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
				Messages.CreaterepoPreferencePage_booleanChecksumName, generalGroup);
		bfeIncludeChecksum.load();
		bfeIncludeChecksum.setPropertyChangeListener(this);
		addField(bfeIncludeChecksum);

		// generate sqlite databases
		bfeSQLDB = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
				Messages.CreaterepoPreferencePage_booleanGenerateSQLDB, generalGroup);
		bfeSQLDB.load();
		bfeSQLDB.setPropertyChangeListener(this);
		addField(bfeSQLDB);

		// ignore symlinks for packages
		bfeIgnoreSymlinks = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
				Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks, generalGroup);
		bfeIgnoreSymlinks.load();
		bfeIgnoreSymlinks.setPropertyChangeListener(this);
		addField(bfeIgnoreSymlinks);

		// output files in pretty xml format
		bfePrettyXML = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
				Messages.CreaterepoPreferencePage_booleanPrettyXML, generalGroup);
		bfePrettyXML.load();
		bfePrettyXML.setPropertyChangeListener(this);
		addField(bfePrettyXML);

		// number of workers
		ifeSpawns = new IntegerFieldEditor(CreaterepoPreferenceConstants.PREF_WORKERS,
				Messages.CreaterepoPreferencePage_numWorkers, generalGroup);
		// more than 128 is alot. limiting. (createrepo warning)
		ifeSpawns.setValidRange(0, 128);
		ifeSpawns.load();
		addField(ifeSpawns);
		updateGroupSpacing(generalGroup);

		// updates group
		updatesGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		updatesGroup.setText(Messages.CreaterepoPreferencePage_updateGroupLabel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).hint(10, SWT.DEFAULT).applyTo(updatesGroup);

		// don't generate repo metadata, if their timestamps are newer than its rpms
		bfeSameFilename = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_CHECK_TS,
				Messages.CreaterepoPreferencePage_booleanCheckTS, updatesGroup);
		bfeSameFilename.load();
		bfeSameFilename.setPropertyChangeListener(this);
		addField(bfeSameFilename);

		// note of caution
		Label lblCheckTs = new Label(updatesGroup, SWT.WRAP);
		lblCheckTs.setText(Messages.CreaterepoPreferencePage_checkTSNote);
		data = new GridData();
		data.widthHint = 130;
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalIndent = 5;
		lblCheckTs.setLayoutData(data);
		updateGroupSpacing(updatesGroup);

		// changelog group
		changelogGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		changelogGroup.setText(Messages.CreaterepoPreferencePage_changelogGroupLabel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).hint(10, SWT.DEFAULT).applyTo(changelogGroup);

		// don't generate repo metadata, if their timestamps are newer than its rpms
		ifeChangelogLimit = new IntegerFieldEditor(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
				Messages.CreaterepoPreferencePage_numChangelogLimit, changelogGroup);
		ifeChangelogLimit.load();
		ifeChangelogLimit.setPropertyChangeListener(this);
		addField(ifeChangelogLimit);
		updateGroupSpacing(changelogGroup);

		Composite checksumsContainer = new Composite(parent, SWT.NONE);
		checksumsContainer.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(0, 20).grab(true, false).applyTo(checksumsContainer);
		// available checksums
		rgfeChecksums = new RadioGroupFieldEditor(CreaterepoPreferenceConstants.PREF_CHECKSUM,
				Messages.CreaterepoPreferencePage_checksumGroupLabel, 1, new String[][]{
				{ICreaterepoChecksums.SHA1, ICreaterepoChecksums.SHA1},
				{ICreaterepoChecksums.MD5, ICreaterepoChecksums.MD5},
				{ICreaterepoChecksums.SHA256, ICreaterepoChecksums.SHA256},
				{ICreaterepoChecksums.SHA512, ICreaterepoChecksums.SHA512}
		}, checksumsContainer, true);
		rgfeChecksums.load();
		rgfeChecksums.setPropertyChangeListener(this);
		addField(rgfeChecksums);

		Composite compressContainer = new Composite(parent, SWT.NONE);
		compressContainer.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(0, 20).grab(true, false).applyTo(compressContainer);
		// available compression types
		rgfeCompressionTypes = new RadioGroupFieldEditor(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE,
				Messages.CreaterepoPreferencePage_compressionGroupLabel, 1, new String[][]{
				{ICreaterepoCompressionTypes.COMPAT, ICreaterepoCompressionTypes.COMPAT},
				{ICreaterepoCompressionTypes.XZ, ICreaterepoCompressionTypes.XZ},
				{ICreaterepoCompressionTypes.GZ, ICreaterepoCompressionTypes.GZ},
				{ICreaterepoCompressionTypes.BZ2, ICreaterepoCompressionTypes.BZ2}
		}, compressContainer, true);
		rgfeCompressionTypes.load();
		rgfeCompressionTypes.setPropertyChangeListener(this);
		addField(rgfeCompressionTypes);
	}

	/**
	 * Make sure there is space above the group as well as space
	 * between the contents of the group and its border.
	 *
	 * @param group The group to update the spacing of.
	 */
	private static void updateGroupSpacing(Group group) {
		GridLayout layout = (GridLayout) group.getLayout();
		GridData data = (GridData) group.getLayoutData();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		data.verticalIndent = 20;
	}

}
