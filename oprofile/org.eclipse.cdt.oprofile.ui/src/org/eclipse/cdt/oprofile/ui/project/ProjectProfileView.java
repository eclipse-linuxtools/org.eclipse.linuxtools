/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.oprofile.ui.BaseProfileView;
import org.eclipse.swt.widgets.Composite;


/**
 * @author keiths
 */
public class ProjectProfileView extends BaseProfileView
{
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		_viewer.setInput(new ProjectProfileRoot(CoreModel.getDefault().getCModel()));
	}
}
