/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.text.MessageFormat;

public class SpecfileSection extends SpecfileElement {

	private SpecfilePackage parentPackage;

	
	public SpecfileSection(String name, Specfile specfile) {
		super(name);
		parentPackage = null;
		super.setSpecfile(specfile);
	}

	public SpecfilePackage getPackage() {
		return parentPackage;
	}

	public void setPackage(SpecfilePackage thePackage) {
		this.parentPackage = thePackage;
	}

	@Override
	public String toString() {
		if (parentPackage == null) {
			return getName();
		} else {
			return MessageFormat.format("{0} {1}", getName(), parentPackage); //$NON-NLS-1$
		}
	}
        
        public String getPackageName(){
            return parentPackage.getPackageName();
        }
	
}
