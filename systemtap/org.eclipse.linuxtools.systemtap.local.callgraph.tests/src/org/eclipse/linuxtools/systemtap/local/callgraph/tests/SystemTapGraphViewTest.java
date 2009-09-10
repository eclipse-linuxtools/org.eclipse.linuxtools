package org.eclipse.linuxtools.systemtap.local.callgraph.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.systemtap.local.callgraph.CallgraphView;

public class SystemTapGraphViewTest extends TestCase {
	private CallgraphView stapView = new CallgraphView();
	private String testText = "blah";
	
	public void test() {
		System.out.println("\n\nLaunching RunSystemTapActionTest\n");

		CallgraphView.forceDisplay();
		
		stapView.println(testText);
		assertEquals(stapView.getText(), testText);
		
		stapView.clearAll();
		assertEquals(stapView.getText(), "");
	}
	
}
