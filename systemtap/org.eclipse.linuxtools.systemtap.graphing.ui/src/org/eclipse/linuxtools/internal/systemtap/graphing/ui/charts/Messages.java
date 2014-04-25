package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.messages"; //$NON-NLS-1$
    public static String AbstractChartWithAxisBuilder_ToolTipCoords;
    public static String BarChartBuilder_LabelTrimTag;
    public static String BarChartBuilder_ToolTipCoords;
    public static String PieChartBuilder_ToolTipCoords;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
