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

public class MemberInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String prototype;
	private String desc;
	private String returnType;
	private String[] paramTypes = new String[0];
	private ArrayList<MemberInfo> children = null;
	
	/**
	 * Constructor for member.
	 * 
	 * @param name
	 */
	public MemberInfo(String name) {
		this.name = name;
	}
	
	/**
	 * Get member name.
	 * 
	 * @return member name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get prototype for member.
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
	 * Get description for member.
	 * 
	 * @return description string or null
	 */
	public String getDescription() {
		return desc;
	}
	
	/**
	 * Set description string for member.
	 * 
	 * @param desc
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}

	/**
	 * Get param types for member.
	 * 
	 * @return return array of param types
	 */
	public String[] getParamTypes() {
		return paramTypes;
	}

	/**
	 * Set param types for member.
	 * 
	 * @param array of param types
	 */
	public void setParamTypes(String[] paramTypes) {
		this.paramTypes = paramTypes;
	}
	/**
	 * Get return type for member.
	 * 
	 * @return return type as string or null
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * Set return type for member.
	 * 
	 * @param returnType
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Add a member with the same name as a child of this member.
	 * 
	 * @param info a member with same name as this member.
	 */
	public void addChild(MemberInfo info) {
		if (children == null)
			children = new ArrayList<MemberInfo>();
		children.add(info);
	}

	/**
	 * Get the children members with same name as this member.
	 * 
	 * @return an ArrayList of members with same name or null.
	 */
	public ArrayList<MemberInfo> getChildren() {
		return children;
	}
}
