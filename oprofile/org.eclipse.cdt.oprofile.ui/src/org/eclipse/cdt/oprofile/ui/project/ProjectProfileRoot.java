/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;


/**
 * @author keiths
 */
public class ProjectProfileRoot extends ProfileElement
{
	protected ICModel _cmodel;
	
	/**
	 * Constructor for ProjectProfileRoot.
	 */
	public ProjectProfileRoot(ICModel cmodel)
	{
		super(null, IProfileElement.ROOT);
		_cmodel = cmodel;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// Children of ProjectProfileRoot are ICProjects in the C CoreModel
		ArrayList kids = new ArrayList();
		
		ICProject[] projects = new ICProject[0];
		try {
			projects = _cmodel.getCProjects();
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < projects.length; i++)
			kids.add(new ProjectProfileProject(this, projects[i]));
		
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
		return _cmodel.getElementName();
	}
	
	public Image getLabelImage()
	{
		return null;
	}
	
	public int getSampleCount() { return 0; }
}
