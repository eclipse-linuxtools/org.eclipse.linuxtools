package org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.dialogs.messages"; //$NON-NLS-1$
	public static String ScriptDetails_Choose_Directory;
	public static String ScriptDetails_Directory;
	public static String ScriptDetails_Script;
	public static String ScriptDetails_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
