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
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.AverageAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.CountAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.IDataAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.MaxAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.MinAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.SumAggregate;



public final class AggregateFactory {
	private static final String[] aggregateNames = new String[] {
		Localization.getString("AggregateFactory.AverageAggregate"),
		Localization.getString("AggregateFactory.CountAggregate"),
		Localization.getString("AggregateFactory.MaxAggregate"),
		Localization.getString("AggregateFactory.MinAggregate"),
		Localization.getString("AggregateFactory.SumAggregate")
	};

	private static final String[] aggregateDescriptions = new String[] {
		Localization.getString("AggregateFactory.AverageDescription"),
		Localization.getString("AggregateFactory.CountDescription"),
		Localization.getString("AggregateFactory.MaxDescription"),
		Localization.getString("AggregateFactory.MinDescription"),
		Localization.getString("AggregateFactory.SumDescription")
	};

	public static final String[] aggregateIDs = new String[] {
		AverageAggregate.ID,
		CountAggregate.ID,
		MaxAggregate.ID,
		MinAggregate.ID,
		SumAggregate.ID
	};
	
	public static String getAggregateName(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return aggregateNames[index];
		return null;
	}
	
	public static String getAggregateDescription(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return aggregateDescriptions[index];
		return null;
	}
	
	private static int getIndex(String id) {
		for(int i=0; i< aggregateIDs.length; i++)
			if(id.equals(aggregateIDs[i]))
				return i;
		
		return -1;
	}
	
	public static final IDataAggregate createAggregate(String id) {
		switch(getIndex(id)) {
			case 0:
				return new AverageAggregate();
			case 1:
				return new CountAggregate();
			case 2:
				return new MaxAggregate();
			case 3:
				return new MinAggregate();
			case 4:
				return new SumAggregate();
		}
		return null;
	}
}
