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

package org.eclipse.linuxtools.systemtap.graphingapi.core.filters;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.core.GraphingAPINonUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.core.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.AverageAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.CountAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.IDataAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.MaxAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.MinAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.SumAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.NumberType;
import org.eclipse.linuxtools.systemtap.structures.Copier;


public class UniqueFilter implements IDataSetFilter {
	public UniqueFilter(int column, IDataAggregate aggregate) {
		this.column = column;
		this.aggregate = aggregate;
	}

	/**
	 * Verify that the number passed is in the bounds of the created filter.
	 *
	 * @param data The data to verify.
	 *
	 * @return True if the number is within bounds.
	 */
	@Override
	public ArrayList<Object>[] filter(ArrayList<Object>[] data) {
		if(column < 0 || column >= data.length) {
			return null;
		}

		ArrayList<Object>[] newData = Copier.copy(data);
		ArrayList<Object>[] aggregates = GraphingAPINonUIPlugin.createArrayList(newData.length, new Object());
		for(int i=0; i<aggregates.length;i++) {
			aggregates[i] = new ArrayList<>();
		}

		for(int k,j,i=newData[0].size()-1; i>=0; i--) {
			for(j=i-1; j>=0; j--) {
				if(newData[column].get(i).toString().equals(newData[column].get(j).toString())) {	//TODO: Find better equivilance method
					i--;
					for(k=0; k<newData.length; k++) {
						aggregates[k].add(newData[k].get(j));
						newData[k].remove(j);
					}
				}
			}
			for(k=0; k<newData.length; k++) {
				aggregates[k].add(newData[k].get(i));
				if(k!=column) {
					newData[k].remove(i);
					try {
						newData[k].add(i, aggregate.aggregate(NumberType.cleanObj2Num(aggregates[k].toArray())));
					} catch(NumberFormatException nfe) {
						newData[k].add(i, columnMerge(aggregates[k].toArray()));
					}
				}
				aggregates[k].clear();
			}
		}
		return newData;
	}

	/**
	 * Merges two passed columns into a StringBuilder object.
	 *
	 * @param col The columns to merge.
	 *
	 * @return The StringBuilder object made as a result of the merge.
	 */
	private String columnMerge(Object[] col) {
		StringBuilder sb = new StringBuilder(col[0].toString());
		for(int i=1; i<col.length; i++) {
			if(sb.indexOf(col[i].toString()) < 0) {
				sb.append("/" + col[i].toString()); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * @since 2.0
	 */
	@Override
	public String getInfo() {
		// Return the aggregate name without referencing outside projects.
		if (aggregate.getID().equals(AverageAggregate.ID)) {
			return Localization.getString("UniqueFilter.Average"); //$NON-NLS-1$
		} else if (aggregate.getID().equals(CountAggregate.ID)) {
			return Localization.getString("UniqueFilter.Count"); //$NON-NLS-1$
		} else if (aggregate.getID().equals(MaxAggregate.ID)) {
			return Localization.getString("UniqueFilter.Max"); //$NON-NLS-1$
		} else if (aggregate.getID().equals(MinAggregate.ID)) {
			return Localization.getString("UniqueFilter.Min"); //$NON-NLS-1$
		} else if (aggregate.getID().equals(SumAggregate.ID)) {
			return Localization.getString("UniqueFilter.Sum"); //$NON-NLS-1$
		}
		return Localization.getString("UniqueFilter.Other"); //$NON-NLS-1$
	}

	/**
	 * @since 2.0
	 */
	@Override
	public int getColumn() {
		return column;
	}

	private int column;
	private IDataAggregate aggregate;
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.core.filters.UniqueFilter"; //$NON-NLS-1$
}