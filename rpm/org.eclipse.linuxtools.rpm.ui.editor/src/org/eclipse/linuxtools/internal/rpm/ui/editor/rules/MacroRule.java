/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.rules;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileSpecialSymbols;

/**
 * A rule for matching %{...} sections in spec file.
 *
 */
public class MacroRule extends SingleLineRule {

	/**
	 * Creates a SingleLineRule by using macros start and end identifiers for start
	 * and end delimiters.
	 *
	 * @param token The token to look into.
	 */
	public MacroRule(IToken token) {
		super(ISpecfileSpecialSymbols.MACRO_START_LONG, ISpecfileSpecialSymbols.MACRO_END_LONG, token);
	}

}
