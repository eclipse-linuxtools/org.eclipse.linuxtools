/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpecfilePackage extends SpecfileSection {
	private String description;
	private List sections;
	private String packageName;

	public SpecfilePackage(String packageName, Specfile specfile) {
		super("package", specfile);
		super.setSpecfile(specfile);
		setPackageName(packageName);
                setPackage(this);
                sections = new ArrayList();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return getPackageName();
	}

	public void addSection(SpecfileSection section) {
		sections.add(section);
	}

	
	public SpecfileSection[] getSections() {
		SpecfileSection[] toReturn = new SpecfileSection[sections.size()];
		int i = 0;
		for (Iterator sectionIter = sections.iterator(); sectionIter.hasNext();) {
			SpecfileSection section = (SpecfileSection) sectionIter.next();
			toReturn[i] = section;
			i++;
		}
		return toReturn;
	}

	public boolean hasChildren() {
		if (sections != null && sections.size() > 0)
			return true;
		return false;
	}

        public SpecfilePackage getPackage() {
                return this;
        }
            
            
        public String getPackageName(){
                return resolve(this.packageName);
        }
        public void setPackageName(String packageName) {
                this.packageName = packageName; 
        }
}
