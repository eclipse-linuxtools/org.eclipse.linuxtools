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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoChecksums;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoCompressionTypes;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Project property page for configuring the delta options for a repository.
 */
public class CreaterepoGeneralPropertyPage extends CreaterepoPropertyPage {

    private static final String linkTags = "<a>{0}</a>"; //$NON-NLS-1$

    private Button btnProjectSettings;
    private Link lnWorkspaceSettings;

    private Group generalGroup;
    private BooleanFieldEditor bfeIncludeChecksum;
    private BooleanFieldEditor bfeSQLDB;
    private BooleanFieldEditor bfeIgnoreSymlinks;
    private BooleanFieldEditor bfePrettyXML;
    private IntegerFieldEditor ifeSpawns;

    private Group updatesGroup;
    private BooleanFieldEditor bfeSameFilename;
    private Label lblCheckTs;

    private Group changelogGroup;
    private IntegerFieldEditor ifeChangelogLimit;

    private Composite checksumsContainer;
    private RadioGroupFieldEditor rgfeChecksums;
    private Composite compressContainer;
    private RadioGroupFieldEditor rgfeCompressionTypes;

    /**
     * Default Constructor. Sets the description of the property page.
     */
    public CreaterepoGeneralPropertyPage() {
        super(Messages.CreaterepoPreferencePage_description);
    }

    @Override
    protected Composite addContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        GridData layoutData = new GridData();
        composite.setLayout(layout);

        // TODO: use BooleanFieldEditor and get it to layout properly
        //        with the link (lnWorkspaceSettings)
        btnProjectSettings = new Button(composite, SWT.CHECK);
        btnProjectSettings.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                toggleEnabled();
            }
        });
        layoutData = new GridData();
        layoutData.horizontalAlignment = GridData.BEGINNING;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        btnProjectSettings.setText(Messages.CreaterepoGeneralPropertyPage_projectSettings);
        btnProjectSettings.setLayoutData(layoutData);
        btnProjectSettings.setSelection(Activator.getDefault().getPreferenceStore()
                .getBoolean(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED));
        btnProjectSettings.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                checkState();
            }
        });

        lnWorkspaceSettings = new Link(composite, SWT.NONE);
        layoutData = new GridData();
        layoutData.horizontalAlignment = SWT.END;
        lnWorkspaceSettings.setLayoutData(layoutData);
        lnWorkspaceSettings.setText(NLS.bind(linkTags, Messages.CreaterepoGeneralPropertyPage_workspaceSettings));
        lnWorkspaceSettings.setFont(parent.getFont());
        lnWorkspaceSettings.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(getShell(),
                        CREATEREPO_PREFERENCE_ID, new String[] {CREATEREPO_PREFERENCE_ID}, null);
                preferenceDialog.open();
            }
        });

        // general group
        generalGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        layout = new GridLayout(2, false);
        generalGroup.setLayout(layout);
        generalGroup.setText(Messages.CreaterepoPreferencePage_generalGroupLabel);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2,1).applyTo(generalGroup);

        // generate unique metadata filenames
        bfeIncludeChecksum = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
                Messages.CreaterepoPreferencePage_booleanChecksumName, generalGroup);
        bfeIncludeChecksum.fillIntoGrid(generalGroup, 2);
        bfeIncludeChecksum.setPreferenceStore(preferenceStore);
        bfeIncludeChecksum.setPropertyChangeListener(this);
        bfeIncludeChecksum.load();

        // generate sqlite databases
        bfeSQLDB = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
                Messages.CreaterepoPreferencePage_booleanGenerateSQLDB, generalGroup);
        bfeSQLDB.fillIntoGrid(generalGroup, 2);
        bfeSQLDB.setPreferenceStore(preferenceStore);
        bfeSQLDB.setPropertyChangeListener(this);
        bfeSQLDB.load();

        // ignore symlinks for packages
        bfeIgnoreSymlinks = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
                Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks, generalGroup);
        bfeIgnoreSymlinks.fillIntoGrid(generalGroup, 2);
        bfeIgnoreSymlinks.setPreferenceStore(preferenceStore);
        bfeIgnoreSymlinks.setPropertyChangeListener(this);
        bfeIgnoreSymlinks.load();

        // output files in pretty xml format
        bfePrettyXML = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
                Messages.CreaterepoPreferencePage_booleanPrettyXML, generalGroup);
        bfePrettyXML.fillIntoGrid(generalGroup, 2);
        bfePrettyXML.setPreferenceStore(preferenceStore);
        bfePrettyXML.setPropertyChangeListener(this);
        bfePrettyXML.load();

        // number of workers
        ifeSpawns = new IntegerFieldEditor(CreaterepoPreferenceConstants.PREF_WORKERS,
                Messages.CreaterepoPreferencePage_numWorkers, generalGroup);
        ifeSpawns.fillIntoGrid(generalGroup, 2);
        // more than 128 is alot. limiting. (createrepo warning)
        ifeSpawns.setValidRange(0, 128);
        ifeSpawns.setPreferenceStore(preferenceStore);
        ifeSpawns.setPropertyChangeListener(this);
        ifeSpawns.load();
        updateGroupSpacing(generalGroup);

        // updates group
        updatesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        updatesGroup.setText(Messages.CreaterepoPreferencePage_updateGroupLabel);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).hint(10, SWT.DEFAULT).applyTo(updatesGroup);

        // don't generate repo metadata, if their timestamps are newer than its rpms
        bfeSameFilename = new BooleanFieldEditor(CreaterepoPreferenceConstants.PREF_CHECK_TS,
                Messages.CreaterepoPreferencePage_booleanCheckTS, updatesGroup);
        bfeSameFilename.fillIntoGrid(updatesGroup, 2);
        bfeSameFilename.setPreferenceStore(preferenceStore);
        bfeSameFilename.setPropertyChangeListener(this);
        bfeSameFilename.load();

        // note of caution
        lblCheckTs = new Label(updatesGroup, SWT.WRAP);
        lblCheckTs.setText(Messages.CreaterepoPreferencePage_checkTSNote);
        layoutData = new GridData();
        layoutData.widthHint = 130;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalIndent = 5;
        lblCheckTs.setLayoutData(layoutData);
        updateGroupSpacing(updatesGroup);

        // changelog group
        changelogGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        changelogGroup.setText(Messages.CreaterepoPreferencePage_changelogGroupLabel);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).hint(10, SWT.DEFAULT).applyTo(changelogGroup);

        // don't generate repo metadata, if their timestamps are newer than its rpms
        ifeChangelogLimit = new IntegerFieldEditor(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
                Messages.CreaterepoPreferencePage_numChangelogLimit, changelogGroup);
        ifeChangelogLimit.fillIntoGrid(changelogGroup, 2);
        ifeChangelogLimit.setValidRange(0, Integer.MAX_VALUE);
        ifeChangelogLimit.setPreferenceStore(preferenceStore);
        ifeChangelogLimit.setPropertyChangeListener(this);
        ifeChangelogLimit.load();
        updateGroupSpacing(changelogGroup);

        checksumsContainer = new Composite(composite, SWT.NONE);
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
        rgfeChecksums.setPreferenceStore(preferenceStore);
        rgfeChecksums.setPropertyChangeListener(this);
        rgfeChecksums.load();

        compressContainer = new Composite(composite, SWT.NONE);
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
        rgfeCompressionTypes.setPreferenceStore(preferenceStore);
        rgfeCompressionTypes.setPropertyChangeListener(this);
        rgfeCompressionTypes.load();

        toggleEnabled();

        return composite;
    }

    @Override
    public void performDefaults() {
        // load the defaults UI
        btnProjectSettings.setSelection(Activator.getDefault().getPreferenceStore()
                .getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED));
        bfeIncludeChecksum.loadDefault();
        bfeSQLDB.loadDefault();
        bfeIgnoreSymlinks.loadDefault();
        bfePrettyXML.loadDefault();
        ifeSpawns.loadDefault();
        rgfeChecksums.loadDefault();
        bfeSameFilename.loadDefault();
        ifeChangelogLimit.loadDefault();
        rgfeChecksums.loadDefault();
        rgfeCompressionTypes.loadDefault();

        // set the defaults preferences
        Activator.getDefault().getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_GENERATE_DB);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_PRETTY_XML);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_WORKERS);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_CHECK_TS);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_CHECKSUM);
        getPreferenceStore().setToDefault(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE);

        toggleEnabled();
    }

    @Override
    public boolean performOk() {
        // only save when using project specific settings
        if (btnProjectSettings.getSelection()) {
            Activator.getDefault().getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED,
                    true);
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
                    bfeIncludeChecksum.getBooleanValue());
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
                    bfeSQLDB.getBooleanValue());
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
                    bfeIgnoreSymlinks.getBooleanValue());
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
                    bfePrettyXML.getBooleanValue());
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_WORKERS,
                    ifeSpawns.getIntValue());
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_CHECK_TS,
                    bfeSameFilename.getBooleanValue());
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
                    ifeChangelogLimit.getIntValue());

            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_CHECKSUM,
                    getSelectedRadioButton(rgfeChecksums.getRadioBoxControl(checksumsContainer),
                            rgfeChecksums.getPreferenceName()));
            getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE,
                    getSelectedRadioButton(rgfeCompressionTypes.getRadioBoxControl(compressContainer),
                            rgfeCompressionTypes.getPreferenceName()));
        } else {
            Activator.getDefault().getPreferenceStore().setValue(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED,
                    false);
        }
        return true;
    }

    /**
     * Helper method to get the selected radio button from the radio button list.
     * The default value of the preference key will be returned if nothing was found to be
     * selected.
     *
     * @param radioBoxControl The radio button list.
     * @param preferenceKey The preference key to get the default from.
     * @return The selected radio button or the default value if nothing selected.
     */
    private String getSelectedRadioButton(Composite radioBoxControl, String preferenceKey) {
        String defaultValue = getPreferenceStore().getDefaultString(preferenceKey);
        Control[] children = radioBoxControl.getChildren();
        if (children.length > 0) {
            for (Control control : children) {
                Button radioButton = (Button) control;
                if (radioButton.getSelection()) {
                    return radioButton.getText();
                }
            }
        }
        return defaultValue;
    }

    @Override
    protected void checkState() {
        // if enable project specific settings is false, then allow performOk
        if (!btnProjectSettings.getSelection()) {
            setErrorMessage(null);
            setValid(true);
        // if the spawns are invalid, disable performOk
        } else if (!ifeSpawns.isValid()) {
            setErrorMessage(ifeSpawns.getErrorMessage());
            setValid(false);
        } else if (!ifeChangelogLimit.isValid()) {
            setErrorMessage(ifeChangelogLimit.getErrorMessage());
            setValid(false);
        // otherwise, allow performOk
        } else {
            setErrorMessage(null);
            setValid(true);
        }
        toggleEnabled();
    }

    /**
     * If "Enable project specific settings" is true, so will
     * the options below it. The workspace settings link will be opposite
     * to what value the checkbox is.
     */
    private void toggleEnabled() {
        boolean enabled = btnProjectSettings.getSelection();
        lnWorkspaceSettings.setEnabled(!enabled);
        bfeIncludeChecksum.setEnabled(enabled, generalGroup);
        bfeSQLDB.setEnabled(enabled, generalGroup);
        bfeIgnoreSymlinks.setEnabled(enabled, generalGroup);
        bfePrettyXML.setEnabled(enabled, generalGroup);
        ifeSpawns.setEnabled(enabled, generalGroup);
        rgfeChecksums.setEnabled(enabled, generalGroup);
        bfeSameFilename.setEnabled(enabled, updatesGroup);
        ifeChangelogLimit.setEnabled(enabled, changelogGroup);
        rgfeChecksums.setEnabled(enabled, checksumsContainer);
        rgfeCompressionTypes.setEnabled(enabled, compressContainer);
        lblCheckTs.setEnabled(enabled);
    }

}
