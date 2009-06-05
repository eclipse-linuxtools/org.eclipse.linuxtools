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
import org.eclipse.linuxtools.cdt.autotools.internal.editors.automake.Parent;


public class AutomakeIfElse extends Parent {
	String condition;
	int startLine;
	int endLine;
	String type;
	public AutomakeIfElse(Directive parent, String type, String condition) {
		super(parent);
		this.type = type;
		this.condition = condition;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public void setLines(int startLine, int endLine) {
		this.startLine = startLine;
		this.endLine = endLine;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
