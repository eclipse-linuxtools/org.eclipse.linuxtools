package org.eclipse.linuxtools.internal.threadprofiler;



/**
 * Basic transformation matrix. Only has support for scaling/shifting
 * @author chwang
 *
 */
public class TransformationMatrix {
	
	public double[][] entries;
	public int columns, rows;
	
	public TransformationMatrix (double[][] values){
		columns = values[0].length;
		rows = values.length;
		entries = values;
	}
	
	public void setXScale(double scale) {
		entries[0][0] = scale;
	}

	public void setXShift(double shift) {
		entries[0][2] = shift;
	}
	public void setYScale(double scale) {
		entries[1][1] = scale;
	}
	
	public double getYScale() {
		return entries[1][1];
	}

	public void setYShift(double shift) {
		entries[1][2] = shift;
	}
	
	public void scaleBy(double scale) {
		entries[0][0] = entries[0][0]*scale;
		entries[1][1] = entries[1][1]*scale;
	}
	
	/**
	 * Does simple matrix multiplication. Currently ignores any shearing effects.
	 * Can change the amount of shifting by adjusting the third vector coordinate.
	 * @param vector of coordinates to multiply
	 * @return
	 */
	public int[] times(int[] vector) {
		if (vector.length != 3)
			return null;
		
		int[] retval = new int[3];
		retval[0] = (int) ( vector[0]*entries[0][0] + vector[2]*entries[0][2]);	//x transform
		retval[1] = (int) (vector[1]*entries[1][1] + vector[2]*entries[1][2]);	//y transform
		retval[2] = 1;											//Ghost coordinate
		
		return retval;
		
	}

	public int getXShift() {
		return (int) entries[0][2];
	}
	
	public int getYShift() {
		return (int) entries[1][2];
	}
}
