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


public class GraphCoordinateSystem {
	
//	TransformationMatrix dataToAxis;	//TODO: Use later to scale data directly
	private TransformationMatrix axisToPixel;
	private String label;
	
	/**
	 * Creates a coordinate system with its origin at the given x,y
	 * 
	 * For graphs, (x,y) should correspond to the (0,0) point on the 
	 * graph axis.
	 * 
	 * @param x
	 * @param y
	 */
	public GraphCoordinateSystem(int x, int y) {
//		double[][] dta = {{1.,0.,0},{0.,1.,0.}};
//		dataToAxis = new TransformationMatrix(dta);
		
		double[][] atp = {{1.,0.,0.},{0.,-1.,0.}};
		axisToPixel = new TransformationMatrix(atp);
		axisToPixel.setYShift(y);
		axisToPixel.setXShift(x);
	}
	
	public DataPoint dataToPixel(DataPoint point) {
		int[] values = point.getValues();
		return dataToPixel(values);
	}
	
	public DataPoint dataToPixel(int[] values) {
		if (values.length != 3) 
			return null;
		
//		return new DataPoint(axisToPixel.times(dataToAxis.times(values)), 0);
		return new DataPoint(axisToPixel.times(values), 0);
	}
	
	public void scaleATPBy(double scale) {
		axisToPixel.scaleBy(scale);
	}
	
//	public void scaleDTABy(double scale) {
//		dataToAxis.scaleBy(scale);
//	}
	

	/**
	 * d will automatically be inverted to properly display
	 * @param d
	 */
	public void setYScale(double d) {
		axisToPixel.setYScale(-1 * d);
	}
	
	public void setXScale(double d) {
		axisToPixel.setXScale(d);
	}
	

	public void setYOffset(int yCoordinate) {
		axisToPixel.setYShift(yCoordinate); 
	}
	
	public void setXOffset(int xCoordinate) {
		axisToPixel.setXShift(xCoordinate); 
	}
	
	public int getXOffset() {
		return axisToPixel.getXShift();
	}
	
	public int getYOffset() {
		return axisToPixel.getYShift();
	}

	public double getYScale() {
		return axisToPixel.getYScale();
	}

	public TransformationMatrix getAxisToPixel() {
		return axisToPixel;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

}
