package org.eclipse.linuxtools.systemtap.ui.ide.structures;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.ui.ide.structures.messages"; //$NON-NLS-1$
	public static String TapsetParser_CannotRunStapMessage;
	public static String TapsetParser_CannotRunStapTitle;
	public static String TapsetParser_ErrorRunningSystemtap;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
