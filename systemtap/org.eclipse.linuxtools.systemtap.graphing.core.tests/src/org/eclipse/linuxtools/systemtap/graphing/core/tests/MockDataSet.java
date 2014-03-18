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

package org.eclipse.linuxtools.systemtap.graphing.core.tests;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowEntry;


public final class MockDataSet {
	public static RowDataSet buildDataSet(int rows, int cols) {
		String[] titles = new String[cols];
		int i;
		for(i=0; i<cols; i++)
			titles[i] = ""+ i;
		
		RowDataSet data = new RowDataSet(titles);
		RowEntry entry;
		
		int j;
		for(i=0; i<rows; i++) {
			Object[] d = new Object[cols];
			for(j=0; j< cols; j++) {
				d[j] = new Double(i*cols + j);
			}
			entry = new RowEntry();
			entry.putRow(0, d);
			data.append(entry);
		}
		return data;
	}
	
	public static ArrayList<Object>[] buildArray(int width, int height, int wrap) {
		ArrayList<Object>[] list = createArrayList(width, new Object());
		
		for(int i=0; i<width; i++) {
			list[i] = new ArrayList<>();
			for(int j=0; j<height; j++) {
				list[i].add("" + ((j+i) % wrap));
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T>[] createArrayList(int size, T instance) {
		return new ArrayList[size];
	}

	public static Integer[] buildIntegerArray(int[] arr) {
		Integer[] ints = new Integer[arr.length];
		for(int i=0; i<arr.length; i++) {
			ints[i] = Integer.valueOf(arr[i]);
		}
		return ints;
	}
	
	public static Double[] buildDoubleArray(double[] arr) {
		Double[] doubles = new Double[arr.length];
		for(int i=0; i<arr.length; i++) {
			doubles[i] = Double.valueOf(arr[i]);
		}
		return doubles;
	}
}
