/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
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
import java.util.List;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileTag;

public class SpecfilePackage extends SpecfileSection {
    private String description;
    private List<SpecfileSection> sections;
    private List<SpecfileTag> requires;
     private String packageName;
    private String summary;
    private String group;

    public SpecfilePackage(String packageName, Specfile specfile) {
        super("package", specfile); //$NON-NLS-1$
        super.setSpecfile(specfile);
        setPackageName(packageName);
        setPackage(this);
        sections = new ArrayList<>();
        requires = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return getPackageName();
    }

    public void addSection(SpecfileSection section) {
        sections.add(section);
    }


    public SpecfileSection[] getSections() {
        SpecfileSection[] toReturn = new SpecfileSection[sections.size()];
        return sections.toArray(toReturn);
    }

    public boolean hasChildren() {
        if (sections != null && sections.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public SpecfilePackage getPackage() {
        return this;
    }

    @Override
    public String getPackageName() {
        return resolve(this.packageName);
    }

    /**
     * Returns the full package name.
     *
     * @return The name of the package with the common part appended in front.
     */
    public String getFullPackageName() {
        if (getSpecfile().getName().equals(getPackageName())){
            return getPackageName();
        }
        return getSpecfile().getName()+"-" + getPackageName(); //$NON-NLS-1$
    }

    public final void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    public boolean isMainPackage() {
        if (getSpecfile().getName().equals(getPackageName())){
            return true;
        }
        return false;
    }

    /**
     * @param require the require to add
     */
    public void addRequire(SpecfileTag require) {
        requires.add(require);
    }

    public List<SpecfileTag> getRequires() {
        return requires;
    }
}
