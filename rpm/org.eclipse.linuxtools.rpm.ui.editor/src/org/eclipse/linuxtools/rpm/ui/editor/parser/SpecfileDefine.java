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
