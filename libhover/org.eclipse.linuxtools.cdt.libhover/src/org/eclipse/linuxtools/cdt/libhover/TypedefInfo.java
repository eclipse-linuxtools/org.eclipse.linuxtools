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

public class TypedefInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String[] templates = new String[0];
	private String typedefName;
	private String transformedType;
	private ArrayList<TypedefInfo> children = null;
	public TypedefInfo(String typedefName, String transformedType) {
		this.typedefName = typedefName;
		this.transformedType = transformedType;
	}
	public String getTypedefName() {
		return typedefName;
	}
	public void setTypedefName(String name) {
		typedefName = name;
	}
	
	private String[] getTemplateArgs(String str) {
		ArrayList<String> list = new ArrayList<String>();
		int index = 0;
		int lastIndex = 0;
		int templateCounter = 0;
		while (index < str.length()) {
			char ch = str.charAt(index);
			if (ch == '<') {
				if (templateCounter == 0)
					lastIndex = index + 1;
				templateCounter++;
			} else if (ch == '>') {
				templateCounter--;
			} else if (ch == ',' && templateCounter == 1) {
				// FIXME: do we have to strip out all blanks here?
				list.add(str.substring(lastIndex, index).trim());
				lastIndex = index + 1;
			}
			++index;
		}
		String[] args = new String[list.size()];
		return list.toArray(args);
	}
	
	public String getTransformedType(String className) {
		int index = className.indexOf('<');
		if (index > 0) {
			TypedefInfo e = this;
			// Search the children list in case the given class name
			// matches a specific template case.
			ArrayList<TypedefInfo> children = getChildren();
			for (int x = 0; x < children.size(); ++x) {
				TypedefInfo child = children.get(x);
				if (className.matches(child.getTypedefName())) {
					e = child;
					break;
				}
			}
			String[] templates = e.getTemplates();
			String transformedName = e.transformedType;
			// Check if there are any template arguments to replace.  If not,
			// we can just return the transformed type we have.
			if (templates.length <= 0)
				return transformedName;
			String[] args = getTemplateArgs(className);
			String[] templateArgs = getTemplateArgs(e.getTypedefName());
			int j = 0;
			// For every argument that doesn't match up, it must be a template
			// parameter so we'll replace the template parameter name with the
			// supplied parameter.  We have to query the template parameter list
			// for the names to replace because for partial specific templates
			// those names will have been replaced with regex sequences designed to
			// help us identify when the specific template has matched.  For example,
			// <char, _Tp> will be stored as <char,[a-zA-Z0-9_]*> and if we have
			// <char,char> we will replace _Tp with char in the transformed type.
			for (int i = 0; i < args.length; ++i) {
				if (!args[i].equals(templateArgs[i])) {
					transformedName = transformedName.replaceAll(templates[j], args[i]);
					++j;
				}
			}
			return transformedName;
		} else {
			// There is no template specified.
			return transformedType;
		}
	}
	
	public void addTypedef(TypedefInfo typedef) {
		if (children == null)
			children = new ArrayList<TypedefInfo>();
		children.add(typedef);
	}
	public ArrayList<TypedefInfo> getChildren() {
		return children;
	}
	public void copyTemplates(String[] newTemplates) {
		templates = new String[newTemplates.length];
		for (int i = 0; i < templates.length; ++i)
			templates[i] = newTemplates[i];
	}
	public String[] getTemplates() {
		return templates;
	}
}

