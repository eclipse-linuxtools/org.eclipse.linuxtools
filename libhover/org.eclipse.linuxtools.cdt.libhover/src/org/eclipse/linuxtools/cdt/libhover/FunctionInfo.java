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
import java.util.ArrayList;

public class FunctionInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String prototype;
	private String desc;
	private String returnType;
	private ArrayList<String> headers = new ArrayList<>();
	private ArrayList<FunctionInfo> children = null;
	
	/**
	 * Constructor for function.
	 * 
	 * @param name
	 */
	public FunctionInfo(String name) {
		this.name = name;
	}
	
	/**
	 * Get function name.
	 * 
	 * @return function name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get prototype for function.
	 * 
	 * @return prototype string or null
	 */
	public String getPrototype() {
		return prototype;
	}
	
	/**
	 * Set prototype string.
	 * 
	 * @param prototype
	 */
	public void setPrototype(String prototype) {
		this.prototype = prototype;
	}
	
	/**
	 * Get description for function.
	 * 
	 * @return description string or null
	 */
	public String getDescription() {
		return desc;
	}
	
	/**
	 * Set description string for function.
	 * 
	 * @param desc
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}

	/**
	 * Get return type for function.
	 * 
	 * @return return type as string or null
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * Set return type for function.
	 * 
	 * @param returnType
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Get the headers that need including for this function.
	 * 
	 * @return list of header strings
	 */
	public ArrayList<String> getHeaders() {
		return headers;
	}
	
	/**
	 * Add a header to the list of headers needed for this function.
	 * 
	 * @param header the name of the header file to add
	 */
	public void addHeader(String header) {
		headers.add(header);
	}
	
	/**
	 * Add a function with the same name as a child of this function (C++-only).
	 * 
	 * @param info a function with same name as this function.
	 */
	public void addChild(FunctionInfo info) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(info);
	}

	/**
	 * Get the children functions with same name as this function.
	 * 
	 * @return an ArrayList of functions with same name or null.
	 */
	public ArrayList<FunctionInfo> getChildren() {
		return children;
	}
}
