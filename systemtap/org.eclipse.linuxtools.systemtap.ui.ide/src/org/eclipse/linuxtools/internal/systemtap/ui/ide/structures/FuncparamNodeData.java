/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;


/**
 * A structure for containing extra information of SystemTap function parameters.
 * @since 3.0
 */
public class FuncparamNodeData implements ISingleTypedNode {

	private final String line;
	private final String type;

	@Override
	public String toString() {
		return line;
	}

	@Override
	public String getType() {
		return type;
	}

	/**
	 * Create a new instance of function parameter information. (Note that the name of a function
	 * or parameter is stored in a {@link TreeNode}, not here.)
	 * @param line The <code>String</code> representation of the entire parameter.
	 * @param type The <code>String</code> representation of only the parameter's type.
	 */
	public FuncparamNodeData(String line, String type) {
		this.line = line;
		this.type = type == null ? FunctionParser.UNKNOWN_TYPE : type; // Parameters can't be void.
	}
}
