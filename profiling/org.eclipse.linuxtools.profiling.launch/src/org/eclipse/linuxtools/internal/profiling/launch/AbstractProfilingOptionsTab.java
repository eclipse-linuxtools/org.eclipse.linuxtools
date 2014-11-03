/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Shared class for displaying profiling options in a single tab.
 *
 * @since 2.0
 */
public abstract class AbstractProfilingOptionsTab extends AbstractLaunchConfigurationTab {

    private String type;
    private String name;
    private String id;
    private Composite top;
    private Combo providerCombo;
    private AbstractLaunchConfigurationTab[] tabs;
    private ILaunchConfiguration initial;
    private Map<String, String> comboItems;
    private CTabFolder tabgroup;
    protected Image img;

    // if tabs are being initialized do not call performApply()
    private Map<String, Boolean> initialized = new HashMap<> ();

    /**
     * Get list of profiling providers for the user to choose from.
     *
     * @return Map of provider ids and provider tool names
     */
    protected abstract Map<String, String> getProviders();

    @Override
    public void createControl(Composite parent) {
        top = new Composite(parent, SWT.NONE);
        setControl(top);
        top.setLayout(new GridLayout(1, true));

        providerCombo = new Combo(top, SWT.READ_ONLY);
        comboItems = getProviders();
        Set<String> providerNames = comboItems.keySet();
        providerCombo.setItems(providerNames.toArray(new String[0]));

        tabgroup = new CTabFolder(top, SWT.NONE);
        tabgroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                true));

        providerCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String curProviderId = comboItems.get(providerCombo.getText());
                loadTabGroupItems(tabgroup, curProviderId);
                initializeFrom(initial);
                // Since we are calling initializeFrom manually, we have to
                // update the launch configuration dialog manually to ensure
                // initial validation on the configuration.
                updateLaunchConfigurationDialog();
                top.layout();
            }
        });
    }

    /**
     * Get the default provider id to use if one is not set for the configuration.
     *
     * @return default provider id
     */
    protected abstract String getDefaultProviderId();

    private void loadTabGroupItems(CTabFolder tabgroup, String curProviderId) {
        // dispose of old tabs and their state
        for (CTabItem item : tabgroup.getItems()) {
            item.dispose();
        }
        setErrorMessage(null);
        initialized.clear();

        ProfileLaunchConfigurationTabGroup tabGroupConfig;

        if (curProviderId == null || curProviderId.isEmpty()) {
            curProviderId = getDefaultProviderId();
        }

        // starting initialization of this tab's controls
        initialized.put(curProviderId, false);

        tabGroupConfig = ProviderFramework.getTabGroupProviderFromId(curProviderId);
        if (tabGroupConfig == null) {
            String profilingToolName = null;
            try {
                profilingToolName = initial.getAttribute(ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT, (String)null);
            } catch (CoreException e) {
                // do nothing
            }
            if (profilingToolName == null) {
                setErrorMessage(NLS.bind(Messages.ProfilingTab_specified_providerid_not_installed, curProviderId));
            } else {
                setErrorMessage(NLS.bind(Messages.ProfilingTab_specified_profiler_not_installed, profilingToolName));
            }
            return;
        }
        tabs = tabGroupConfig.getProfileTabs();
        setProvider(curProviderId);

        // Show provider name in combo.
        int itemIndex = getComboItemIndexFromId(curProviderId);
        providerCombo.select(itemIndex);

        // Set name of configuration.
        setConfigurationName(providerCombo.getText());

        // create the tab item, and load the specified tab inside
        for (ILaunchConfigurationTab tab : tabs) {
            tab.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
            CTabItem item = new CTabItem(tabgroup, SWT.NONE);
            item.setText(tab.getName());
            item.setImage(tab.getImage());

            tab.createControl(tabgroup);
            item.setControl(tab.getControl());
            tabgroup.setSelection(0);
        }
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        if (providerCombo != null && !providerCombo.getText().isEmpty()) {
            for (AbstractLaunchConfigurationTab tab : tabs) {
                tab.setDefaults(configuration);
            }
        }
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        /**
         * First time the configuration is selected.
         *
         * This is a cheap way to get access to the launch configuration. Our
         * tabs are loaded dynamically, so the tab group doesn't "know" about
         * them. We get access to this launch configuration to ensure that we
         * can properly load the widgets the first time.
         */

        // update current configuration (initial) with configuration being
        // passed in
        initial = configuration;


        // check if there exists a launch provider id in the configuration
        if (initial != null) {
            try {
                String providerId = initial.getAttribute(
                        ProviderProfileConstants.PROVIDER_CONFIG_ATT, (String)null);
                // load provider corresponding to specified ids
                loadTabGroupItems(tabgroup, providerId);
            } catch (CoreException e) {
                // continue, initialize tabs
            }
        }
        if (tabs != null) {
            for (AbstractLaunchConfigurationTab tab : tabs) {
                tab.initializeFrom(configuration);
            }
        }

        // finished initialization
        initialized.put(getProviderId(), true);
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // make sure tabs are not null, and the tab's controls have been
        // initialized.

        Boolean isInitialized = initialized.get(getProviderId());
        isInitialized = (isInitialized != null) ? isInitialized : false;

        if (tabs != null && isInitialized) {
            for (AbstractLaunchConfigurationTab tab : tabs) {
                tab.performApply(configuration);
            }
        }
    }

    /**
     * Set the provider attribute in the specified configuration.
     * @param providerId The new provider id.
     */
    private void setProvider(String providerId) {
        try {
            ILaunchConfigurationWorkingCopy wc = initial.getWorkingCopy();
            wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT,
                    providerId);
            initial = wc.doSave();
        } catch (CoreException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Set name of the launch configuration.
     *
     * @param newToolName String tool name to be appended to configuration name,
     */
    protected void setConfigurationName(String newToolName) {
        try {
            String currentToolName = initial.getAttribute(
                    ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT, ""); //$NON-NLS-1$

            // Append the new tool name as long as the current and new tool
            // names are different.
            if (newToolName != null && !newToolName.isEmpty()
                    && !currentToolName.equals(newToolName)) {

                String projectName = initial.getAttribute(
                        ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$

                // String of the form <project name> [<tool name>].
                String newConfigurationName = ProviderLaunchShortcut
                        .generateProviderConfigurationName(projectName,
                                newToolName);

                // Unique name of the form <project name> [<tool name>]{(<number>)}.
                String newUniqueToolName = getLaunchManager()
                        .generateLaunchConfigurationName(newConfigurationName);

                // Save changes in current configuration.
                ILaunchConfigurationWorkingCopy wc = initial.getWorkingCopy();
                wc.rename(newUniqueToolName);
                wc.setAttribute(
                        ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT,
                        newToolName);
                initial = wc.doSave();

                // Set name field in launch configuration dialog to avoid the
                // new configuration name from being overwritten.
                getLaunchConfigurationDialog().setName(newUniqueToolName);
            }
        } catch (CoreException e) {
            // If unable to set the name, leave the original name as is.
        }
    }

    /**
     * Get the provider ID for the provider of the currently loaded
     * configuration.
     *
     * @return the provider ID or an empty string if the configuration
     * has no provider ID defined.
     */
    private String getProviderId() {
        try {
            return initial.getAttribute(
                    ProviderProfileConstants.PROVIDER_CONFIG_ATT, ""); //$NON-NLS-1$
        } catch (CoreException e) {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Get Combo item name from specified id
     *
     * @param id provider id
     * @return name of item, <code>null</code> if no entry found with given id.
     */
    private String getComboItemNameFromId(String id) {
        for (Entry<String, String> entry : comboItems.entrySet()) {
            if (id.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get index of specific name in the combo items list.
     *
     * @param name Name of item
     * @return Index of given name, -1 if it not found.
     */
    private int getItemIndex(String name) {
        int itemCount = providerCombo.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            if (providerCombo.getItem(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get index of specific id in the provider combo items list
     *
     * @param id Combo item id.
     * @return index of given id in provider combo items list, -1 if it not found.
     */
    private int getComboItemIndexFromId(String id) {
        String providerName = getComboItemNameFromId(id);
        return getItemIndex(providerName);
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        String provider;
        try {
            provider = config.getAttribute(
                    ProviderProfileConstants.PROVIDER_CONFIG_ATT, ""); //$NON-NLS-1$
        } catch (CoreException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        if (provider.isEmpty()) {
            setErrorMessage(Messages.ProfilingTab_providerid_not_found);
            return false;
        }

        Boolean isInitialized = initialized.get(getProviderId());

        if (isInitialized) {
            // Tabs should not be null after initialization.
            if (tabs == null) {
                return false;
            }

            // Validate tab configurations of underlying tool.
            for (AbstractLaunchConfigurationTab tab : tabs) {
                if (!tab.isValid(config)) {
                    setErrorMessage(tab.getErrorMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get name of profiling type that used for this tab.
     *
     * @return String profiling name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name for this tab.
     *
     * @param name New tab name.
     */
    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Set the id for this tab.
     *
     * @param id New id of the tab.
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * Get profiling type of the configuration.
     *
     * @return String profiling type this plug-in supports.
     */
    protected String getProfilingType() {
        return type;
    }

    /**
     * Set profiling type of configuration.
     *
     * @param type New profiling type.
     */
    protected void setProfilingType(String type) {
        this.type = type;
    }

    @Override
    public Image getImage() {
        return img;
    }

    /**
     * Set the image for this tab.
     *
     * @param img New image.
     */
    public void setImage(Image img) {
        this.img = img;
    }

    @Override
    public void dispose() {
        if (img != null) {
            img.dispose();
        }
        super.dispose();
    }
}

