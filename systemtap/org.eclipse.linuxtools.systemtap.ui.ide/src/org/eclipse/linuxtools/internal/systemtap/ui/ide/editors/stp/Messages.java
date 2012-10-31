package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.messages"; //$NON-NLS-1$
	public static String STPCompletionProcessor_global;
	public static String STPCompletionProcessor_probe;
	public static String STPCompletionProcessor_function;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
