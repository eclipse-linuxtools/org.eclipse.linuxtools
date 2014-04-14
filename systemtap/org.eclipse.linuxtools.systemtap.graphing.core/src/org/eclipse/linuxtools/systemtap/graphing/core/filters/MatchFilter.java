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

package org.eclipse.linuxtools.systemtap.graphing.core.filters;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.graphing.core.Localization;
import org.eclipse.linuxtools.systemtap.structures.Copier;



public class MatchFilter implements IDataSetFilter {
	public MatchFilter(int column, Object value, int style) {
		this.column = column;
		this.value = value;
		this.style = style;
	}

	/**
	 * Apply the MatchFilter to the passed dataset.
	 *
	 * @param data The dataset to filter.
	 *
	 * @return The filtered dataset.
	 */
	@Override
	public List<Object>[] filter(List<Object>[] data) {
		if(column < 0 || column >= data.length) {
			return null;
		}

		List<Object>[] newData = Copier.copy(data);
		boolean equals;
		for(int j, i=newData[column].size()-1; i>=0; i--) {
			equals = newData[column].get(i).toString().equals(value.toString());	//TODO: Find better equivilance method
			if((equals && REMOVE_MATCHING == (style & 1)) || (!equals && KEEP_MATCHING == (style & 1))) {
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
	 * @since 2.0
	 */
	@Override
	public String getInfo() {
		return MessageFormat.format(Localization.getString(style == KEEP_MATCHING ? "MatchFilter.Matches" : "MatchFilter.Removes"), value.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @since 2.0
	 */
	@Override
	public int getColumn() {
		return column;
	}

	private int column;
	private Object value;
	private int style;

	public static final int KEEP_MATCHING = 0;
	public static final int REMOVE_MATCHING = 1;

	public static final String ID = "org.eclipse.linuxtools.systemtap.graphing.core.filters.MatchFilter"; //$NON-NLS-1$
}
