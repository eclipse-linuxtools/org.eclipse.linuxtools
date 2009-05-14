/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.forms;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RpmTagText extends Text {

	private String tag;
	private Specfile specfile;
	
	public RpmTagText(Composite parent, int style, String rpmTag, Specfile specfile) {
		super(parent, style);
		this.tag = rpmTag;
		this.specfile = specfile;
		setText(specfile.getDefine(rpmTag).getStringValue());
	}

}
