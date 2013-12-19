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
package org.eclipse.linuxtools.internal.valgrind.massif.charting;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ChartPNG {

	protected HeapChart cm = null;
	
	public ChartPNG(HeapChart chart) {
		cm = chart;
	}

	public void renderPNG(IPath pngPath) {
		Composite comp = cm.getChartControl();
		Display dsp = Display.getCurrent();
		GC gc = new GC(comp);
		Image img = new Image(dsp, comp.getSize().x + 1, comp.getSize().y + 1);
		gc.copyArea(img, 0, 0);
		gc.dispose();
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {img.getImageData()};
		imageLoader.save(pngPath.toOSString(), SWT.IMAGE_PNG);
	}

	public HeapChart getDesignTimeModel() {
		return cm;
	}

	public Object peerInstance() {
		return this;
	}

	public void regenerateChart() {
	}

	public void repaintChart() {
	}
	
	/**
	 * Shows the Valgrind view in the active page and gives it focus.
	 */
	public void showView() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					activePage.showView(ValgrindUIConstants.VIEW_ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}			
		});
	}
	
}
