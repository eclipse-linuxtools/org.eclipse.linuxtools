package org.eclipse.linuxtools.rpmstubby.popup.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.rpmstubby.SpecfileWriter;

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
}
