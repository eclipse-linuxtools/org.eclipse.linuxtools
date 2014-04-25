/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.IndentUtil;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPDocumentProvider;
import org.junit.Test;

/**
 * Tests for the CIndenter.
 *
 * @since 4.0
 */
public class STPIndenterTest {

    private static class MockSTPDocumentProvider extends STPDocumentProvider{
        MockSTPDocumentProvider(IDocument document){
            this.setupDocument(document);
        }
    }

    protected void assertIndenterResult(String before, String expected) throws Exception {
        IDocument document= new Document(before);
        new MockSTPDocumentProvider(document);
        int numLines = document.getNumberOfLines();
        if (document.getLineLength(numLines - 1) == 0) {
            numLines--;  // Exclude an empty line at the end.
        }
        IndentUtil.indentLines(document, new LineRange(0, numLines), null, null);
        assertEquals(expected, document.get());
    }

    @Test
    public void testIfStatement() throws Exception {
        assertIndenterResult(
                "if (a == b) {\n" +
                " k = 7",

                "if (a == b) {\n" +
                "\tk = 7");
    }

    @Test
    public void testIfElseStatement() throws Exception {
        assertIndenterResult(
                "if (a == b)\n" +
                " k = 7\n" +
                "   else\n" +
                "k = 9",

                "if (a == b)\n" +
                "\tk = 7\n" +
                "else\n" +
                "\tk = 9");
    }

    @Test
    public void testForStatement() throws Exception {
        assertIndenterResult(
                "for (i = 0; i < 3; ++i) {\n" +
                " k = 7",

                "for (i = 0; i < 3; ++i) {\n" +
                "\tk = 7");
    }

    @Test
    public void testWhileStatement() throws Exception {
        assertIndenterResult(
                "while (i < 3) {\n" +
                " k = 7",

                "while (i < 3) {\n" +
                "\tk = 7");
    }

    @Test
    public void testForeachStatement() throws Exception {
        assertIndenterResult(
                "foreach (i+ in arr) {\n" +
                " k = 7",

                "foreach (i+ in arr) {\n" +
                "\tk = 7");
    }

    @Test
    public void testStringLiteralAsLastArgument_1_Bug192412() throws Exception {
        assertIndenterResult(
                "foo(arg,\n" +
                "\"string\"",

                "foo(arg,\n" +
                "\t\"string\"");
    }

    @Test
    public void testIndentationAfterArrowOperator_Bug192412() throws Exception {
        assertIndenterResult(
                "if (1)\n" +
                "foo->bar();\n" +
                "dontIndent();",

                "if (1)\n" +
                "\tfoo->bar();\n" +
                "dontIndent();");
    }

    @Test
    public void testIndentationAfterShiftRight_Bug192412() throws Exception {
        assertIndenterResult(
                "if (1)\n" +
                "foo>>bar();\n" +
                "  dontIndent();",

                "if (1)\n" +
                "\tfoo>>bar();\n" +
                "dontIndent();");
    }

    @Test
    public void testIndentationAfterGreaterOrEquals_Bug192412() throws Exception {
        assertIndenterResult(
                "if (1)\n" +
                "foo >= bar();\n" +
                "  dontIndent();",

                "if (1)\n" +
                "\tfoo >= bar();\n" +
                "dontIndent();");
    }

    @Test
    public void testInitializerLists_Bug194585() throws Exception {
        assertIndenterResult(
                "int a[]=\n" +
                "{\n" +
                "1,\n" +
                "2\n" +
                "}",

                "int a[]=\n" +
                "{\n" +
                " 1,\n" +
                " 2\n" +
                "}");
    }

    @Test
    public void testWrappedAssignment_1_Bug277624() throws Exception {
        assertIndenterResult(
                "x =\n" +
                "0;",

                "x =\n" +
                "\t\t0;");
    }

    @Test
    public void testWrappedAssignment_2_Bug277624() throws Exception {
        assertIndenterResult(
                "{\n" +
                "a = 0;\n" +
                "x = 2 +\n" +
                "2 +\n" +
                "2;",

                "{\n" +
                "\ta = 0;\n" +
                "\tx = 2 +\n" +
                "\t\t\t2 +\n" +
                "\t\t\t2;");
    }

    @Test
    public void testWrappedAssignment_3_Bug277624() throws Exception {
        assertIndenterResult(
                "if (1 > 0) {\n" +
                "double d = a * b /\n" +
                "c",

                "if (1 > 0) {\n" +
                "\tdouble d = a * b /\n" +
                "\t\t\tc");
    }

    @Test
    public void testConditionalExpression_Bug283970() throws Exception {
        assertIndenterResult(
                "int x = 1 < 2 ?\n" +
                "f(0) :\n" +
                "1;\n" +
                "g();",

                "int x = 1 < 2 ?\n" +
                "\t\tf(0) :\n" +
                "\t\t1;\n" +
                "g();");
    }

    @Test
    public void testWrappedFor_1_Bug277625() throws Exception {
        assertIndenterResult(
                "for (int i = 0;\n" +
                "i < 2; i++)",

                "for (int i = 0;\n" +
                "\t\ti < 2; i++)");
    }

    @Test
    public void testWrappedFor_2_Bug277625() throws Exception {
        assertIndenterResult(
                "for (int i = 0; i < 2;\n" +
                "i++)",

                "for (int i = 0; i < 2;\n" +
                "\t\ti++)");
    }

    @Test
    public void testWrappedFor_3_Bug277625() throws Exception {
        assertIndenterResult(
                "for (int i = 0;\n" +
                "i < 2;\n" +
                "i++)\n" +
                "{",

                "for (int i = 0;\n" +
                "\t\ti < 2;\n" +
                "\t\ti++)\n" +
                "{");
    }

}
