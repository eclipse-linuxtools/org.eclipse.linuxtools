/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.compare.structuremergeviewer.StructureDiffViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

/**
 * ViewerCreator which creates a StructureDiffViewer with a
 * SpecStructureCreator.
 *
 */
public class SpecStructureMergeViewerCreator implements IViewerCreator {

    @Override
    public Viewer createViewer(Composite parent, CompareConfiguration config) {
        StructureDiffViewer diffViewer = new StructureDiffViewer(parent, config);
        diffViewer.setStructureCreator(new SpecStructureCreator());
        return diffViewer;
    }

}
