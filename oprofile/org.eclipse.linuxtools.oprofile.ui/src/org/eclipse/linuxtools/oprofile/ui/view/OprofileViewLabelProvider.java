package org.eclipse.linuxtools.oprofile.ui.view;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.swt.graphics.Image;

public class OprofileViewLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewLabelProvider");
		return ((IUiModelElement) element).getLabelImage();
	}

	@Override
	public String getText(Object element) {
		Assert.isLegal(element instanceof IUiModelElement, "in OprofileViewLabelProvider");
		return ((IUiModelElement) element).getLabelText();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

}
