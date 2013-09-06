/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.core.tests.datasets.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableParser;
import org.junit.Before;
import org.junit.Test;

public class TableParserTest {

	@Before
	public void setUp() {
		parser = new TableParser(new String[] {"\\d+", "(\\D+)", "\\d+", "\\D+"}, "\n\n");
	}

	@Test
	public void testParse() {
		assertNull(parser.parse(null));
		assertNull(parser.parse(new StringBuilder("")));
		assertNull(parser.parse(new StringBuilder("asdf")));
		assertNull(parser.parse(new StringBuilder("1, ")));
		assertNull(parser.parse(new StringBuilder("1, 3")));

		IDataEntry entry = parser.parse(new StringBuilder("1, (2), 3, 4, 5\n\n"));
		assertNotNull(entry);
		assertEquals(2, entry.getColCount());
		assertEquals(2, entry.getRowCount());
		assertEquals("1", entry.getRow(0)[0]);
	}

	private TableParser parser;
}
