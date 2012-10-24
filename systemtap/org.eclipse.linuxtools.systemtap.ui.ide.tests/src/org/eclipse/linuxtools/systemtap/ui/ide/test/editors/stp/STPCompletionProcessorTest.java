package org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPCompletionProcessor;
import org.junit.Test;

public class STPCompletionProcessorTest {
	
	private static String TEST_STP_SCRIPT = ""+
			"\n"+
			"\n"+
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
	
}
