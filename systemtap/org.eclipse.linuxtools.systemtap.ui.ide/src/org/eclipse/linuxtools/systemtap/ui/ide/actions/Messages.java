package org.eclipse.linuxtools.systemtap.ui.ide.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.ui.ide.actions.messages"; //$NON-NLS-1$
	public static String ScriptRunAction_InvalidScriptTitle;
	public static String ScriptRunAction_InvalidScriptTMessage;
	public static String TempFileAction_errorDialogTitle;
	public static String RunScriptChartAction_couldNotSwitchToGraphicPerspective;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
