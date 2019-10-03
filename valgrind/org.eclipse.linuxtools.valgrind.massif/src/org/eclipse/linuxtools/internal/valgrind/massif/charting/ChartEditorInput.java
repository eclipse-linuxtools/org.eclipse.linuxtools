/*******************************************************************************
 * Copyright (c) 2008, 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.charting;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ChartEditorInput implements IEditorInput {

    private HeapChart chart;
    private MassifViewPart view;
    private String name;
    private Integer pid;

    public ChartEditorInput(HeapChart chart, MassifViewPart view, String name, Integer pid) {
        this.chart = chart;
        this.view = view;
        this.name = name;
        this.pid = pid;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ResourceLocator.imageDescriptorFromBundle(MassifPlugin.PLUGIN_ID, "icons/linecharticon.gif").get(); //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return name;
    }

    public Integer getPid() {
        return pid;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return NLS.bind(Messages.getString("ChartEditorInput.Heap_allocation_chart_for"), name); //$NON-NLS-1$
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    public HeapChart getChart() {
        return chart;
    }

    public MassifViewPart getView() {
        return view;
    }

}
