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

public class GraphPointBuffer extends CircularPointBuffer{
	
	private final int style;
	private final String name;

	public GraphPointBuffer(int size, int style, String name) {
		super(size);
		this.style = style;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getStyle() {
		return style;
	}

}
