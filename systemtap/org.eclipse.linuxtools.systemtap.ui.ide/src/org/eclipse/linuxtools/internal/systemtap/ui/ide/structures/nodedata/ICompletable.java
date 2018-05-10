/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc.
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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;

/**
 * An interface for providing completion text for any object
 * that can be typed in a SystemTap script.
 */
public interface ICompletable {
    /**
     * @return The text associated with this object that can be auto-completed
     * and inserted into a script.
     */
    String getCompletionText();
}
