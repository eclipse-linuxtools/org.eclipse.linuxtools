/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;

public class LibHoverInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	public HashMap<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
	public HashMap<String, TypedefInfo> typedefs = new HashMap<String, TypedefInfo>();
	public TreeMap<String, FunctionInfo> functions = new TreeMap<String, FunctionInfo>();

}
