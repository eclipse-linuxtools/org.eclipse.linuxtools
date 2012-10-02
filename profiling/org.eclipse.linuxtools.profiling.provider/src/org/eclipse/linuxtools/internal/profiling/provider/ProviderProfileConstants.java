package org.eclipse.linuxtools.internal.profiling.provider;

/**
 * Container class for various constants
 */
public class ProviderProfileConstants {

	/**
	 * The plug-in id.
	 */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.provider"; //$NON-NLS-1$

	/**
	 * Profiling preferences key.
	 */
	public static final String PREFS_KEY = "provider"; //$NON-NLS-1$

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
}
