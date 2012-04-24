/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.charting;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ChartEditorInput implements IEditorInput {
	
	protected HeapChart chart;
	protected MassifViewPart view;
	protected String name;
	protected Integer pid;

	public ChartEditorInput(HeapChart chart, MassifViewPart view, String name, Integer pid) {
		this.chart = chart;
		this.view = view;
		this.name = name;
		this.pid = pid;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID, "icons/linecharticon.gif"); //$NON-NLS-1$
	}

	public String getName() {		
		return name;
	}
	
	public Integer getPid() {
		return pid;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return NLS.bind(Messages.getString("ChartEditorInput.Heap_allocation_chart_for"), name); //$NON-NLS-1$
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public HeapChart getChart() {
		return chart;
	}
	
	public MassifViewPart getView() {
		return view;
	}

}
