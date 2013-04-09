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
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.MatchFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.SortFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.UniqueFilter;
import org.eclipse.ui.IMemento;



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

	public static IDataSetFilter getDataSetFilter(IMemento xml) {
		String id = xml.getID();
		int column = xml.getInteger("column").intValue(); //$NON-NLS-1$
		int style;

		switch(getIndex(id)) {
		case 0:
			String val = xml.getString("value"); //$NON-NLS-1$
			style = xml.getInteger("style").intValue(); //$NON-NLS-1$
			return new MatchFilter(column, val, style);
		case 1:
			style = xml.getInteger("style").intValue(); //$NON-NLS-1$
			String l = xml.getString("low"); //$NON-NLS-1$
			String h = xml.getString("high"); //$NON-NLS-1$

			Number low, high;
			if(l.contains(".")) //$NON-NLS-1$
				low = Double.valueOf(l);
			else
				low = Long.valueOf(l);
			if(h.contains(".")) //$NON-NLS-1$
				high = Double.valueOf(h);
			else
				high = Long.valueOf(h);

			return new RangeFilter(column, low, high, style);
		case 2:
			style = xml.getInteger("style").intValue(); //$NON-NLS-1$
			return new SortFilter(column, style);
		case 3:
			String agg = xml.getString("aggregate"); //$NON-NLS-1$
			style = xml.getInteger("style").intValue(); //$NON-NLS-1$
			return new UniqueFilter(column, AggregateFactory.createAggregate(agg), style);
		}
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
