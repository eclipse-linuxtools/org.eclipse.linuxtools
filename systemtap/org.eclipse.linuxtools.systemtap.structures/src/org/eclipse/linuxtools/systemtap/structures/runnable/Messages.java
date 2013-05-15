package org.eclipse.linuxtools.systemtap.structures.runnable;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.structures.runnable.messages"; //$NON-NLS-1$
	public static String Command_failedToRunSystemtap;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
