/***********************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 * Elliott Baron <ebaron@redhat.com> - Modified implementation
 ***********************************************************************/
package org.eclipse.linuxtools.valgrind.massif.birt;

import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ChartShell {
	protected Shell shell;
	protected MassifSnapshot[] snapshots;

	public ChartShell(Display display, MassifSnapshot[] snapshots) {
		this.snapshots = snapshots;
		if (snapshots.length > 0) {
			shell = new Shell(display);
			shell.setLayout(new GridLayout());
			shell.setLayoutData(new GridData(GridData.FILL_BOTH));
			shell.setText(Messages.getString("ChartShell.Heap_Allocation_Chart")); //$NON-NLS-1$

			shell.open();
		}
	}

	public void open() {
		Canvas paintCanvas = new Canvas(shell, SWT.BORDER);
		paintCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		paintCanvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		ChartRenderer renderer = new ChartRenderer();
		paintCanvas.addPaintListener(renderer);
		paintCanvas.addControlListener(renderer);
		renderer.setCanvas(paintCanvas);
		renderer.renderModel(ChartBuilder.createLine(snapshots));
	}
}
