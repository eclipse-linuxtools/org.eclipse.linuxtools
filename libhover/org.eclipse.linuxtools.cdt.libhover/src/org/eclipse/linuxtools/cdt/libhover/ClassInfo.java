/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;


public class ClassInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String templateParms[] = new String[0];
    private boolean templateParmsFilled = false;
    private String className;
    private String include;
    private ArrayList<ClassInfo> baseClasses = new ArrayList<>();
    private HashMap<String, MemberInfo> members = new HashMap<>();
    public transient Node classNode;
    private ArrayList<ClassInfo> children = null;
    public ClassInfo(String className, Node classNode) {
        this.className = className;
        this.classNode = classNode;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String newName) {
        className = newName;
    }
    public void addTemplate(ClassInfo child) {
        if (children == null) {
            children = new ArrayList<>();
        }
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

    public void addBaseClass(ClassInfo info) {
        baseClasses.add(info);
    }
}
