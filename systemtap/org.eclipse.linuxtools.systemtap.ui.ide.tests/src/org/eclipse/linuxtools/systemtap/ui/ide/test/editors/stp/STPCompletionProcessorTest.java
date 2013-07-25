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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPCompletionProcessor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPDocumentProvider;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.systemtap.ui.tests.SystemtapTest;
import org.junit.Test;

public class STPCompletionProcessorTest extends SystemtapTest{

	private static String TEST_STP_SCRIPT = ""+
			"\n"+
			"\n//marker1"+
			"probe syscall.write{\n"+
			"  // Some comment inside a probe\n"+
			"   printf(\"%s fd %d\taddr%d\tcount%dargstr%s\n\", name, fd, buf_uaddr, count, argstr)\n"+
			"}\n"+
			"\n";

	private static class MockSTPDocumentProvider extends STPDocumentProvider{
		private IDocument document;

		MockSTPDocumentProvider(IDocument document){
			this.document = document;
			this.setupDocument(document);
		}

		@Override
		protected IDocument createDocument(Object element) {
			return document;
		}
	}

	private static class MockSTPEditor extends STPEditor{
		public MockSTPEditor(IDocument document) {
			super();
			setDocumentProvider(new MockSTPDocumentProvider(document));
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

		printProposals(proposals);
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
		assumeTrue(stapInstalled);
		String prefix = "probe ";
		ICompletionProposal[] proposals = getCompletionsForPrefix(prefix);
		assertTrue(proposalsContain(proposals, "syscall"));
		assertTrue(!proposalsContain(proposals, "syscall.write"));
	}

	@Test
	public void testMultiProbeCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);
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
		assertTrue(proposals.length == 0);
	}

	@Test
	public void testStaticProbeCompletion() throws BadLocationException{
		ICompletionProposal[] proposals = getCompletionsForPrefix("probe kernel.");
		assertTrue(proposalsContain(proposals, "kernel.function(\"PATTERN\")"));
		assertTrue(proposalsContain(proposals, "kernel.mark(\"MARK\")"));
	}

	@Test
	public void testEndProbeCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);

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
		assumeTrue(stapInstalled);

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
	public void testStaticProbeNormalizationCompletion() throws BadLocationException{
		ICompletionProposal[] proposals = getCompletionsForPrefix("probe kernel.function(\"PATTERNASDF\").");
		assertTrue(proposalsContain(proposals, "kernel.function(\"PATTERN\").return"));

        proposals = getCompletionsForPrefix("probe probe process(\"PAT/H/\").");
		assertTrue(proposalsContain(proposals, "process(\"PATH\").begin"));
		assertTrue(proposalsContain(proposals, "process(\"PATH\").end"));

        proposals = getCompletionsForPrefix("probe  process(123).");
		assertTrue(proposalsContain(proposals, "process(PID).begin"));
		assertTrue(proposalsContain(proposals, "process(PID).end"));

        proposals = getCompletionsForPrefix("probe module(\"MPATTERasdfN\").");
		assertTrue(proposalsContain(proposals, "module(\"MPATTERN\").function(\"PATTERN\")"));
		assertTrue(proposalsContain(proposals, "module(\"MPATTERN\").statement(\"PATTERN\")"));
	}

	private ICompletionProposal[] getCompletionsForPrefix(String prefix) throws BadLocationException{
		MockSTPDocumentProvider provider = new MockSTPDocumentProvider(new Document(TEST_STP_SCRIPT));
		IDocument testDocument = provider.createDocument(null);
		int offset = TEST_STP_SCRIPT.indexOf("//marker1");
		testDocument.replace(offset, 0, prefix);
		offset += prefix.length();

		STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
		completionProcessor.waitForInitialization();

		System.out.println(testDocument.get());
		
		ICompletionProposal[] proposals = completionProcessor
				.computeCompletionProposals(testDocument,
						offset);
		return proposals;
	}

	@Test
	public void testFunctionCompletion() throws BadLocationException {
		assumeTrue(stapInstalled);

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
	
	private void printProposals(ICompletionProposal[] proposals){
		for (ICompletionProposal p : proposals) {
			System.out.println(p.getDisplayString());
		}
	}
}
