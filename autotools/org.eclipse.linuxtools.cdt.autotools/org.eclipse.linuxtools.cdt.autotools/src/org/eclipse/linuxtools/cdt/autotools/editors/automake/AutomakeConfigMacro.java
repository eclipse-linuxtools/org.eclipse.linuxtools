/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.cdt.autotools.editors.automake;

import org.eclipse.linuxtools.cdt.autotools.internal.editors.automake.Directive;


public class AutomakeConfigMacro extends Directive {
	String name;
	
	public AutomakeConfigMacro(Directive parent, String name) {
		super(parent);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name + "\n";
	}
}
