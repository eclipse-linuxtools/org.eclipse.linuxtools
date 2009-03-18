package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import java.io.File;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class DoubleClickTest extends AbstractCachegrindTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$	
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}

	private void doDoubleClick(TreePath path) {
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		TreeViewer treeViewer = (TreeViewer) view.getViewer();

		ICachegrindElement element = (ICachegrindElement) path.getLastSegment();
		treeViewer.expandToLevel(element, TreeViewer.ALL_LEVELS);
		TreeSelection selection = new TreeSelection(path);

		IDoubleClickListener listener = view.getDoubleClickListener();
		listener.doubleClick(new DoubleClickEvent(treeViewer, selection));
	}

	public void testDoubleClickFile() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testDoubleClickFile"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		TreePath path = new TreePath(new Object[] { output, file });

		doDoubleClick(path);

		checkFile(file);
	}
	
	public void testDoubleClickFunction() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testDoubleClickFunction"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		CachegrindFunction func = getFunctionByName(file, "main"); //$NON-NLS-1$
		TreePath path = new TreePath(new Object[] { output, file, func });

		doDoubleClick(path);

		// check file in editor
		IEditorPart editor = checkFile(file);
		
		// check line number
		ITextEditor textEditor = (ITextEditor) editor;
			
		ISelection selection = textEditor.getSelectionProvider().getSelection();
		TextSelection textSelection = (TextSelection) selection;
		int line = textSelection.getStartLine() + 1; // zero-indexed
				
		int expectedLine = ((IFunction) func.getModel()).getSourceRange().getStartLine();
		assertEquals(expectedLine, line);
	}
	
	public void testDoubleClickLine() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testDoubleClickFunction"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		CachegrindFunction func = getFunctionByName(file, "main"); //$NON-NLS-1$
		CachegrindLine line = func.getLines()[0];
		TreePath path = new TreePath(new Object[] { output, file, func });

		doDoubleClick(path);

		// check file in editor
		IEditorPart editor = checkFile(file);
		
		// check line number
		ITextEditor textEditor = (ITextEditor) editor;
			
		ISelection selection = textEditor.getSelectionProvider().getSelection();
		TextSelection textSelection = (TextSelection) selection;
		int actualLine = textSelection.getStartLine() + 1; // zero-indexed
				
		assertEquals(line.getLine(), actualLine);
	}

	private IEditorPart checkFile(CachegrindFile file) {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		IFileEditorInput fileInput = (IFileEditorInput) input;
		IResource expectedResource = proj.getProject().findMember(file.getName());
		File expectedFile = expectedResource.getLocation().toFile();
		File actualFile = fileInput.getFile().getLocation().toFile();
		assertEquals(expectedFile, actualFile);
		return editor;
	}
}
