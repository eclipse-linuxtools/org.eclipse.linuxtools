package org.eclipse.linuxtools.systemtap.graphingapi.core.tests.datasets.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableParser;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.junit.Before;
import org.junit.Test;

public class TableParserTest {

	@Before
	public void setUp() {
		parser = new TableParser(new String[] {"\\d+", "(\\D+)", "\\d+", "\\D+"}, "\n\n");
		
		IMemento m = XMLMemento.createWriteRoot("a");
		parser.saveXML(m);
		parser2 = new TableParser(m);
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

		entry = parser2.parse(new StringBuilder("1, 2, 3, 4, 5\n\n"));
		assertNotNull(entry);
		assertEquals(2, entry.getColCount());
		assertEquals(2, entry.getRowCount());
		assertEquals("1", entry.getRow(0)[0]);
	}
	@Test
	public void testSaveXML() {
		IMemento m = XMLMemento.createWriteRoot("a");
		parser.saveXML(m);
		assertSame(TableDataSet.ID, m.getString("dataset"));

		IMemento[] children = m.getChildren("Series");
		assertEquals(2, children.length);
		assertSame("\\d+", children[0].getString("parsingExpression"));
		assertSame("(\\D+)", children[0].getString("parsingSpacer"));
		assertSame("\\d+", children[1].getString("parsingExpression"));
		assertSame("\\D+", children[1].getString("parsingSpacer"));
		
		IMemento child = m.getChild("Delimiter");
		assertSame("\n\n", child.getString("parsingExpression"));
	}
	
	TableParser parser;
	TableParser parser2;
}
