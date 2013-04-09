/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphingapi.core;

import java.util.ArrayList;

/**
 * The main plugin class to be used in the desktop.
 */
public class GraphingAPINonUIPlugin {

	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T>[] createArrayList(int size, T instance) {
		return new ArrayList[size];
	}
}
