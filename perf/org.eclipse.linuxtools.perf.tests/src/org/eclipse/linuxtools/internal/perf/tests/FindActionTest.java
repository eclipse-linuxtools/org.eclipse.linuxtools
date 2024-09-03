/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.linuxtools.internal.perf.ui.SourceDisassemblyView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

/**
 * Tests for perf source disassembly find text dialog.
 */
public class FindActionTest {
    private static final String SAMPLE_STRS[] = new String[] { "sample", "styled", "text", " ", "tyled", "ample" };

    @Test
    public void testFindAction() {
        for (String strFind : SAMPLE_STRS) {
            int[] offsets = new int[] { 0, strFind.length()/2, strFind.length()};
            for (int offset : offsets) {
                testForwardSearch(offset , strFind);
                testBackwardSearch(offset , strFind);
            }
        }
    }

    /**
     * Test forward search functionality.
     * @param offset offset at which searching starts
     * @param findStr the string to find
     */
    private void testForwardSearch(int offset, String findStr) {
        StubSourceDisassemblyView stubView = new StubSourceDisassemblyView();
        String searchStr = stubView.getContent();
        int[] indices = new int[]{
                /*
                 * SourceDisassemblyView#findAndSelect parameters:
                 *         - widgetOffset, findstring, searchforward, casesensitive, wholeword
                 */
                stubView.findAndSelect(offset, findStr, true, true, false),
                stubView.findAndSelect(offset, findStr, true, false, false)
        };

        int expected = -1;
        for(int actual : indices){
            expected =  searchStr.indexOf(findStr, offset);
            assertEquals(expected, actual, "Failed on the following case:"
                    + " offset= " + offset
                    + " substring= " + findStr);
        }
    }

    /**
     * Test backward search functionality.
     * @param offset offset at which searching starts
     * @param findStr the string to find
     */
    private void testBackwardSearch(int offset, String findStr) {

        StubSourceDisassemblyView stubView = new StubSourceDisassemblyView();
        String searchStr = stubView.getContent().substring(0, offset);
        int[] indices = new int[]{
            /*
             * SourceDisassemblyView#findAndSelect parameters:
             *         - widgetOffset, findString, searchForward, caseSensitive, wholeWord
             */
            stubView.findAndSelect(offset, findStr, false, true, false),
            stubView.findAndSelect(offset, findStr, false, false, false)
        };

        int expected = -1;
        for(int actual : indices){
            expected =  searchStr.lastIndexOf(findStr, offset - 1);
            assertEquals(expected, actual, "Failed on the following case:"
                    + " offset= " + offset
                    + " substring= " + findStr);
        }
    }

    /**
     * Stub source disassemlby view part.
     */
    private static class StubSourceDisassemblyView extends
            SourceDisassemblyView {
        public StubSourceDisassemblyView() {
            Shell parent = new Shell();
            StyledText txt = new StyledText(parent, SWT.DEFAULT);
            txt.setText("sample styled text");
            txt.setEditable(false);
            setStyledText(txt);
        }
    }
}
