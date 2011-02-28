package org.eclipse.linuxtools.rpm.ui.editor.tests.actions;

import junit.framework.TestCase;

import org.eclipse.linuxtools.rpm.ui.editor.actions.SpecfileEditorShowOutlineActionDelegate;
//TODO make it a real test
public class SpecfileEditorShowOutlineActionDelegateTest extends TestCase {

	private SpecfileEditorShowOutlineActionDelegate delegate;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		delegate = new SpecfileEditorShowOutlineActionDelegate();
	}

	public void testSetActiveEditor() {
		delegate.setActiveEditor(null, null);
	}

	public void testRun() {
		delegate.run(null);
	}

	public void testSelectionChanged() {
		delegate.selectionChanged(null, null);
	}

	public void testDispose() {
		delegate.dispose();
	}

	public void testInit() {
		delegate.init(null);
	}

}
