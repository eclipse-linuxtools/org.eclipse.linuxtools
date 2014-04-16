/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.wizards.filter;

import org.eclipse.linuxtools.internal.systemtap.graphing.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.MatchFilter;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.SortFilter;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.UniqueFilter;



public final class AvailableFilterTypes {

    private AvailableFilterTypes() {}

    private static final String[] FILTER_NAMES = new String[] {
        Localization.getString("AvailableFilterTypes.MatchFilter"), //$NON-NLS-1$
        Localization.getString("AvailableFilterTypes.RangeFilter"), //$NON-NLS-1$
        Localization.getString("AvailableFilterTypes.SortFilter"), //$NON-NLS-1$
        Localization.getString("AvailableFilterTypes.UniqueFilter") //$NON-NLS-1$
    };

    private static final String[] FILTER_DESCRIPTONS = new String[] {
        Localization.getString("AvailableFilterTypes.MatchFilterDescription"), //$NON-NLS-1$
        Localization.getString("AvailableFilterTypes.RangeFilterDescription"), //$NON-NLS-1$
        Localization.getString("AvailableFilterTypes.SortFilterDescription"), //$NON-NLS-1$
        Localization.getString("AvailableFilterTypes.UniqueFilterDescription") //$NON-NLS-1$
    };

    private static final FilterWizardPage[] FILTER_WIZARDS = new FilterWizardPage[] {
        new MatchFilterWizardPage(),
        new RangeFilterWizardPage(),
        new SortFilterWizardPage(),
        new UniqueFilterWizardPage()
    };

    static final String[] FILTER_IDS = new String[] {
        MatchFilter.ID,
        RangeFilter.ID,
        SortFilter.ID,
        UniqueFilter.ID
    };

    public static FilterWizardPage getFilterWizardPage(String id) {
        FilterWizardPage page = null;

        int index = getIndex(id);
        if(index >=0 && index < FILTER_WIZARDS.length) {
            return FILTER_WIZARDS[index];
        }
        return page;
    }

    public static String getFilterName(String id) {
        int index = getIndex(id);
        if(index >= 0) {
            return FILTER_NAMES[index];
        }
        return null;
    }

    public static String getFilterDescription(String id) {
        int index = getIndex(id);
        if(index >= 0) {
            return FILTER_DESCRIPTONS[index];
        }
        return null;
    }

    private static int getIndex(String id) {
        for(int i=0; i< FILTER_IDS.length; i++) {
            if(id.equals(FILTER_IDS[i])) {
                return i;
            }
        }
        return -1;
    }
}
