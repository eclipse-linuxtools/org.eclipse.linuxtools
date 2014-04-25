/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/package org.eclipse.linuxtools.rpm.ui.editor.tests;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class SpecfileTestFailure {

    private Position position;
    private Annotation annotation;

    public SpecfileTestFailure(Annotation annotation, Position position) {
        this.annotation = annotation;
        this.position = position;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Position getPosition() {
        return position;
    }
}
