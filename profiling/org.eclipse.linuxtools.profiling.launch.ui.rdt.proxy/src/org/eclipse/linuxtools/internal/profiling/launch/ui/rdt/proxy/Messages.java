package org.eclipse.linuxtools.internal.profiling.launch.ui.rdt.proxy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.profiling.launch.ui.rdt.proxy.messages"; //$NON-NLS-1$
	public static String RDTResourceSelectorProxy_unsupported_resourceType;
	public static String RDTResourceSelectorProxy_URI_syntax_error;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
