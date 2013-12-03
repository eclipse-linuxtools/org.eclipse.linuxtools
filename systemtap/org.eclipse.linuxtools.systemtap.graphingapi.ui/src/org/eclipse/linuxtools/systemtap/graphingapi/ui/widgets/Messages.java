package org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.messages"; //$NON-NLS-1$
	public static String GraphContinuousControl_ZoomInLabel;
	public static String GraphContinuousControl_ZoomInTooltip;
	public static String GraphContinuousControl_ZoomOutLabel;
	public static String GraphContinuousControl_ZoomOutTooltip;
	public static String GraphContinuousXControl_ScaleMessage;
	public static String GraphContinuousXControl_ScrollMessage;
	public static String GraphContinuousYControl_ScaleMessage;
	public static String GraphContinuousYControl_ScrollMessage;
	public static String GraphDiscreteXControl_First;
	public static String GraphDiscreteXControl_Left;
	public static String GraphDiscreteXControl_ZoomIn;
	public static String GraphDiscreteXControl_ZoomOut;
	public static String GraphDiscreteXControl_All;
	public static String GraphDiscreteXControl_Right;
	public static String GraphDiscreteXControl_Last;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
