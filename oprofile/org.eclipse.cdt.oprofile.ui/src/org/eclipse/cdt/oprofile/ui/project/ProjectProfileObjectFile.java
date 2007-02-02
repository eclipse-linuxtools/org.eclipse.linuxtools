/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;


/**
 * @author keiths
 */
public class ProjectProfileObjectFile extends ProfileElement
{
	// FIXME: gone! protected ICFile _cfile;
	
	/**
	 * Constructor for ProjectProfileObjectFile.
	 * @param parent
	 */
	public ProjectProfileObjectFile(IProfileElement parent/* FIXME: gone! ICFile cfile*/)
	{
		super(parent, IProfileElement.OBJECT);
		// FIXME: gone! _cfile = cfile;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// Children of object files are functions
		ArrayList kids = new ArrayList();
		/* FIXME: gone!
		ICElement[] elements = _cfile.getChildren();
		for (int i = 0; i < elements.length; i++)
		{
			if (elements[i].getElementType() == ICElement.C_FUNCTION)
				kids.add(new ProjectProfileFunction(this, (IFunction) elements[i]));
		}
		*/
		
		IProfileElement[] children = new IProfileElement[kids.size()];
		kids.toArray(children);
		return children;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return (getChildren().length > 0);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		return CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return null; // FIXME: gone!_cfile.getElementName();
	}

	public int getSampleCount() { return 0; }
}
