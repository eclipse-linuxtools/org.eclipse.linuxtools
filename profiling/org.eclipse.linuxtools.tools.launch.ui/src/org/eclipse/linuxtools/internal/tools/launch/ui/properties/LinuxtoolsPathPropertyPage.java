/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *    Rafael Peria de Sene <rpsene@br.ibm.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tools.launch.ui.properties;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.tools.launch.core.LaunchCoreConstants;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * <p>
 * Preference page and property page implementation for configuring the Linuxtools Path property.
 * </p>
 *
 * <p>
 * In this property page, it is possible to change the PATH used to run LinuxTools commands.
 * This is necessary for users that want to use tools not located in system PATH.
 * With this page you can, for example, have 2 different versions of valgrind installed in your
 * system and select which one will be used to profile your application.
 * </p>
 *
 * @author Otavio Pontes
 */
public class LinuxtoolsPathPropertyPage extends PropertyPage {
    public static final String LINUXTOOLS_PATH_COMBO_NAME = LaunchCoreConstants.PLUGIN_ID + ".LinuxtoolsPathCombo"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_EXT_POINT = "LinuxtoolsPathOptions"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_OPTION = "option"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_OPTION_NAME = "name"; //$NON-NLS-1$
    private static final String LINUXTOOLS_PATH_OPTION_PATH = "path"; //$NON-NLS-1$

    private static final String[][] DEFAULT_PATHS= {
                {"Custom", ""}, //$NON-NLS-1$  //$NON-NLS-2$
    };
    private StringFieldEditor linuxtoolsPath;
    private StringFieldEditor linuxtoolsPrefix;
    private CustomComboFieldEditor linuxtoolsPathCombo;
    private IAdaptable element = null;
    private Composite result;
    private Button systemEnvButton, customButton;
    private boolean customSelected;
    private boolean initialized = false;

    private String [][]fillPaths() {
        LinkedList<String[]> list = new LinkedList<>();
        for (String[] t : DEFAULT_PATHS) {
            list.add(t);
        }

        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchCoreConstants.PLUGIN_ID, LINUXTOOLS_PATH_EXT_POINT);
        IConfigurationElement[] configs = extPoint.getConfigurationElements();
        for (IConfigurationElement config : configs)
            if (config.getName().equals(LINUXTOOLS_PATH_OPTION)) {
                String path = config.getAttribute(LINUXTOOLS_PATH_OPTION_PATH);
                String name = config.getAttribute(LINUXTOOLS_PATH_OPTION_NAME);
                list.add(new String[]{name, path});
            }
        return list.toArray(new String[0][0]);
    }

    @Override
    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);

        result= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth= 0;
        layout.numColumns= 1;
        result.setLayout(layout);
        String paths[][] = fillPaths();

        //defaults
        getPreferenceStore().setDefault(LaunchCoreConstants.LINUXTOOLS_PATH_SYSTEM_NAME, LinuxtoolsPathProperty.getInstance().getLinuxtoolsPathSystemDefault());
        getPreferenceStore().setDefault(LINUXTOOLS_PATH_COMBO_NAME, LinuxtoolsPathProperty.getInstance().getLinuxtoolsPathDefault());

        // Add radio buttons
        Composite radios = new Composite(result, SWT.NONE);
        GridLayout layoutRadios= new GridLayout();
        layoutRadios.marginWidth= 0;
        layoutRadios.numColumns= 1;
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        radios.setLayout(layoutRadios);
        radios.setLayoutData(gridData);

        boolean systemPathSelected = getPreferenceStore().getBoolean(LaunchCoreConstants.LINUXTOOLS_PATH_SYSTEM_NAME);
        systemEnvButton = new Button(radios, SWT.RADIO);
        systemEnvButton.setText(Messages.LINUXTOOLS_PATH_SYSTEM_ENV);
        systemEnvButton.setSelection(systemPathSelected);
        systemEnvButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> updateOptionsEnable()));

        customButton = new Button(radios, SWT.RADIO);
        customButton.setText(Messages.LINUXTOOLS_PATH_CUSTOM);
        customButton.setToolTipText(Messages.LINUXTOOLS_PATH_CUSTOM_TOOLTIP);
        customButton.setSelection(!systemPathSelected);

        //Add combo box
        linuxtoolsPathCombo = new CustomComboFieldEditor(
                                    LINUXTOOLS_PATH_COMBO_NAME,
                                    Messages.LINUXTOOLS_PATH_COMBO,
                                    paths,
                                    result);
        linuxtoolsPathCombo.setPage(this);
        linuxtoolsPathCombo.setPreferenceStore(getPreferenceStore());
        linuxtoolsPathCombo.load();
        linuxtoolsPathCombo.setPropertyChangeListener(event -> {
		    customSelected = event.getNewValue().toString().equals(""); //$NON-NLS-1$
		    if (!customSelected){
		        linuxtoolsPath.setStringValue(event.getNewValue().toString());
		    }
		    updateOptionsEnable();
		});

        //Add textbox
        linuxtoolsPath = new StringFieldEditor(
                LaunchCoreConstants.LINUXTOOLS_PATH_NAME,
                Messages.LINUXTOOLS_PATH, result);

        linuxtoolsPath.setPage(this);
        linuxtoolsPath.setPreferenceStore(getPreferenceStore());
        linuxtoolsPath.getTextControl(result).setToolTipText(Messages.LINUXTOOLS_PATH_TOOLTIP);
        
        
        
        //Add textbox
		linuxtoolsPrefix = new StringFieldEditor(LaunchCoreConstants.LINUXTOOLS_PREFIX_NAME,Messages.LINUXTOOLS_PREFIX_COMBO, result);
		linuxtoolsPrefix.setPage(this);
		linuxtoolsPrefix.setPreferenceStore(getPreferenceStore());
		linuxtoolsPrefix.getTextControl(result).setToolTipText(Messages.LINUXTOOLS_PREFIX_TOOLTIP);
		
		getPreferenceStore().setDefault(LaunchCoreConstants.LINUXTOOLS_PREFIX_NAME, LinuxtoolsPathProperty.getInstance().getLinuxtoolsPrefixDefault());
		linuxtoolsPrefix.load();

        String selected = getPreferenceStore().getString(LINUXTOOLS_PATH_COMBO_NAME);
        customSelected = selected.equals(""); //$NON-NLS-1$
        getPreferenceStore().setDefault(LaunchCoreConstants.LINUXTOOLS_PATH_NAME, LinuxtoolsPathProperty.getInstance().getLinuxtoolsPathDefault());
        linuxtoolsPath.load();
        linuxtoolsPathCombo.setSelectedValue(linuxtoolsPath.getStringValue());
        Dialog.applyDialogFont(result);
        updateOptionsEnable();
        initialized = true;
        return result;
    }

    private void updateOptionsEnable() {
        if (systemEnvButton.getSelection()) {
            linuxtoolsPath.setEnabled(false, result);
            linuxtoolsPathCombo.setEnabled(false, result);
        } else {
            linuxtoolsPath.setEnabled(customSelected, result);
            linuxtoolsPathCombo.setEnabled(true, result);
        }
    }

    @Override
    protected void performDefaults() {
    	if (initialized) {
    		linuxtoolsPath.loadDefault();
    		linuxtoolsPrefix.loadDefault();
    		linuxtoolsPathCombo.loadDefault();
    		customButton.setSelection(!LinuxtoolsPathProperty.getInstance().getLinuxtoolsPathSystemDefault());
    		systemEnvButton.setSelection(LinuxtoolsPathProperty.getInstance().getLinuxtoolsPathSystemDefault());
    		updateOptionsEnable();
    	}
    }

    @Override
    public boolean performOk() {
    	if (initialized) {
    		linuxtoolsPath.store();
    		linuxtoolsPrefix.store();
    		linuxtoolsPathCombo.store();
    		getPreferenceStore().setValue(LaunchCoreConstants.LINUXTOOLS_PATH_SYSTEM_NAME, systemEnvButton.getSelection());
    	}
        return super.performOk();
    }

    @Override
    protected void performApply() {
    	if (initialized) {
    		linuxtoolsPath.store();
    		linuxtoolsPrefix.store();
    		linuxtoolsPathCombo.store();
    		getPreferenceStore().setValue(LaunchCoreConstants.LINUXTOOLS_PATH_SYSTEM_NAME, systemEnvButton.getSelection());
    	}
        super.performApply();
    }

    /**
     * Receives the object that owns the properties shown in this property page.
     *
     * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public void setElement(IAdaptable element) {
		this.element = element;
		IAdaptable e = getElement();
		if (e != null) {
			setPreferenceStore(
					new ScopedPreferenceStore(new ProjectScope((IProject) e), LaunchCoreConstants.PLUGIN_ID));
		}
    }

    /**
     * Delivers the object that owns the properties shown in this property page.
     *
     * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
     */
    @Override
    public IAdaptable getElement() {
        if (element == null) {
            return element;
        }
        if (!(element instanceof IProject)) {
            return element.getAdapter(IProject.class);
        }
        return element;
    }
}
