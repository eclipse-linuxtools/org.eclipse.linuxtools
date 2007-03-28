package org.eclipse.linuxtools.rpm.ui.editor.outline;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackageContainer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpecfileContentProvider implements ITreeContentProvider {

	private IDocumentProvider documentProvider;
	private Specfile specfile;
	private SpecfileEditor specEditor;
	protected final static String SECTION_POSITIONS = "section_positions";
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(SECTION_POSITIONS);
	
	public SpecfileContentProvider(ITextEditor editor) {
		if (editor instanceof SpecfileEditor) {
			specEditor = (SpecfileEditor) editor;
			specfile = specEditor.getSpecfile();
		}
		this.documentProvider = editor.getDocumentProvider();
	}
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != null)
		{
			IDocument document = documentProvider.getDocument(oldInput);
			if (document != null)
			{
				try
				{
					document.removePositionCategory(SECTION_POSITIONS);
				}
				catch (BadPositionCategoryException x)
				{
				}
				document.removePositionUpdater(positionUpdater);
			}
		}

		if (newInput != null)
		{
			IDocument document = documentProvider.getDocument(newInput);
			if (document != null)
			{
				document.addPositionCategory(SECTION_POSITIONS);
				document.addPositionUpdater(positionUpdater);
				if (specEditor != null)
					specfile = specEditor.getSpecfile();
			}
		}
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement == specfile) {
			Object[] elms = new Object[1 + 1 + specfile.getSections().length];
			elms[0] = specfile.getPreamble();
			elms[1] = specfile.getPackages();
			Object[] sections = specfile.getSections();
			for (int i = 0; i < sections.length; i++) {
				 elms[i + 2] = sections[i];
			}
			return elms;
		} else if (parentElement instanceof SpecfilePackageContainer) {
			Object [] ret = ((SpecfilePackageContainer) parentElement).getPackages();
			return ret;
		} else if (parentElement instanceof SpecfilePackage) {
			Object [] ret = ((SpecfilePackage) parentElement).getSections();
			return ret;
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element == specfile) {
			return true;
		} else if (element instanceof SpecfilePackageContainer) {
			return ((SpecfilePackageContainer) element).hasChildren();
		} else if (element instanceof SpecfilePackage) {
			return ((SpecfilePackage) element).hasChildren();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return this.getChildren(specfile);
	}

}
