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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.filter;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.MatchFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.SortFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.UniqueFilter;



public final class AvailableFilterTypes {
	private static final String[] filterNames = new String[] {
		Localization.getString("AvailableFilterTypes.MatchFilter"), //$NON-NLS-1$
		Localization.getString("AvailableFilterTypes.RangeFilter"), //$NON-NLS-1$
		Localization.getString("AvailableFilterTypes.SortFilter"), //$NON-NLS-1$
		Localization.getString("AvailableFilterTypes.UniqueFilter") //$NON-NLS-1$
	};

	private static final String[] filterDescriptions = new String[] {
		Localization.getString("AvailableFilterTypes.MatchFilterDescription"), //$NON-NLS-1$
		Localization.getString("AvailableFilterTypes.RangeFilterDescription"), //$NON-NLS-1$
		Localization.getString("AvailableFilterTypes.SortFilterDescription"), //$NON-NLS-1$
		Localization.getString("AvailableFilterTypes.UniqueFilterDescription") //$NON-NLS-1$
	};

	private static final FilterWizardPage[] filterWizards = new FilterWizardPage[] {
		new MatchFilterWizardPage(),
		new RangeFilterWizardPage(),
		new SortFilterWizardPage(),
		new UniqueFilterWizardPage()
	};

	public static final String[] filterIDs = new String[] {
		MatchFilter.ID,
		RangeFilter.ID,
		SortFilter.ID,
		UniqueFilter.ID
	};

	public static FilterWizardPage getFilterWizardPage(String id) {
		FilterWizardPage page = null;

		int index = getIndex(id);
		if(index >=0 && index < filterWizards.length)
			return filterWizards[index];

		return page;
	}

	public static String getFilterName(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return filterNames[index];
		return null;
	}

	public static String getFilterDescription(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return filterDescriptions[index];
		return null;
	}

	private static int getIndex(String id) {
		for(int i=0; i< filterIDs.length; i++) {
			if(id.equals(filterIDs[i])) {
				return i;
			}
		}

		return -1;
	}
}
