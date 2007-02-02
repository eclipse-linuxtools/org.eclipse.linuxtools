/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;


/**
 * @author keiths
 */
public class ProjectProfileFunction extends ProfileElement
{
	protected IFunction _function;
	
	/**
	 * Constructor for ProjectProfileFunction.
	 * @param parent
	 * @param element
	 */
	public ProjectProfileFunction(IProfileElement parent, IFunction function)
	{
		super(parent, IProfileElement.SYMBOL);
		_function = function;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		return new IProfileElement[0];
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return false;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return _function.getElementName() + "()"; //$NON-NLS-1$
	}

	public int getSampleCount() { return 0; }
}
