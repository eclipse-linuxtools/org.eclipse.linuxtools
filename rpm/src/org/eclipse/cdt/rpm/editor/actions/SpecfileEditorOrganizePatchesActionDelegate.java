package org.eclipse.cdt.rpm.editor.actions;

import org.eclipse.cdt.rpm.editor.SpecfileEditor;
import org.eclipse.cdt.rpm.editor.parser.Specfile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class SpecfileEditorOrganizePatchesActionDelegate implements
		IEditorActionDelegate {
	
	SpecfileEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof SpecfileEditor) {
			editor = (SpecfileEditor) targetEditor;
		}
	}

	public void run(IAction action) {
		Specfile specfile = editor.getSpecfile();
		if (specfile != null) {
			specfile.organizePatches();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

}
