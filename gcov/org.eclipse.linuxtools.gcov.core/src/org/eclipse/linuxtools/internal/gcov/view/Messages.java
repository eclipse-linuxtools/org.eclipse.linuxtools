/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.view.messages"; //$NON-NLS-1$
    public static String CovView_filter_by_name;
    public static String CovView_sort_coverage_per_file;
    public static String CovView_sort_coverage_per_folder;
    public static String CovView_sort_coverage_per_function;
    public static String CovView_type_filter_text;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
