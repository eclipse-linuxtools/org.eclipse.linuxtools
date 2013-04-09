/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Graphing Primitive Interface, defines the basic mechanism through which graph objects are drawn
 * to a Graph type.
 * @author Henry Hughes
 * @author Ryan Morse
 *
 */
public interface IGraphPrimitive {
	/**
	 * Paint method, called by the Graph object to paint the graph.
	 * @param gc GC object to render with.
	 */
	public void paint(GC gc);
	
	/**
	 * CalculateBounds method, called by the Graph object when the graph's bounds changed (say
	 * if a data sample was added) in order to allow the primitives to update their locations.
	 * 
	 * The behavior of this method is entirely left up to the primitive author, but it is ideally
	 * used to take care of location and bounds calculations for graph objects, rather than performing
	 * such calculations during the paint method.
	 */
	public void calculateBounds();
	
	/**
	 * This method determines if the graph primitive is currently shown on the Graph surface.
	 * @return True if the primitive is currently displayed on the graph surface.
	 */
	public boolean isVisible();
	
	/**
	 * This method determines if the primitive covers the given point. If the point is inside the
	 * bounds of the graph primitive, the point is defined as isUnder=true.
	 * @param loc Location to test.
	 * @return True if the location is inside the bounds of the primitive.
	 */
	public boolean isUnder(Point loc);
}