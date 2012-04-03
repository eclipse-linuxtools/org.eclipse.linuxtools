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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.filter;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.MatchFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.SortFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.UniqueFilter;
import org.eclipse.ui.IMemento;



public final class AvailableFilterTypes {
	private static final String[] filterNames = new String[] {
		Localization.getString("AvailableFilterTypes.MatchFilter"),
		Localization.getString("AvailableFilterTypes.RangeFilter"),
		Localization.getString("AvailableFilterTypes.SortFilter"),
		Localization.getString("AvailableFilterTypes.UniqueFilter")
	};

	private static final String[] filterDescriptions = new String[] {
		Localization.getString("AvailableFilterTypes.MatchFilterDescription"),
		Localization.getString("AvailableFilterTypes.RangeFilterDescription"),
		Localization.getString("AvailableFilterTypes.SortFilterDescription"),
		Localization.getString("AvailableFilterTypes.UniqueFilterDescription")
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
		int column = xml.getInteger("column").intValue();
		int style;
		
		switch(getIndex(id)) {
		case 0:
			String val = xml.getString("value");
			style = xml.getInteger("style").intValue();
			return new MatchFilter(column, val, style);
		case 1:
			style = xml.getInteger("style").intValue();
			String l = xml.getString("low");
			String h = xml.getString("high");
			
			Number low, high;
			if(l.contains("."))
				low = new Double(Double.parseDouble(l));
			else
				low = new Long(Long.parseLong(l));
			if(h.contains("."))
				high = new Double(Double.parseDouble(h));
			else
				high = new Long(Long.parseLong(h));
			
			return new RangeFilter(column, low, high, style);
		case 2:
			style = xml.getInteger("style").intValue();
			return new SortFilter(column, style);
		case 3:
			String agg = xml.getString("aggregate");
			style = xml.getInteger("style").intValue();
			return new UniqueFilter(column, AggregateFactory.createAggregate(agg), style);
		}
		return null;
	}
	
	private static int getIndex(String id) {
		for(int i=0; i< filterIDs.length; i++)
			if(id.equals(filterIDs[i]))
				return i;
		
		return -1;
	}
}
