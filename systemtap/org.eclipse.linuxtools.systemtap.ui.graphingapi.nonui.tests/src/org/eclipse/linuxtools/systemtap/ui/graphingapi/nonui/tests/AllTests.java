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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.adapters.BlockAdapterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.adapters.ScrollAdapterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.AverageAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.CountAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.MaxAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.MinAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.SumAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.FilteredRowDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.RowDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.RowEntryTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.RowParserTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.FilteredTableDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.TableDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.TableEntryTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.TableParserTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.MatchFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.RangeFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.SortFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.UniqueFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.ChartStreamDaemonTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.DataPointTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.GraphDataTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.NumberTypeTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.UpdateManagerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests");

		//Adapters
		suite.addTestSuite(BlockAdapterTest.class);
		suite.addTestSuite(ScrollAdapterTest.class);
		
		//Aggregates
		suite.addTestSuite(AverageAggregateTest.class);
		suite.addTestSuite(CountAggregateTest.class);
		suite.addTestSuite(MaxAggregateTest.class);
		suite.addTestSuite(MinAggregateTest.class);
		suite.addTestSuite(SumAggregateTest.class);
		
		//DataSets
		
		//DataSets.Row
		suite.addTestSuite(FilteredRowDataSetTest.class);
		suite.addTestSuite(RowDataSetTest.class);
		suite.addTestSuite(RowEntryTest.class);
		suite.addTestSuite(RowParserTest.class);
		
		//DataSets.Table
		suite.addTestSuite(FilteredTableDataSetTest.class);
		suite.addTestSuite(TableDataSetTest.class);
		suite.addTestSuite(TableEntryTest.class);
		suite.addTestSuite(TableParserTest.class);

		//Filters
		suite.addTestSuite(MatchFilterTest.class);
		suite.addTestSuite(RangeFilterTest.class);
		suite.addTestSuite(SortFilterTest.class);
		suite.addTestSuite(UniqueFilterTest.class);

		//Structures
		suite.addTestSuite(ChartStreamDaemonTest.class);
	//	suite.addTestSuite(ChartStreamDaemonTest2.class);
		suite.addTestSuite(DataPointTest.class);
		suite.addTestSuite(GraphDataTest.class);
		suite.addTestSuite(NumberTypeTest.class);
		suite.addTestSuite(UpdateManagerTest.class);

		return suite;
	}
}
