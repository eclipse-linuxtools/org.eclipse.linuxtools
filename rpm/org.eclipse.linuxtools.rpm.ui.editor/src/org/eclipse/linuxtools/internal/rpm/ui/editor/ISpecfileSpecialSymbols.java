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
     * Identifier for comment start. Comment is from this symbol till the line
     * end.
     */
    String COMMENT_START = "#"; //$NON-NLS-1$
}
