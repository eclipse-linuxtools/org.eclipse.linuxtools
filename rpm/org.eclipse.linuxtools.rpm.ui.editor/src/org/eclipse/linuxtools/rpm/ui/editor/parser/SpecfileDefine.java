/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileTag;

public class SpecfileDefine extends SpecfileTag {

	public SpecfileDefine(String name, int value, Specfile specfile, SpecfilePackage parent) {
		super(name, value, specfile, parent);
	}

	public SpecfileDefine(String name, String value, Specfile specfile, SpecfilePackage parent) {
		super(name, value, specfile, parent);
	}

	public SpecfileDefine(SpecfileTag tag) {
		super();
		setName(tag.getName().toLowerCase());
		setSpecfile(tag.getSpecfile());
		setLineNumber(tag.getLineNumber());
		setParent(tag.getParent());
		if (tag.getTagType().equals(TagType.STRING)) {
			setValue(tag.getStringValue());
		}
		if (tag.getTagType().equals(TagType.INT)) {
			setValue(tag.getIntValue());
		}
	}

}
