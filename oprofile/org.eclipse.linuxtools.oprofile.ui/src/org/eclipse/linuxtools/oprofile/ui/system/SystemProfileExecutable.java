/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.system;

import org.eclipse.linuxtools.oprofile.core.model.IOpModelContainer;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.swt.graphics.Image;

public class SystemProfileExecutable extends SystemProfileRootElement
{
	SystemProfileExecutable(IProfileElement parent, IOpModelContainer sfile)
	{
		super(parent, IProfileElement.OBJECT, sfile);
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.EXEC_ICON).createImage();
	}
}
