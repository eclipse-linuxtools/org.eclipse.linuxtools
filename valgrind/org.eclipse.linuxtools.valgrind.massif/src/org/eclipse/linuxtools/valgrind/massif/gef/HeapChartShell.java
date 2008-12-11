/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif.gef;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.linuxtools.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class HeapChartShell {
	protected Shell shell;

	public HeapChartShell(Display display, MassifSnapshot[] snapshots) {
		shell = new Shell(display);	
		shell.setText(Messages.getString("HeapChartShell.Heap_Allocation_Chart")); //$NON-NLS-1$
		shell.setImage(MassifPlugin.imageDescriptorFromPlugin(ValgrindUIPlugin.PLUGIN_ID, "icons/valgrind-icon.png").createImage()); //$NON-NLS-1$
		shell.setBackground(ColorConstants.white);
		shell.setSize(500, 400);
		
		LightweightSystem lws = new LightweightSystem(shell);
		lws.setContents(new MassifHeapChart(snapshots));
	}
	
	public void open() {
		shell.open();
	}
}
