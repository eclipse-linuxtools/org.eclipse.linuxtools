/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.system;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.oprofile.core.ISampleContainer;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.swt.graphics.Image;

/**
 * @author keiths
 */
public class SystemProfileObject extends SystemProfileRootElement
{
	SystemProfileObject(IProfileElement parent, ISampleContainer sfile)
	{
		// Containers are SampleFiles for all counters
		super(parent, IProfileElement.OBJECT, sfile);
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		// FIXME: Probably should use different icons for (system?) profile view
		return CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT);
	}
}
