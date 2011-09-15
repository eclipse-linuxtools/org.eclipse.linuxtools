/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - modified for Automake editor usage
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui.editors.automake;

import java.io.File;
import java.io.IOException;

public class Else extends Conditional implements IAutomakeConditional, ICommand {

	private boolean isAutomake;
	private Rule[] rules;
	
	public Else(Directive parent) {
		super(parent);
	}

	public boolean isAutomake() {
		return isAutomake;
	}
	
	public Rule[] getRules() {
		return rules.clone();
	}

	public void setRules(Rule[] rules) {
		this.rules = rules.clone();
	}
	
	public void setAutomake(boolean value) {
		isAutomake = value;
	}
	
	public boolean isElse() {
		return true;
	}

	public String toString() {
		return GNUMakefileConstants.CONDITIONAL_ELSE;
	}
	
	// ICommand methods so Automake else can be a child of an IRule
	public Process execute(String shell, String[] envp, File dir)
			throws IOException {
		return null;
	}
	
	public boolean shouldBeSilent() {
		return false;
	}
	
	public boolean shouldIgnoreError() {
		return false;
	}
	
	public boolean shouldExecute() {
		return false;
	}
}
