/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPDocumentProvider;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers.ToggleCommentHandler;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@link ToggleCommentHandler}.
 */
public class STPToggleCommentTest {
    private static final String[] PARTITIONED_SCRIPT = new String[]{
        "//comment\n",                            // line 0
        "probe syscall.write{\n",                // line 1
        "// Some comment inside a probe\n",        // line 2
        "   printf(\"write syscall\")\n",        // line 3
        "not//a//commented//block\n",            // line 4
        "}\n",                                    // line 5
        "\n",                                     // line 6
        ""                                        // line 7
    };
    private static String stpScript;
    private static ToggleCommentHandler cmdHandler;
    private static IDocument document;

    @BeforeClass
    public static void setUp() {
        StringBuffer stpScriptBuff = new StringBuffer();
        for (String scriptLine : PARTITIONED_SCRIPT) {
            stpScriptBuff.append(scriptLine);
        }
        stpScript = stpScriptBuff.toString();
        cmdHandler = new ToggleCommentHandler();
        document =  new Document(stpScript);
    }

    private static class MockSTPDocumentProvider extends STPDocumentProvider{
        private IDocument document;

        MockSTPDocumentProvider(IDocument document){
            this.document = document;
            this.setupDocument(document);
        }

        private IDocument createDocument(Object element) {
            return document;
        }
    }

    @Test
    public void getFirstCompleteLineOfRegionTest() {
        IRegion scriptRegion;
        int i = 0;
        int curPos = 0;
        for (String scriptLine : PARTITIONED_SCRIPT) {
            int lineLength = scriptLine.length();
            int offset = stpScript.indexOf(scriptLine, curPos);
            scriptRegion = new Region(offset, lineLength);
            int actualIndex = cmdHandler.getFirstCompleteLineOfRegion(scriptRegion, document);
            assertEquals(i, actualIndex);

            curPos = offset + lineLength;
            i++;
        }
    }

    @Test
    public void isBlockEmptyTest() {
        int i = 0;
        for(String scriptLine : PARTITIONED_SCRIPT){
            // new lines are considered empty blocks
            boolean expected = "\n".equals(scriptLine) ? true : scriptLine.isEmpty();
            assertEquals(expected, cmdHandler.isBlockEmpty(i, i, document));

            i++;
        }
    }

    @Test
    public void isBlockCommentedTest() {
        MockSTPDocumentProvider provider = new MockSTPDocumentProvider(new Document(stpScript));
        IDocument document = provider.createDocument(null);

        int i = 0;
        for( String scriptLine : PARTITIONED_SCRIPT){
            // for the purposes of this test, commented blocks start with "//"
            assertEquals(scriptLine.startsWith("//"), cmdHandler.isBlockCommented(i, i, "//", document));

            i++;
        }
    }

    @Test
    public void getTextBlockFromSelectionTest() throws BadLocationException {
        int i = 0;
        int curPos = 0;
        for (String scriptLine : PARTITIONED_SCRIPT) {
            int lineLength = scriptLine.length();
            int offset = stpScript.indexOf(scriptLine, curPos);

            ITextSelection selection = new MockTextSelection(offset, lineLength, i, i, scriptLine);
            IRegion actualRegion = cmdHandler.getTextBlockFromSelection( selection, document);
            IRegion expectedRegion = new Region(offset, lineLength);
            assertEquals(scriptLine + " :", expectedRegion, actualRegion);

            curPos = offset + lineLength;
            i++;
        }
    }

    @Test
    public void isSelectionCommentedTest() {
        int i = 0;
        int curPos = 0;
        for (String scriptLine : PARTITIONED_SCRIPT) {
            int lineLength = scriptLine.length();
            int offset = stpScript.indexOf(scriptLine, curPos);
            ITextSelection selection = new MockTextSelection(offset, lineLength, i, i, scriptLine);
            MockSTPDocumentProvider provider = new MockSTPDocumentProvider(new Document(stpScript));
            IDocument document = provider.createDocument(null);

            // for the purposes of this test, commented blocks start with "//"
            assertEquals(scriptLine.startsWith("//"), cmdHandler.isSelectionCommented(selection, document));

            curPos = offset + lineLength;
            i++;
        }
    }

    /**
     * Mock {@link ITextSelection} implementation.
     */
    private static class MockTextSelection implements ITextSelection {
        private int offset;
        private int length;
        private int startLine;
        private int endLine;
        private String text;

        public MockTextSelection(int offset, int length, int startLine,
                int endLine, String text) {
            this.offset = offset;
            this.length = length;
            this.startLine = startLine;
            this.endLine = endLine;
            this.text = text;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getStartLine() {
            return startLine;
        }

        @Override
        public int getEndLine() {
            return endLine;
        }

        @Override
        public String getText() {
            return text;
        }
    }
}
