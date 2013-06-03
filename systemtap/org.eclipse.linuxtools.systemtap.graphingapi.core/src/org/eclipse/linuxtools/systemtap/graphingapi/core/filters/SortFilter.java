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

import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.NumberType;
import org.eclipse.linuxtools.systemtap.structures.Copier;
import org.eclipse.linuxtools.systemtap.structures.IndexedObject;
import org.eclipse.linuxtools.systemtap.structures.Sort;



public class SortFilter implements IDataSetFilter {
	public SortFilter(int column, int ordering) {
		this.column = column;
		this.style = (ordering==ASCENDING ? ASCENDING : DESCENDING);
	}

	/**
	 * Apply the RangeFilter to the passed dataset.
	 *
	 * @param data The dataset to filter.
	 *
	 * @return The filtered dataset.
	 */
	@Override
	public ArrayList<Object>[] filter(ArrayList<Object>[] data) {
		if(column < 0 || column >= data.length)
			return null;

		ArrayList<Object>[] newData = Copier.copy(data);
		IndexedObject[] items = new IndexedObject[newData[0].size()];

		try {
			for(int i=0; i<newData[column].size(); i++)
				items[i] = new IndexedObject(i, NumberType.cleanObj2Num(newData[column].get(i)));
		} catch(NumberFormatException nfe) {
			for(int i=0; i<newData[column].size(); i++)
				items[i] = new IndexedObject(i, newData[column].get(i));
		}

		Sort.quicksort(items, 0, items.length-1);

		for(int j, i=0; i<newData.length; i++) {
			for(j=0; j<items.length; j++) {
				if(DESCENDING == style)
					newData[i].add(newData[i].get(items[items.length-j-1].index));
				else
					newData[i].add(newData[i].get(items[j].index));
			}
			for(j=0; j<items.length; j++)
				newData[i].remove(0);
		}
		return newData;
	}

	@Override
	public String getID() {
		return ID;
	}

	private int column;
	private int style;

	public static final int ASCENDING = 0;
	public static final int DESCENDING = 1;
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.core.filters.SortFilter"; //$NON-NLS-1$
}
