/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.swt.graphics.Image;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;


/**
 * @author keiths
 */
public class ProjectProfileProject extends ProfileElement
{
	protected ICProject _cproject;
	
	public ProjectProfileProject(IProfileElement parent, ICProject project)
	{
		super(parent, IProfileElement.ROOT);
		_cproject = project;
	}
	
	public IProfileElement[] getChildren()
	{
		// Children of ICProject are executables		
		ArrayList kids = new ArrayList();
		IBinary[] binaries = new IBinary[0];
		try {
			binaries = _cproject.getBinaryContainer().getBinaries();
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < binaries.length; i++)
		{
			if (binaries[i].isExecutable())
				kids.add(new ProjectProfileExecutable(this, binaries[i]));
		}
		
		IProfileElement[] children = new IProfileElement[kids.size()];
		kids.toArray(children);
		return children;
	}
	
	public boolean hasChildren()
	{
		return (getChildren().length > 0);
	}
	
	public String getLabelText()
	{
		return _cproject.getElementName();
	}
	
	public Image getLabelImage()
	{
		return CPluginImages.get(CPluginImages.IMG_OBJS_PROJECT);
	}
	
	public int getSampleCount() { return 0; }
}
