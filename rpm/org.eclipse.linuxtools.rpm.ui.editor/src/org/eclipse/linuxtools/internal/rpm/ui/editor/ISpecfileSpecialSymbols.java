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
package org.eclipse.linuxtools.internal.rpm.ui.editor;

/**
 * Constants for all the special symbols in spec file.
 *
 */
public interface ISpecfileSpecialSymbols {
	/**
	 * Identifier for macro start - long one.
	 */
	String MACRO_START_LONG = "%{"; //$NON-NLS-1$
	/**
	 * Identifier for macro end - long one.
	 */
	String MACRO_END_LONG = "}"; //$NON-NLS-1$

	/**
	 * Identifier for macro start - short one.
	 */
	String MACRO_START_SHORT = "%"; //$NON-NLS-1$
	/**
	 * Identifier for comment start. Comment is from this symbol till the line end.
	 */
	String COMMENT_START = "#"; //$NON-NLS-1$
}
