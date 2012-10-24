/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.provider;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.linuxtools.internal.profiling.provider.launch.Messages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class AbstractProviderPreferencesPage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage, IExecutableExtension {

	// Launch provider extension point tool information attribute
	private static final String PROVIDER_ATT_INFO = "information";

	// Launch provider extension point tool description attribute
	private static final String PROVIDER_ATT_DESC = "description";

	// Profiling type
	private String type;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		Hashtable<String, String> parameters = (Hashtable<String, String>) data;
		String profilingType = parameters
				.get(ProviderProfileConstants.INIT_DATA_TYPE_KEY);

		if (profilingType == null) {
			profilingType = "";
		}

		setProfilingType(profilingType);
	}

	public AbstractProviderPreferencesPage() {
		super(GRID);
	}

	public void init(IWorkbench workbench) {
			final IPreferenceStore store = new ScopedPreferenceStore(
					ConfigurationScope.INSTANCE, ProviderProfileConstants.PLUGIN_ID);
			setPreferenceStore(store);
	}

	public void initializeDefaultPreferences() {
			super.performDefaults();
			String providerId = ProfileLaunchShortcut
					.getDefaultLaunchShortcutProviderId(type);
			ConfigurationScope.INSTANCE.getNode(ProviderProfileConstants.PLUGIN_ID)
					.put(ProviderProfileConstants.PREFS_KEY + type, providerId);

	}

	@Override
	protected void createFieldEditors() {
		String providerId = ProfileLaunchShortcut
				.getDefaultLaunchShortcutProviderId(type);

		getPreferenceStore().setDefault(
				ProviderProfileConstants.PREFS_KEY + type, providerId);

		HashMap<String, String> map = ProfileLaunchConfigurationTabGroup
				.getProviderNamesForType(type);
		// 2d array containing launch provider names on the first column and
		// corresponding id's on the second.
		String[][] providerList = new String[map.size()][2];
		int i = 0;
		for (Entry<String, String> entry : map.entrySet()) {
			String toolId = entry.getValue();
			String toolDescription = getToolInformationFromId(toolId, PROVIDER_ATT_DESC);
			String toolName = entry.getKey();

			// Append tool description to tool name if available.
			if (toolDescription != null && !toolDescription.equals("")) {
				toolName = toolName + " " + "[" + toolDescription + "]"; //$NON-NLS-1$
			}

			providerList[i][0] = toolName;
			providerList[i][1] = toolId;
			i++;
		}

		// Create basic field editor.
		RadioGroupFieldEditor editor = new RadioGroupFieldEditor(
				ProviderProfileConstants.PREFS_KEY + type,
				Messages.ProviderPreferencesPage_1, 1, providerList,
				getFieldEditorParent(), true);
		editor.setPreferenceStore(getPreferenceStore());
		addField(editor);

		Composite radioBoxControl = editor
				.getRadioBoxControl(getFieldEditorParent());
		Control[] providerOptions = radioBoxControl.getChildren();

		// Set tool tip text on field editors.
		for (Control control : providerOptions) {
			// Get tool specific information from provider id.
			String curProviderId = (String) control.getData();
			// Set tool tip description text.
			String toolDescription = getToolInformationFromId(curProviderId,
					PROVIDER_ATT_INFO);
			if (toolDescription != null && !toolDescription.equals("")) {
				control.setToolTipText(toolDescription);
			}
		}
	}

	/**
	 * Set profiling type.
	 *
	 * @param profilingType String profiling type.
	 */
	private void setProfilingType(String profilingType) {
		type = profilingType;
	}

	/**
	 * Get content of attribute <code>attribute</code> from the launch provider
	 * with id <code>toolId</code>.
	 * 
	 * @param toolId String unique id of the tool.
	 * @return String description of tool.
	 */
	private static String getToolInformationFromId(String toolId,
			String attribute) {
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID,
						"launchProvider"); //$NON-NLS-1$
		IConfigurationElement[] configs = extPoint.getConfigurationElements();
		for (IConfigurationElement config : configs) {
			if (config.getName().equals("provider")) { //$NON-NLS-1$
				String currentId = config.getAttribute("id"); //$NON-NLS-1$
				String currentToolDescription = config.getAttribute(attribute); //$NON-NLS-1$
				if (currentId != null && currentToolDescription != null
						&& currentId.equals(toolId)) {
					return currentToolDescription;
				}
			}
		}
		return null;
	}
}
