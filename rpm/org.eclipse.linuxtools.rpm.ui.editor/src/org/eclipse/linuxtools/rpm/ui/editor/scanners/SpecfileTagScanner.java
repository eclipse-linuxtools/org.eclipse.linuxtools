/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.scanners;
//package org.eclipse.linuxtools.rpm.ui.editor;
//
//import org.eclipse.jface.text.*;
//import org.eclipse.jface.text.rules.*;
//
//public class SpecfileTagScanner extends RuleBasedScanner {
//
//	public SpecfileTagScanner(ColorManager manager) {
//		IToken string =
//			new Token(
//				new TextAttribute(manager.getColor(ISpecfileColorConstants.STRING)));
//
//		IRule[] rules = new IRule[3];
//
//		// Add rule for double quotes
//		rules[0] = new SingleLineRule("\"", "\"", string, '\\');
//		// Add a rule for single quotes
//		rules[1] = new SingleLineRule("'", "'", string, '\\');
//		// Add generic whitespace rule.
//		rules[2] = new WhitespaceRule(new SpecfileWhitespaceDetector());
//
//		setRules(rules);
//	}
//}
