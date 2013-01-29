package org.eclipse.linuxtools.internal.profiling.launch.provider;
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

/**
 * Container class for various constants
 */
public class ProviderProfileConstants {

	/**
	 * The plug-in id.
	 */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.launch"; //$NON-NLS-1$

	/**
	 * Profiling preferences key.
	 */
	public static final String PREFS_KEY = "provider"; //$NON-NLS-1$

	/**
	 * Project attribute to indicate project is overriding settings
	 */
	public static final String USE_PROJECT_SETTINGS = "use_project";

	/**
	 * Key to acquire the extension point class parameter containing the
	 * profiling type.
	 */
	public static final String INIT_DATA_TYPE_KEY = "type"; //$NON-NLS-1$

	/**
	 * Key to acquire the extension point class parameter containing the name to
	 * be used by the options tab.
	 */
	public static final String INIT_DATA_NAME_KEY = "name"; //$NON-NLS-1$

	/**
	 * Key to acquire the extension point class parameter containing the
	 * configuration type identifier.
	 */
	public static final String INIT_DATA_CONFIG_ID_KEY = "configurationId"; //$NON-NLS-1$

	/**
	 * Key to acquire the provider launch configuration attribute.
	 */
	public static final String PROVIDER_CONFIG_ATT = "provider"; //$NON-NLS-1$

	/**
	 * Key to acquire the tool name launch configuration attribute.
	 */
	public static final String PROVIDER_CONFIG_TOOLNAME_ATT = "toolname"; //$NON-NLS-1$
}
