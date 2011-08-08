package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.rpmstubby.SpecfileWriter;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Action handling stybifying RPM spec file from a Eclipse feature.xml file.
 * 
 */
public class StubifyFeatureAction extends StubifyAction {

	@Override
	public void run(IAction action) {
		IFile featureFile = null;
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		for (Iterator<?> selectionIter = structuredSelection.iterator(); selectionIter
				.hasNext();) {
			Object selected = selectionIter.next();
			if (selected instanceof IProject) {
				featureFile = ((IProject) selected).getFile(new Path(
						"/feature.xml"));
			} else if (selected instanceof IFile) {
				featureFile = (IFile) selected;
			} else {
				// FIXME: error
			}
		}
		SpecfileWriter specfileWriter = new SpecfileWriter();
		specfileWriter.write(featureFile);
	}

	public Object execute(ExecutionEvent event) {

		IFile featureFile = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) selection).toList()) {
				if (element instanceof IFile) {
					featureFile = (IFile) element;
				} else if (element instanceof IAdaptable) {
					featureFile = (IFile) ((IAdaptable) element)
							.getAdapter(IFile.class);
				}
				if (featureFile != null) {
					SpecfileWriter specfileWriter = new SpecfileWriter();
					specfileWriter.write(featureFile);
				}
			}
			// StructuredSelection structuredSelection = (StructuredSelection)
			// selection;
			// for (Iterator<?> selectionIter = structuredSelection.iterator();
			// selectionIter
			// .hasNext();) {
			// Object selected = selectionIter.next();
			// if (selected instanceof IProject) {
			// featureFile = ((IProject) selected).getFile(new Path(
			// "/feature.xml"));
			// } else if (selected instanceof IFile) {
			// featureFile = (IFile) selected;
			// } else {
			// // FIXME: error
			// }
			// }

		}
		return null;
	}
}
