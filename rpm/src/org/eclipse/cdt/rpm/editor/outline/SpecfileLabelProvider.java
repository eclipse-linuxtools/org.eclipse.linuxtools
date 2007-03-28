package org.eclipse.cdt.rpm.editor.outline;

import org.eclipse.cdt.rpm.editor.parser.Specfile;
import org.eclipse.cdt.rpm.editor.parser.SpecfileElement;
import org.eclipse.cdt.rpm.editor.parser.SpecfilePackage;
import org.eclipse.cdt.rpm.editor.parser.SpecfilePackageContainer;
import org.eclipse.cdt.rpm.editor.parser.SpecfilePreamble;
import org.eclipse.cdt.rpm.editor.parser.SpecfileSection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class SpecfileLabelProvider implements ILabelProvider {

	public SpecfileLabelProvider() {
		super();
	}
	
	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		if (element instanceof SpecfileSection) {
			SpecfileSection specfileSection = (SpecfileSection) element;
			return specfileSection.toString();
		} else if (element instanceof Specfile) {
			return ((Specfile) element).getName();
		} else if (element instanceof SpecfilePackageContainer) {
			return "Packages";
		} else if (element instanceof SpecfilePreamble){
			return "Preamble";
		} else if (element instanceof SpecfileElement) {
			SpecfileElement specfileElement = (SpecfileElement) element;
			return specfileElement.getName();
		} else if (element instanceof String) {
			return (String) element;
		} else if (element instanceof SpecfilePackage) {
			return ((SpecfilePackage) element).getName();
		}
		return "";
	}

}
