/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.structures;


/**
 * A structure for containing extra information of SystemTap functions (and their parameters).
 * @since 3.0
 */
public class FunctionNodeData {
	/**
	 * The descriptor used for unresolvable types.
	 */
	public static final String UNKNOWN_TYPE = "unknown"; //$NON-NLS-1$

	private String line;
	private String type;

	/**
	 * @return <code>true</code> if this node is a parameter,
	 * or <code>false</code> if it is a function.
	 */
	public boolean isParam() {
		return line == null;
	}

	/**
	 * Get the original script text that defines a function.
	 * @return The entire text contents of the original function definition,
	 * if this node is a function; <code>null</code> otherwise.
	 */
	@Override
	public String toString() {
		return line;
	}

	/**
	 * @return The <code>String</code> representation of the return type of the
	 * node's function (<code>null</code> for void functions), or the type of its parameter.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Create a new instance of function node information. (Note that the name of a function
	 * or parameter is stored in a {@link TreeNode}, not here.)
	 * @param line For a function, set this to the original script text that defines this function.
	 * For a parameter, set this to <code>null</code>.
	 * @param type The <code>String</code> representation of the return type of the
	 * node's function, or the type of its parameter.
	 */
	public FunctionNodeData(String line, String type) {
		this.line = line;
		this.type = line == null && type == null ? UNKNOWN_TYPE : type; // Parameters can't be void.
	}
}
