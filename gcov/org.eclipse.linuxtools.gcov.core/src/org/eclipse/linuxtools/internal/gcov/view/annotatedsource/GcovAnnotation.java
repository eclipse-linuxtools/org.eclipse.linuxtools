/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.osgi.util.NLS;

/**
 * Representation of a GCov source file annotation.
 */
public class GcovAnnotation extends Annotation {

    private final Position position;
    private final long count;

    public GcovAnnotation(int offset, int length, long count, String type) {
        super(type, false, null);
        this.position = new Position(offset, length);
        this.count = count;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String getText() {
        if (count == 0) {
            return Messages.CoverageAnnotationColumn_line_never_exec;
        } else if (count == 1) {
            return Messages.CoverageAnnotationColumn_line_exec_once;
        } else if ( count > 0) {
            return NLS.bind(Messages.CoverageAnnotationColumn_line_mulitiple_exec, Long.toString(count));
        } else  {
            return Messages.CoverageAnnotationColumn_non_exec_line;
        }
    }
}
