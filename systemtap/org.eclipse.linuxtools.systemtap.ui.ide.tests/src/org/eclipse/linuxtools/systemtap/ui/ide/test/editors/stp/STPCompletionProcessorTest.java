/*******************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPCompletionProcessor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPDocumentProvider;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class STPCompletionProcessorTest {

    private static String TEST_STP_SCRIPT = ""+
            "\n"+
            "\n//marker1"+
            "probe syscall.write{\n"+
            "  // Some comment inside a probe\n"+
            "   printf(\"%s fd %d\taddr%d\tcount%dargstr%s\n\", name, fd, buf_uaddr, count, argstr)\n"+
            "}\n"+
            "\n";

    private static class MockSTPDocumentProvider extends STPDocumentProvider {
        private IDocument document;

        MockSTPDocumentProvider(IDocument document){
            this.document = document;
            this.setupDocument(document);
        }

        protected IDocument createDocument(Object element) {
            return document;
        }
    }

    private static class MockSTPEditor extends STPEditor {
        public MockSTPEditor(IDocument document) {
            super();
            setDocumentProvider(new MockSTPDocumentProvider(document));
        }
    }

    /**
     * Use pre-written contents to populate the Function & Probe views.
     */
    @BeforeClass
    public static void prepareTrees() {
        TapsetLibrary.stop();
        IPath path = new Path(System.getenv("HOME")). //$NON-NLS-1$
                append(".systemtapgui").append("TreeSettings").
                addFileExtension("xml"); //$NON-NLS-1$

        try (InputStream is = FileLocator.openStream(
                FrameworkUtil.getBundle(STPCompletionProcessorTest.class),
                new Path("helpers/TreeSettings.xml"), false);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {

            String line;
            while ((line = br.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            TapsetLibrary.readTreeFile();
        } catch (IOException e) {
            fail("Unable to read dummy function/probe tree file for testing");
        }
    }

    @Test
    public void testCompletionRequest() {
        Document testDocument = new Document("");
        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument, 0);
        assertNotNull(proposals);
    }

    @Test
    public void testCompletionRequestAtEOF() {
        Document testDocument = new Document(TEST_STP_SCRIPT);
        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument,
                        TEST_STP_SCRIPT.length());
        assertNotNull(proposals);
    }

    @Test
    public void testGlobalCompletion() {
        MockSTPDocumentProvider provider = new MockSTPDocumentProvider(new Document(TEST_STP_SCRIPT));
        IDocument testDocument = provider.createDocument(null);
        int offset = TEST_STP_SCRIPT.indexOf("//marker1");

        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        completionProcessor.waitForInitialization();
        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument,
                        offset);

        assertTrue(proposalsContain(proposals, "probe "));
        assertTrue(proposalsContain(proposals, "global "));
        assertTrue(proposalsContain(proposals, "function "));
    }

    @Test
    public void testGlobalPartialCompletion() throws BadLocationException {
        String prefix = "prob";
        ICompletionProposal[] proposals = getCompletionsForPrefix(prefix);
        assertTrue(proposalsContain(proposals, "probe "));
        assertTrue(!proposalsContain(proposals, "global "));
        assertTrue(!proposalsContain(proposals, "function "));
    }

    @Test
    public void testProbeCompletion() throws BadLocationException {
        String prefix = "probe ";
        ICompletionProposal[] proposals = getCompletionsForPrefix(prefix);
        assertTrue(proposalsContain(proposals, "syscall"));
        assertTrue(!proposalsContain(proposals, "syscall.write"));
    }

    @Test
    public void testMultiProbeCompletion() throws BadLocationException {
        String prefix = "probe begin,e";
        ICompletionProposal[] proposals = getCompletionsForPrefix(prefix);
        assertTrue(proposalsContain(proposals, "end"));
        assertTrue(proposalsContain(proposals, "error"));

        prefix = "probe myBegin = b";
        proposals = getCompletionsForPrefix(prefix);
        assertTrue(proposalsContain(proposals, "begin"));
    }

    @Test
    public void testGlobalInvalidCompletion() throws BadLocationException {
        ICompletionProposal[] proposals = getCompletionsForPrefix("probe fake.fake");
        assertEquals(proposals.length, 0);
    }

    @Test
    public void testStaticProbeCompletion() throws BadLocationException {
        ICompletionProposal[] proposals = getCompletionsForPrefix("probe kernel.");
        assertTrue(proposalsContain(proposals, "kernel.function(string)"));
        assertTrue(proposalsContain(proposals, "kernel.mark(string)"));
    }

    @Test
    public void testEndProbeCompletion() throws BadLocationException {
        Document testDocument = new Document(TEST_STP_SCRIPT);
        @SuppressWarnings("unused")
        MockSTPEditor editor = new MockSTPEditor(testDocument);

        int offset = TEST_STP_SCRIPT.indexOf("//marker1");
        String prefix = "probe end{}";
        testDocument.replace(offset, 0, prefix);
        offset += prefix.length() - 1;

        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        completionProcessor.waitForInitialization();
        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument,
                        offset);

        assertTrue(proposalsContain(proposals, "addr"));
        assertTrue(proposalsContain(proposals, "backtrace"));
        assertTrue(proposalsContain(proposals, "cmdline_args"));
    }

    @Test
    public void testProbeVariableCompletion() throws BadLocationException {
        Document testDocument = new Document(TEST_STP_SCRIPT);
        @SuppressWarnings("unused")
        MockSTPEditor editor = new MockSTPEditor(testDocument);

        int offset = TEST_STP_SCRIPT.indexOf("//marker1");
        String prefix = "probe syscall.write{}";
        testDocument.replace(offset, 0, prefix);
        offset += prefix.length() - 1;

        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument,
                        offset);

        assertTrue(proposalsContain(proposals, "fd:long"));
        assertTrue(proposalsContain(proposals, "name:string"));
        assertTrue(proposalsContain(proposals, "buf_uaddr:long"));
    }

    @Test
    public void testStaticProbeNormalizationCompletion() throws BadLocationException {
        ICompletionProposal[] proposals = getCompletionsForPrefix("probe kernel.function(\"PATTERNASDF\").");
        assertTrue(proposalsContain(proposals, "kernel.function(string).return"));

        proposals = getCompletionsForPrefix("probe probe process(\"PAT/H/\").");
        assertTrue(proposalsContain(proposals, "process(string).begin"));
        assertTrue(proposalsContain(proposals, "process(string).end"));

        proposals = getCompletionsForPrefix("probe  process(123).");
        assertTrue(proposalsContain(proposals, "process(number).begin"));
        assertTrue(proposalsContain(proposals, "process(number).end"));

        proposals = getCompletionsForPrefix("probe module(\"MPATTERasdfN\").");
        assertTrue(proposalsContain(proposals, "module(string).function(string)"));
        assertTrue(proposalsContain(proposals, "module(string).statement(string)"));
    }

    private ICompletionProposal[] getCompletionsForPrefix(String prefix) throws BadLocationException {
        MockSTPDocumentProvider provider = new MockSTPDocumentProvider(new Document(TEST_STP_SCRIPT));
        IDocument testDocument = provider.createDocument(null);
        int offset = TEST_STP_SCRIPT.indexOf("//marker1");
        testDocument.replace(offset, 0, prefix);
        offset += prefix.length();

        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        completionProcessor.waitForInitialization();

        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument,
                        offset);
        return proposals;
    }

    @Test
    public void testFunctionCompletion() throws BadLocationException {
        Document testDocument = new Document(TEST_STP_SCRIPT);
        @SuppressWarnings("unused")
        MockSTPEditor editor = new MockSTPEditor(testDocument);

        int offset = TEST_STP_SCRIPT.indexOf("//marker1");
        String prefix = "probe syscall.write{addr}";
        testDocument.replace(offset, 0, prefix);
        offset += prefix.length() - 1;

        STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
        completionProcessor.waitForInitialization();

        ICompletionProposal[] proposals = completionProcessor
                .computeCompletionProposals(testDocument,
                        offset);

        assertTrue(proposalsContain(proposals, "addr"));
        assertTrue(proposalsContain(proposals, "addr_from_rqst"));
        assertTrue(proposalsContain(proposals, "addr_from_rqst_str"));
    }

    private boolean proposalsContain(ICompletionProposal[] proposals, String proposal){
        for (ICompletionProposal p : proposals) {
            if (p.getDisplayString().contains(proposal)) {
                return true;
            }
        }
        return false;
    }
}
