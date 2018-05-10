/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.core;

import java.util.ArrayList;

/**
 * The main plugin class to be used in the desktop.
 */
public class GraphingCorePlugin {

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T>[] createArrayList(int size) {
        return new ArrayList[size];
    }
}
