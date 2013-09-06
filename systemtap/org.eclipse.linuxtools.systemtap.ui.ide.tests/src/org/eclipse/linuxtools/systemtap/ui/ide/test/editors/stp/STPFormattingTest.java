/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPAutoEditStrategy;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPPartitionScanner;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPPartitioner;
import org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp.AbstractAutoEditTest.AutoEditTester;
import org.eclipse.linuxtools.systemtap.ui.tests.SystemtapTest;
import org.junit.Test;

public class STPFormattingTest extends SystemtapTest{

	/**
	 * A DocumentCommand with public constructor and exec method.
	 */
	protected static class TestDocumentCommand extends DocumentCommand {

		public TestDocumentCommand(int offset, int length, String text) {
			super();
			doit = true;
			this.text = text;

			this.offset = offset;
			this.length = length;

			owner = null;
			caretOffset = -1;
		}

		/**
		 * @param doc
		 * @return the new caret position.
		 * @throws BadLocationException
		 */
		public int exec(IDocument doc) throws BadLocationException {
			doc.replace(offset, length, text);
			return caretOffset != -1 ?
						caretOffset :
						offset + (text == null ? 0 : text.length());
		}
	}

	/**
	 * Sets up the document partitioner for the given document for the given partitioning.
	 *
	 * @param document
	 * @param partitioning
	 * @param owner may be null
	 */
	private void setupDocumentPartitioner(IDocument document, String partitioning) {
		IDocumentPartitioner partitioner = new STPPartitioner(new STPPartitionScanner(), STPPartitionScanner.STP_PARTITION_TYPES);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(partitioning, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}

	private AutoEditTester createAutoEditTester() {
		IDocument doc = new Document();
		setupDocumentPartitioner(doc, STPPartitionScanner.STP_PARTITIONING);
		AutoEditTester tester = new AutoEditTester(doc, STPPartitionScanner.STP_PARTITIONING);

		STPAutoEditStrategy s = new STPAutoEditStrategy(STPPartitionScanner.STP_PARTITIONING, null);
		tester.setAutoEditStrategy(IDocument.DEFAULT_CONTENT_TYPE, s);
		tester.setAutoEditStrategy(STPPartitionScanner.STP_COMMENT, s);
		tester.setAutoEditStrategy(STPPartitionScanner.STP_CONDITIONAL, s);
		tester.setAutoEditStrategy(STPPartitionScanner.STP_STRING, s);
		return tester;
	}

	@Test
	public void testEndProbeCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
	}

	@Test
	public void testSquareBracketCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());

		tester.type("a[");

		// Verify automatic completion of square brackets
		assertEquals(1, tester.getCaretLine());
		assertEquals(3, tester.getCaretColumn());
		assertEquals("\ta[]", tester.getLine());

		tester.type("2]");

		// Verify we can overwrite the end square-bracket
		assertEquals(1, tester.getCaretLine());
		assertEquals(5, tester.getCaretColumn());
		assertEquals("\ta[2]", tester.getLine());

		// Verify we don't add square brackets inside a string
		tester.type("=\"b[");
		assertEquals(1, tester.getCaretLine());
		assertEquals(9, tester.getCaretColumn());
		assertEquals("\ta[2]=\"b[\"", tester.getLine());

		// Verify we don't add square brackets inside a comment
		tester.goTo(tester.getCaretLine(), tester.getCaretColumn() + 1);
		tester.type(" /* a[2");
		assertEquals(1, tester.getCaretLine());
		assertEquals(17, tester.getCaretColumn());
		assertEquals("\ta[2]=\"b[\" /* a[2", tester.getLine());

		// Verify we don't add square brackets inside a line comment
		tester.type(" */ // a[2");
		assertEquals(1, tester.getCaretLine());
		assertEquals(27, tester.getCaretColumn());
		assertEquals("\ta[2]=\"b[\" /* a[2 */ // a[2", tester.getLine());

		// Verify we don't add square brackets inside a line comment
		tester.type("\n# a[2");

		assertEquals(2, tester.getCaretLine());
		assertEquals(5, tester.getCaretColumn());
		assertEquals("# a[2", tester.getLine());

		// Verify we don't add square brackets inside a char specifier
		tester.type("\na[3]='[");

		System.out.println(tester.fDoc.get());
		assertEquals(3, tester.getCaretLine());
		assertEquals(8, tester.getCaretColumn());
		assertEquals("\ta[3]='[", tester.getLine());

	}

	@Test
	public void testBracketCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());

		// Verify we don't complete brackets inside a comment
		tester.type("// if (a == b) {\n");

		assertEquals(2, tester.getCaretLine());
		assertEquals(1, tester.getCaretColumn());
		assertEquals("\t", tester.getLine());

		// verify we don't complete brackets inside a comment
		tester.type("# if (a == b) {\n");

		// Verify we can overwrite the end square-bracket
		assertEquals(3, tester.getCaretLine());
		assertEquals(1, tester.getCaretColumn());
		assertEquals("\t", tester.getLine());

	}

	@Test
	public void testQuoteCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());

		tester.type("a[2]=\"");

		// Verify automatic completion of quotes
		assertEquals(1, tester.getCaretLine());
		assertEquals(7, tester.getCaretColumn());
		assertEquals("\ta[2]=\"\"", tester.getLine());

		tester.type("\\\"\"");

		// Verify we can overwrite the auto end-quote without appending
		// and escaped quotes can be added inside strings
		assertEquals(1, tester.getCaretLine());
		assertEquals(10, tester.getCaretColumn());
		assertEquals("\ta[2]=\"\\\"\"", tester.getLine());

		// Verify we don't add quotes inside a comment
		tester.type(" /* \"");
		assertEquals(1, tester.getCaretLine());
		assertEquals(15, tester.getCaretColumn());
		assertEquals("\ta[2]=\"\\\"\" /* \"", tester.getLine());

		// Verify we don't add quotes inside a line comment
		tester.type(" */ // \"");
		assertEquals(1, tester.getCaretLine());
		assertEquals(23, tester.getCaretColumn());
		assertEquals("\ta[2]=\"\\\"\" /* \" */ // \"", tester.getLine());

		// Verify we don't add quotes inside a line comment
		tester.type("\n# \"");

		assertEquals(2, tester.getCaretLine());
		assertEquals(3, tester.getCaretColumn());
		assertEquals("# \"", tester.getLine());

		// Verify we don't add quotes inside a char specifier
		tester.type("\na[3]='\"");

		System.out.println(tester.fDoc.get());
		assertEquals(3, tester.getCaretLine());
		assertEquals(8, tester.getCaretColumn());
		assertEquals("\ta[3]='\"", tester.getLine());

	}

	@Test
	public void testIfCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("if (");

		assertEquals(1, tester.getCaretLine());
		assertEquals(5, tester.getCaretColumn());
		assertEquals("\tif ()", tester.getLine());

		// Verify we can overwrite the closing bracket for if
		tester.type("a == b)");

		assertEquals(1, tester.getCaretLine());
		assertEquals(12, tester.getCaretColumn());
		assertEquals("\tif (a == b)", tester.getLine());
	}

	@Test
	public void testElseBracketCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("if (a == 2) {\n");

		assertEquals(2, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\tif (a == 2) {", tester.getLine(-1));
		assertEquals("\t}", tester.getLine(1));

		tester.goTo(3, 2);

		tester.type(" else {\n");

		assertEquals(4, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t}", tester.getLine(1));
	}

	@Test
	public void testForCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("for (");

		assertEquals(1, tester.getCaretLine());
		assertEquals(6, tester.getCaretColumn());
		assertEquals("\tfor ()", tester.getLine());
	}

	@Test
	public void testForBracketCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("for (i = 0; i < 3; ++i) {\n");

		assertEquals(2, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t}", tester.getLine(1));
		assertEquals("}", tester.getLine(2));
	}

	@Test
	public void testWhileCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("while (");

		assertEquals(1, tester.getCaretLine());
		assertEquals(8, tester.getCaretColumn());
		assertEquals("\twhile ()", tester.getLine());
	}

	@Test
	public void testWhileBracketCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("while (i == 0) {\n");

		assertEquals(2, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t}", tester.getLine(1));
		assertEquals("}", tester.getLine(2));
	}

	@Test
	public void testForeachBracketCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
		AutoEditTester tester = createAutoEditTester();

		tester.type("probe end {\n");

		// We are located on 2nd line
		assertEquals(1, tester.getCaretLine());
		// Nested location is indented
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.  Note, getLine() gets
		// line from current position which is on line 1 of doc.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$

		tester.type("foreach (n in k+) {\n");

		assertEquals(2, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t}", tester.getLine(1));
		assertEquals("}", tester.getLine(2));
	}

	@Test
	public void testPasteAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester();
		tester.type("probe end {\n"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("if (a == b) {\n" +
				     "\tfor (x = 0; x < 3; ++x) {\n" +
				     "\t\tz = 4;\n" +
				     "\t}\n" +
				     "}\n"); //$NON-NLS-1$
		tester.goTo(1, 0);
		assertEquals("\tif (a == b) {", tester.getLine(0)); //$NON-NLS-1$
		assertEquals("\t\tfor (x = 0; x < 3; ++x) {", tester.getLine(1)); //$NON-NLS-1$
		assertEquals("\t\t\tz = 4;", tester.getLine(2)); //$NON-NLS-1$
		assertEquals("\t\t}", tester.getLine(3)); //$NON-NLS-1$
		assertEquals("\t}", tester.getLine(4)); //$NON-NLS-1$
	}

}
