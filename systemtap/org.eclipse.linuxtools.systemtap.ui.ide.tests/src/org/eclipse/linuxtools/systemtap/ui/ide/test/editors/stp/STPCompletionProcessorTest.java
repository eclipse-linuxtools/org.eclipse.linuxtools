package org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPCompletionProcessor;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.junit.Test;

public class STPCompletionProcessorTest {
	
	private static String TEST_STP_SCRIPT = ""+
			"\n"+
			"\n//marker1"+
			"probe syscall.write{\n"+
			"  // Some comment inside a probe\n"+
			"   printf(\"%s fd %d\taddr%d\tcount%dargstr%s\n\", name, fd, buf_uaddr, count, argstr)\n"+
			"}\n"+
			"\n";

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
		Document testDocument = new Document(TEST_STP_SCRIPT);
		int offset = TEST_STP_SCRIPT.indexOf("//marker1");

		STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
		ICompletionProposal[] proposals = completionProcessor
				.computeCompletionProposals(testDocument,
						offset);
		
		assertTrue(proposalsContain(proposals, "probe "));
		assertTrue(proposalsContain(proposals, "global "));
		assertTrue(proposalsContain(proposals, "function "));
	}

	@Test
	public void testGlobalPartialCompletion() throws BadLocationException {
		Document testDocument = new Document(TEST_STP_SCRIPT);
		int offset = TEST_STP_SCRIPT.indexOf("//marker1");
		String prefix = "prob";
		testDocument.replace(offset, 0, prefix);
		offset += prefix.length();

		STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
		ICompletionProposal[] proposals = completionProcessor
				.computeCompletionProposals(testDocument,
						offset);
		
		assertTrue(proposalsContain(proposals, "probe "));
		assertTrue(!proposalsContain(proposals, "global "));
		assertTrue(!proposalsContain(proposals, "function "));
	}
	
	@Test
	public void testProbeCompletion() throws BadLocationException {
		assumeTrue(stapInstalled());

		Document testDocument = new Document(TEST_STP_SCRIPT);
		int offset = TEST_STP_SCRIPT.indexOf("//marker1");
		String prefix = "probe ";
		testDocument.replace(offset, 0, prefix);
		offset += prefix.length();

		STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
		ICompletionProposal[] proposals = completionProcessor
				.computeCompletionProposals(testDocument,
						offset);

		assertTrue(proposalsContain(proposals, "syscall"));
		assertTrue(!proposalsContain(proposals, "syscall.write"));
	}

	private boolean stapInstalled(){
		try {
			Process process = RuntimeProcessFactory.getFactory().exec(new String[]{"stap", "-V"}, null);
			return (process != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Test
	public void testGlobalInvalidCompletion() throws BadLocationException {
		Document testDocument = new Document(TEST_STP_SCRIPT);
		int offset = TEST_STP_SCRIPT.indexOf("//marker1");
		String prefix = "probe fake.fake";
		testDocument.replace(offset, 0, prefix);
		offset += prefix.length();

		STPCompletionProcessor completionProcessor = new STPCompletionProcessor();
		ICompletionProposal[] proposals = completionProcessor
				.computeCompletionProposals(testDocument,
						offset);
		
		assertTrue(proposalsContain(proposals, "No completion data found."));
	}

	private boolean proposalsContain(ICompletionProposal[] proposals, String proposal){
		for (ICompletionProposal p : proposals) {
			if (p.getDisplayString().equals(proposal))
				return true;
		}
		return false;
	}

}
