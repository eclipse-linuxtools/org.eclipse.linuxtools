package org.eclipse.linuxtools.internal.threadprofiler;

public class DataPoint {
	public static final int DATA_POINT = 0;
	public static final int AXIS_POINT = 1;
	public static final int PIXEL_POINT = 2;
	
	/** Inactive, do not draw this point */
	public static final int THREAD_INACTIVE = 0;
	
	/** Active, draw this point */
	public static final int THREAD_ACTIVE = 1;
	
	
	private int[] values;
	
	private int type;

	public DataPoint(int x, int y, int type) {
		values = new int[3];
		values[0] = x;
		values[1] = y;
		values[2] = 1;
		this.type = type; 
	}
	
	public DataPoint(int[] values, int type) {
		this.values = values;
		this.type = type;
	}
	
	/**
	 * Rounds to an integer
	 * @return
	 */
	public int getX() {
		return values[0];
	}

	public void setX(int x) {
		this.values[0] = x;
	}

	/** 
	 * Rounds to an integer
	 * @return
	 */
	public int getY() {
		return values[1];
	}

	public void setY(int y) {
		this.values[1] = y;
	}
	
	public int getType() {
		return type;
	}

	public int[] getValues() {
		return values;
	}
}
