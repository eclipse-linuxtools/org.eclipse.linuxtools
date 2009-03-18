package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.io.File;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckViewPart;
import org.eclipse.linuxtools.valgrind.memcheck.model.StackFrameTreeElement;
import org.eclipse.linuxtools.valgrind.memcheck.model.ValgrindTreeElement;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class LinkedResourceDoubleClickTest extends AbstractLinkedResourceMemcheckTest {	
	private StackFrameTreeElement frame;

	public void testLinkedDoubleClickFile() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testLinkedDoubleClickFile"); //$NON-NLS-1$

		doDoubleClick();
		
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			File expectedFile = new File(frame.getFrame().getDir(), frame.getFrame().getFile());
			File actualFile = fileInput.getFile().getLocation().toFile();
			
			assertTrue(fileInput.getFile().isLinked(IResource.CHECK_ANCESTORS));
			assertEquals(expectedFile.getCanonicalPath(), actualFile.getCanonicalPath());
		}
		else {
			fail();
		}
	}
	
	public void testLinkedDoubleClickLine() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testLinkedDoubleClickLine"); //$NON-NLS-1$

		doDoubleClick();
		
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			
			ISelection selection = textEditor.getSelectionProvider().getSelection();
			if (selection instanceof TextSelection) {
				TextSelection textSelection = (TextSelection) selection;
				int line = textSelection.getStartLine() + 1; // zero-indexed
				
				assertEquals(frame.getFrame().getLine(), line);
			}
			else {
				fail();
			}
		}
		else {
			fail();
		}
	}
	
	private void doDoubleClick() throws Exception {
		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		TreeViewer viewer = view.getViewer();

		// get first leaf
		ValgrindTreeElement element = (ValgrindTreeElement) viewer.getInput();
		TreePath path = new TreePath(new Object[] { element });
		frame = null;
		while (element.getChildren().length > 0) {
			element = element.getChildren()[0];
			path = path.createChildPath(element);
			if (element instanceof StackFrameTreeElement) {
				frame = (StackFrameTreeElement) element;
			}
		}
		assertNotNull(frame);

		viewer.expandToLevel(frame, TreeViewer.ALL_LEVELS);
		TreeSelection selection = new TreeSelection(path);

		// do double click
		IDoubleClickListener listener = view.getDoubleClickListener();
		listener.doubleClick(new DoubleClickEvent(viewer, selection));
	}
}
