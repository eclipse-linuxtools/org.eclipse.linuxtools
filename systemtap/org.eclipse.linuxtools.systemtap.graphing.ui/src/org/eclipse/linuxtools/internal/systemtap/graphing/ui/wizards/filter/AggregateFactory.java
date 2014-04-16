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
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.AverageAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.CountAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.IDataAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.MaxAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.MinAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.SumAggregate;



public final class AggregateFactory {

    private AggregateFactory() {}

	private static final String[] AGGREGATE_NAMES = new String[] {
		Localization.getString("AggregateFactory.AverageAggregate"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.CountAggregate"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.MaxAggregate"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.MinAggregate"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.SumAggregate") //$NON-NLS-1$
	};

	private static final String[] AGGREGATION_DESCRIPTIONS = new String[] {
		Localization.getString("AggregateFactory.AverageDescription"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.CountDescription"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.MaxDescription"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.MinDescription"), //$NON-NLS-1$
		Localization.getString("AggregateFactory.SumDescription") //$NON-NLS-1$
	};

	static final String[] AGGREGATES = new String[] {
		AverageAggregate.ID,
		CountAggregate.ID,
		MaxAggregate.ID,
		MinAggregate.ID,
		SumAggregate.ID
	};

	public static String getAggregateName(String id) {
		int index = getIndex(id);
		if(index >= 0) {
			return AGGREGATE_NAMES[index];
		}
		return null;
	}

	public static String getAggregateDescription(String id) {
		int index = getIndex(id);
		if(index >= 0) {
			return AGGREGATION_DESCRIPTIONS[index];
		}
		return null;
	}

	private static int getIndex(String id) {
		for(int i=0; i< AGGREGATES.length; i++) {
			if(id.equals(AGGREGATES[i])) {
				return i;
			}
		}
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
