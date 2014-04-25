/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.swt.graphics.RGB;

/**
 * Color scheme to use for syntax highlighting.
 *
 * @author klee (Kyu Lee)
 *
 */
public interface IChangeLogColorConstants {
    RGB FILE_NAME = new RGB(128, 0, 0);

    RGB FUNC_NAME = new RGB(0, 128, 0);

    RGB TEXT = new RGB(0, 0, 0);

    RGB EMAIL = new RGB(0, 0, 128);

    RGB DATE = new RGB(64, 64, 0);

    RGB AUTHOR = new RGB(0, 64, 64);
}
