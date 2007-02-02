/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;


/**
 * @author keiths
 */
public class ProjectProfileShLib extends ProfileElement
{
	protected String _name;
	
	/**
	 * Constructor for ProjectProfileShLib.
	 * @param parent
	 */
	public ProjectProfileShLib(IProfileElement parent, String name)
	{
		super(parent, IProfileElement.OBJECT);
		_name = name;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// How to get object files from this?
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
		return CPluginImages.get(CPluginImages.IMG_OBJS_SHLIB);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return _name;
	}

	public int getSampleCount() { return 0; }
}
