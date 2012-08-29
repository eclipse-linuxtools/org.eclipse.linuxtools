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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class ClassInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String templateParms[] = new String[0];
	private boolean templateParmsFilled = false;
	private String className;
	private String id;
	private String include;
	private ArrayList<ClassInfo> baseClasses = new ArrayList<ClassInfo>();
	private HashMap<String, MemberInfo> members = new HashMap<String, MemberInfo>();
	private transient Document document;
	public transient Node classNode;
	private ArrayList<ClassInfo> children = null;
	public ClassInfo(String className, String id, Node classNode) {
		this.className = className;
		this.id = id;
		this.classNode = classNode;
	}
	public String getClassName() {
		return className;
	}
	public Node getClassNode() {
		if (classNode == null)
			classNode = document.getElementById(id);
		return classNode;
	}
	public void setDocument (Document d) {
		document = d;
	}
	public void setClassName(String newName) {
		className = newName;
	}
	public void addTemplate(ClassInfo child) {
		if (children == null)
			children = new ArrayList<ClassInfo>();
		children.add(child);
	}
	public boolean areTemplateParmsFilled() {
		return templateParmsFilled;
	}
	
	public String[] getTemplateParms() {
		return templateParms;
	}
	
	public void setTemplateParms(String[] templateParms) {
		templateParmsFilled = true;
		this.templateParms = templateParms;
	}
	
	public String getInclude() {
		return include;
	}
	
	public void setInclude(String include) {
		this.include = include;
	}
	
	public ArrayList<ClassInfo> getChildren() {
		return children;
	}
	
	public MemberInfo getMember(String name) {
		return members.get(name);
	}
	
	public void addMember(MemberInfo info) {
		String name = info.getName();
		MemberInfo member = members.get(name);
		if (member != null)
			member.addChild(info);
		else
			members.put(name, info);
	}
	
	public MemberInfo[] getMembers(String nameStart) {
		ArrayList<MemberInfo> matchList = new ArrayList<MemberInfo>();
		Collection<MemberInfo> values = members.values();
		for (Iterator<MemberInfo> i = values.iterator(); i.hasNext();) {
			MemberInfo k = i.next();
			if (k.getName().startsWith(nameStart)) {
				matchList.add(k);
				ArrayList<MemberInfo> children = k.getChildren();
				if (children != null) {
					for (Iterator<MemberInfo> j = children.iterator(); j.hasNext();) {
						MemberInfo child = i.next();
						matchList.add(child);
					}
				}
			}
		}
		MemberInfo[] matches = new MemberInfo[matchList.size()];
		return matchList.toArray(matches);
	}
	
	public ArrayList<ClassInfo> getBaseClasses() {
		return baseClasses;
	}
	
	public void addBaseClass(ClassInfo info) {
		baseClasses.add(info);
	}
}
