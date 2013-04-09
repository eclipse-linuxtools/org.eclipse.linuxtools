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

package org.eclipse.linuxtools.systemtap.graphingapi.core.adapters;

public interface IAdapter {
	public Number getYSeriesMax(int series, int start, int end);
	public Number getSeriesMax(int series, int start, int end);

	public String[] getLabels();
	public int getRecordCount();
	public int getSeriesCount();
	public Object[][] getData();
	public Object[][] getData(int start, int end);
}
