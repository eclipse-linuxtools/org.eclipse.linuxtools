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

package org.eclipse.linuxtools.internal.rpm.ui.editor.parser;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;

public class SpecfileTag extends SpecfileElement {

	public enum TagType {
		INT, STRING
	}

	private TagType tagType;

	private String stringValue;
	private int intValue;
	private SpecfilePackage parent;

	public SpecfileTag() {
		// Empty constructor
	}

	public SpecfileTag(String name, String value, Specfile specfile,
			SpecfilePackage parentPackage) {
		setName(name);
		this.stringValue = value;
		this.tagType = TagType.STRING;
		super.setSpecfile(specfile);
		this.parent = parentPackage;
	}

	public SpecfileTag(String name, int value, Specfile specfile,
			SpecfilePackage parentPackage) {
		setName(name);
		this.intValue = value;
		this.tagType = TagType.INT;
		super.setSpecfile(specfile);
		this.parent = parentPackage;
	}

	public String getStringValue() {
		if (tagType == TagType.INT) {
			return Integer.toString(intValue);
		}
		return resolve(stringValue);
	}

	public void setValue(String value) {
		this.tagType = TagType.STRING;
		this.stringValue = value;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setValue(int value) {
		this.tagType = TagType.INT;
		this.intValue = value;
	}

	public SpecfilePackage getParent() {
		return parent;
	}

	public void setParent(SpecfilePackage parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		if (tagType == TagType.INT) {
			return getName() + ": " + getIntValue(); //$NON-NLS-1$
		}
		String tagValue = getStringValue();
		if ((tagValue != null) && (tagValue.length() > 0)
				&& (tagValue.indexOf('%') > 0)) {
			return getName() + ": " + super.resolve(tagValue); //$NON-NLS-1$
		}
		return getName() + ": " + getStringValue(); //$NON-NLS-1$
	}

	public TagType getTagType() {
		return tagType;
	}
}
