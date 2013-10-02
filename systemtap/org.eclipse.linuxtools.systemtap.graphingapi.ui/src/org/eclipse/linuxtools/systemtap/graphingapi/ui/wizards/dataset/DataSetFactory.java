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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.FilteredRowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.FilteredTableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.datadisplay.DataGrid;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.SelectGraphAndSeriesWizard;
import org.eclipse.swt.widgets.Composite;



public final class DataSetFactory {
	public static IDataSet createDataSet(String id, String[] labels) {
		if(id.equals(RowDataSet.ID)) {
			return new RowDataSet(labels);
		} else if(id.equals(TableDataSet.ID)) {
			return new TableDataSet(labels);
		}
		return null;
	}

	public static IFilteredDataSet createFilteredDataSet(String id, String[] labels) {
		if(id.equals(RowDataSet.ID)) {
			return new FilteredRowDataSet(labels);
		} else if(id.equals(TableDataSet.ID)) {
			return new FilteredTableDataSet(labels);
		}
		return new FilteredRowDataSet(labels);
	}

	public static IFilteredDataSet createFilteredDataSet(IDataSet set) {
		if(set instanceof RowDataSet) {
			return new FilteredRowDataSet((RowDataSet)set);
		} else if(set instanceof TableDataSet) {
			return new FilteredTableDataSet((TableDataSet)set);
		}
		return null;
	}

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	public static String[] getIDs() {
		return ids;
	}

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	public static String getName(String id) {
		int index = getIndex(id);
		if(index >= 0) {
			return names[index];
		}
		return null;
	}

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	public static String getDescription(String id) {
		int index = getIndex(id);
		if(index >= 0) {
			return descriptions[index];
		}
		return null;
	}

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	public static ParsingWizardPage getParsingWizardPage(String id) {
		ParsingWizardPage page = null;

		int index = getIndex(id);
		if(index >=0 && index < dataSetWizards.length) {
			return dataSetWizards[index];
		}

		return page;
	}

	public static DataGrid getDataGrid(Composite composite, IDataSet set) {
		if(set instanceof RowDataSet) {
			return new DataGrid(composite, set, DataGrid.NONE);
		} else if(set instanceof TableDataSet) {
			return new DataGrid(composite, set, DataGrid.FULL_UPDATE);
		}

		return null;
	}

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	private static int getIndex(String id) {
		for(int i=0; i<ids.length; i++) {
			if(id.equals(ids[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	private static final String[] ids = {
		RowDataSet.ID,
		TableDataSet.ID
	};

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	private static final String[] names = {
		Localization.getString("DataSetFactory.RowDataSet"), //$NON-NLS-1$
		Localization.getString("DataSetFactory.TableDataSet") //$NON-NLS-1$
	};

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	private static final String[] descriptions = {
		Localization.getString("DataSetFactory.RowDataSetDescription") + //$NON-NLS-1$
		Localization.getString("DataSetFactory.DataSetExample") + //$NON-NLS-1$
		Localization.getString("DataSetFactory.DataSetHeader") + //$NON-NLS-1$
		"1305	2309	4233\n" + //$NON-NLS-1$
		"2322	3234	4223\n" + //$NON-NLS-1$
		"2321	3123	4533\n" + //$NON-NLS-1$
		"2343	2931	4423\n" + //$NON-NLS-1$
		"1356	2984	3850\n", //$NON-NLS-1$

		Localization.getString("DataSetFactory.TableDataSetDescription") + //$NON-NLS-1$
		Localization.getString("DataSetFactory.DataSetExample") + //$NON-NLS-1$
		Localization.getString("DataSetFactory.DataSetHeader") + //$NON-NLS-1$
		"2322	3232	3453\n" + //$NON-NLS-1$
		"2321	3123	4533\n" + //$NON-NLS-1$
		"2145	2135	5921\n" + //$NON-NLS-1$
		"-------------------\n" + //$NON-NLS-1$
		Localization.getString("DataSetFactory.DataSetHeader") + //$NON-NLS-1$
		"2343	2931	4423\n" + //$NON-NLS-1$
		"2234	2723	5233\n" + //$NON-NLS-1$
		"3215	3565	4922\n" + //$NON-NLS-1$
		"-------------------\n" //$NON-NLS-1$
	};

	/**
	 * TODO remove in 3.0
	 */
	@Deprecated
	private static final ParsingWizardPage[] dataSetWizards = new ParsingWizardPage[] {
		new SelectRowParsingWizardPage(),
		new SelectTableParsingWizardPage()
	};
}
