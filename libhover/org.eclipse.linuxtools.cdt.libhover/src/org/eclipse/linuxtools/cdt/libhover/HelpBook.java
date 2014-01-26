/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover;

import org.eclipse.cdt.ui.ICHelpBook;

public class HelpBook implements ICHelpBook {

	private String title;
	private int type;
	
	public HelpBook (String title, String typeName) {
		this.title = title;
		if (typeName.equals("C")) //$NON-NLS-1$
		    type = HELP_TYPE_C;
		else if (typeName.equals("C++")) //$NON-NLS-1$
			type = HELP_TYPE_CPP;
		else
			type = HELP_TYPE_ASM;
	}
	@Override
	public String getTitle () {
		return title;
	}
	
	@Override
	public int getCHelpType () {
		return type;
	}

}
