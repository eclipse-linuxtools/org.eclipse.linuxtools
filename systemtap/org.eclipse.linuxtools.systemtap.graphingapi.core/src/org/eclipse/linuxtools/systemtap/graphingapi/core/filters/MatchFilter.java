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

import org.eclipse.linuxtools.systemtap.structures.Copier;
import org.eclipse.ui.IMemento;



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
	public ArrayList<Object>[] filter(ArrayList<Object>[] data) {
		if(column < 0 || column >= data.length)
			return null;

		ArrayList<Object>[] newData = Copier.copy(data);
		boolean equals;
		for(int j, i=newData[column].size()-1; i>=0; i--) {
			equals = newData[column].get(i).toString().equals(value.toString());	//TODO: Find better equivilance method
			if((equals && REMOVE_MATCHING == (style & 1)) || (!equals && KEEP_MATCHING == (style & 1))) {
				for(j=0; j<newData.length; j++)
					newData[j].remove(i);
			}
		}
		return newData;
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * Preserve what filter was applied.
	 *
	 * @param parent Parent object of the new child Memento to create.
	 */
	@Override
	public void writeXML(IMemento parent) {
		IMemento child = parent.createChild("Filter", ID); //$NON-NLS-1$
		child.putInteger("column", column); //$NON-NLS-1$
		child.putString("value", value.toString()); //$NON-NLS-1$
		child.putInteger("style", style); //$NON-NLS-1$
	}

	private int column;
	private Object value;
	private int style;

	public static final int KEEP_MATCHING = 0;
	public static final int REMOVE_MATCHING = 1;

	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.core.filters.MatchFilter"; //$NON-NLS-1$
}
