package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.messages"; //$NON-NLS-1$
	public static String BarChartBuilder_LabelTrimTag;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
