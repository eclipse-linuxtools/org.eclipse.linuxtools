/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.threadprofiler;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


/**
 * Represents a colour scheme. Should be made serializable and save-able.
 * @author chwang
 *
 */
public class GraphColorScheme {
	private List<Color> graphColors;
	private Color axisColor;
	private Color fontColor;
	public final Color defaultColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	public static final GraphColorScheme DEFAULT_SCHEME = new GraphColorScheme(	
			new Color[] { new Color(Display.getDefault(), 255, 127, 80),
			new Color(Display.getDefault(), 138, 43, 226), new Color(Display.getDefault(), 184, 243, 80)},
			Display.getDefault().getSystemColor(SWT.COLOR_GRAY), Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
	
	
	public GraphColorScheme(Color[] graphColors, Color axisColor, Color fontColor) {
		this.graphColors = Arrays.asList(graphColors);
		this.axisColor = axisColor;
		this.fontColor = fontColor;
	}
	
	/**
	 * @return
	 * 		Array of colors to use for drawing graphs 
	 */
	public Iterator<Color> getIterator() {
		return graphColors.listIterator();
	}
	
	/**
	 * @return
	 * 		Array of colors to use for drawing graphs 
	 */
	public void setGraphColors(Color[] newColors) {
		this.graphColors = Arrays.asList(newColors);
	}

	/**
	 * @return
	 * 		Axis color to be used for axis lines
	 */
	public Color getAxisColor() {
		return axisColor;
	}
	
	/**
	 * 
	 * @return
	 * 		Font color to be used for labels
	 */
	public Color getFontColor() {
		return fontColor;
	}
	
	public void setAxisColor(Color c) {
		axisColor = c;
	}
	
	public void setFontColor(Color c) {
		fontColor = c;
	}

}
