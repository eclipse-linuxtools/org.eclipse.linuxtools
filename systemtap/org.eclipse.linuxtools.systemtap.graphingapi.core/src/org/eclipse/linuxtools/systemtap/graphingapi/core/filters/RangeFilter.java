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

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.core.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.NumberType;
import org.eclipse.linuxtools.systemtap.structures.Copier;



public class RangeFilter implements IDataSetFilter {
	public RangeFilter(int column, Number lowerBound, Number upperBound, int style) {
		this.column = column;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		if (lowerBound == null || upperBound == null) {
			this.style = style & INCLUSIVE;
		} else {
			this.style = style;
		}
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
		if(column < 0 || column >= data.length) {
			return null;
		}

		ArrayList<Object>[] newData = Copier.copy(data);
		for(int j,i=newData[column].size()-1; i>=0; i--) {
			if(!inBounds(NumberType.obj2num(newData[column].get(i)))) {
				for(j=0; j<newData.length; j++) {
					newData[j].remove(i);
				}
			}
		}
		return newData;
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * Verify that the number passed is in the bounds of the created filter.
	 *
	 * @param num The number to verify.
	 *
	 * @return True if the number is within bounds.
	 */
	private boolean inBounds(Number num) {
		if (INSIDE_BOUNDS == (style & 1)) {
			if(INCLUSIVE == (style & 2)) {
				if((upperBound != null && num.doubleValue() > upperBound.doubleValue())
						|| (lowerBound != null && num.doubleValue() < lowerBound.doubleValue())) {
					return false;
				}
			} else {
				if((upperBound != null && num.doubleValue() >= upperBound.doubleValue())
						|| (lowerBound != null && num.doubleValue() <= lowerBound.doubleValue())) {
					return false;
				}
			}
		} else {
			if(INCLUSIVE == (style & 2)) {
				if((upperBound != null && num.doubleValue() < upperBound.doubleValue())
						&& (lowerBound != null && num.doubleValue() > lowerBound.doubleValue())) {
					return false;
				}
			} else {
				if((upperBound != null && num.doubleValue() <= upperBound.doubleValue())
						&& (lowerBound != null && num.doubleValue() >= lowerBound.doubleValue())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @since 2.0
	 */
	@Override
	public String getInfo() {
		boolean insideBounds = INSIDE_BOUNDS == (style & 1);
		boolean inclusive = INCLUSIVE == (style & 2);
		String info;
		if (lowerBound != null && upperBound != null) {
			info = MessageFormat.format(Localization.getString(insideBounds ?
					"RangeFilter.Inside" : "RangeFilter.Outside"), lowerBound.toString(), upperBound.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			return inclusive ? MessageFormat.format(Localization.getString("RangeFilter.Inclusive"), info) : info; //$NON-NLS-1$
		} else {
			boolean lower = lowerBound != null;
			info = (lower ? lowerBound : upperBound).toString();
			return MessageFormat.format(Localization.getString(lower ?
					"RangeFilter.GreaterThan" : "RangeFilter.LessThan"), !inclusive ? //$NON-NLS-1$ //$NON-NLS-2$
							info : MessageFormat.format(Localization.getString("RangeFilter.EqualTo"), info)); //$NON-NLS-1$
		}
	}

	/**
	 * @since 2.0
	 */
	@Override
	public int getColumn() {
		return column;
	}

	private int column;
	private Number upperBound;
	private Number lowerBound;
	private int style;

	public static final int INSIDE_BOUNDS = 0;
	public static final int OUTSIDE_BOUNDS = 1;

	public static final int INCLUSIVE = 2;
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.core.filters.RangeFilter"; //$NON-NLS-1$
}
