/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.oprofile.ui.view.OprofileView;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class OprofileUiPlugin extends AbstractUIPlugin {
    //The shared instance.
    private static OprofileUiPlugin plugin;

    private OprofileView oprofileview = null;

    public static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.ui"; //$NON-NLS-1$
    public static final String ID_OPROFILE_VIEW = PLUGIN_ID + ".OProfileView"; //$NON-NLS-1$

    private static final String ICON_PATH = "icons/"; //$NON-NLS-1$
    public static final String SESSION_ICON = ICON_PATH + "session.gif"; //$NON-NLS-1$
    public static final String EVENT_ICON = ICON_PATH + "event.gif"; //$NON-NLS-1$
    public static final String IMAGE_ICON = ICON_PATH + "image.gif"; //$NON-NLS-1$
    public static final String DEPENDENT_ICON = ICON_PATH + "dependent.gif"; //$NON-NLS-1$
    public static final String SYMBOL_ICON = ICON_PATH + "symbol.gif"; //$NON-NLS-1$
    public static final String SAMPLE_ICON = ICON_PATH + "sample.gif"; //$NON-NLS-1$
    public static final String ERROR_ICON = ICON_PATH + "error.png"; //$NON-NLS-1$

    public static final double MINIMUM_SAMPLE_PERCENTAGE = 0.0001;

    public static final String ANNOTATION_TYPE_LT_MIN_PERCENTAGE = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.min.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_LT_05 = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.05.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_LT_10 = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.10.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_LT_20 = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.20.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_LT_30 = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.30.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_LT_40 = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.40.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_LT_50 = "org.eclipse.linuxtools.oprofile.ui.annotation.lt.50.pct"; //$NON-NLS-1$
    public static final String ANNOTATION_TYPE_GT_50 = "org.eclipse.linuxtools.oprofile.ui.annotation.gt.50.pct"; //$NON-NLS-1$

    /**
     * The constructor.
     */
    public OprofileUiPlugin() {
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }


    /**
     * Returns the shared instance.
     */
    public static OprofileUiPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public OprofileView getOprofileView() {
        return oprofileview;
    }

    public void setOprofileView(OprofileView oprofileview) {
        this.oprofileview = oprofileview;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

    public static String getPercentageString(double percentage) {
        NumberFormat nf = NumberFormat.getPercentInstance();
        if (nf instanceof DecimalFormat) {
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
        }

        if (percentage < OprofileUiPlugin.MINIMUM_SAMPLE_PERCENTAGE) {
            return "<" + nf.format(OprofileUiPlugin.MINIMUM_SAMPLE_PERCENTAGE); //$NON-NLS-1$
        } else {
            return nf.format(percentage);
        }
    }
}
