package org.eclipse.linuxtools.threadprofiler.internal;

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
