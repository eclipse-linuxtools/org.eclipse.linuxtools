/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.charting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifTreeLabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

public class ChartLocationsDialog extends ListDialog {

    private List<MassifHeapTreeNode> allocs;

    public ChartLocationsDialog(Shell parent) {
        super(parent);
        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new MassifTreeLabelProvider());
        setMessage(Messages.getString("ChartLocationsDialog.Message"));  //$NON-NLS-1$
        setTitle(Messages.getString("ChartLocationsDialog.Title")); //$NON-NLS-1$
    }

    @Override
    public void setInput(Object input) {
        setInput((MassifSnapshot) input);
    }

    public void setInput(MassifSnapshot snapshot) {
        MassifHeapTreeNode node = snapshot.getRoot();

        allocs = new ArrayList<>(node.getChildren().length);

        for (MassifHeapTreeNode alloc : node.getChildren()) {
            if (alloc.hasSourceFile()) {
                allocs.add(alloc);
            }
        }
        super.setInput(allocs);
    }

    @Override
    public int open() {
        int result = OK;

        if (allocs.size() > 1) {
            result = super.open();
        }

        return result;
    }

    public void openEditorForResult() {
        MassifHeapTreeNode element = null;

        if (allocs.size() > 1) {
            element = (MassifHeapTreeNode) getResult()[0];
        }
        else if (allocs.size() == 1) {
            element = allocs.get(0);
        }

        if (element != null) {
            MassifPlugin.getDefault().openEditorForNode(element);
        }
    }

}
