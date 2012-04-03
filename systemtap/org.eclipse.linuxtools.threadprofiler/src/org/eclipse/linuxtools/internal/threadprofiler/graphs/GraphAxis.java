package org.eclipse.linuxtools.internal.threadprofiler.graphs;

public class GraphAxis {
	
	
	//NOTE: xStart is a y-coordinate and yStart is an x-coordinate. That's how axes work.
	private String topLabel;
	
	public GraphAxis(int xAxisCoordinate, int yAxisCoordinate) {
		topLabel = "";
	}
	
	public void setTopLabel(String value) {
		// TODO If needed, replace with labels for grid lines
		topLabel = value;
	}
	
	/**
	 * @return The highest value
	 */
	public String getTopLabel() {
		return topLabel;
	}
	
}
