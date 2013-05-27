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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.graphs;

import org.eclipse.linuxtools.systemtap.graphingapi.core.IGraphColorConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.BlockAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.DataPoint;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.NumberType;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;



/**
 * A Bar Graph implementation for the Graphing system.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class BarGraph extends AGraph implements IBlockGraph, IScrollGraph {
	/**
	 * Default constructor for the bar graph.
	 * @param title Title of the graph.
	 * @param style Style code to use.
	 * @param adapter Dataset Adapter for the graph.
	 */
	public BarGraph(GraphComposite parent, int style, String title, IAdapter adapter) {
		super(parent, style, title, adapter);
		this.adapter = adapter;
		fullUpdate = (adapter instanceof BlockAdapter) ? true : false;
		handleUpdateEvent();
	}

	/**
	 * Paints all of the data elements to the screen
	 */
	@Override
	public void paintElementList(GC gc) {
		DataPoint[] points = new DataPoint[0];

		Color temp = gc.getForeground();
		Color temp1 = gc.getBackground();

		Color c;
		Color c1;

		double xSize = super.getSize().x - (super.getXPadding()<<1);
		xSize /= (super.getLocalWidth()+1);
		xSize /= (elementList.length);
		double ySize = super.getSize().y - (super.getYPadding()<<1);
		ySize /= (super.getLocalHeight());

		double px, py;
		double pw=0, ph;

		for(int j=0; j<elementList.length; j++) {
			points = elementList[j].toArray(points);

			c = new Color(getDisplay(), IGraphColorConstants.COLORS[j]);
			c1 = new Color(getDisplay(), c.getRed()>>1, c.getGreen()>>1, c.getBlue()>>1);
			gc.setForeground(c);
			gc.setBackground(c1);
			double width = WIDTH_PERCENT;
			for(DataPoint point :points) {
				px = super.getLocation().x + (((point.x * (elementList.length))-super.getLocalXMin()) * xSize) + super.getXPadding();
				px = px + ((j - elementList.length/2) * (xSize * width));
				pw = (xSize * width);

				py = super.getSize().y - super.getYPadding();
				ph = ((super.getLocalYMax() - point.y) * ySize) + super.getYPadding()-py;
				gc.fillGradientRectangle((int)(px), (int)py, (int)pw, (int)ph, true);
			}
		}

		gc.setForeground(temp);
		gc.setBackground(temp1);
	}

	@Override
	public boolean isMultiGraph() {
		return adapter.getSeriesCount() > 0;
	}

	/**
	 * Handles an update notification for new data in the Data Set. Causes the graph to add
	 * all new samples to the graph, and then repaint itself.
	 */
	@Override
	public void handleUpdateEvent() {
		if(null == adapter) return;

		this.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Object[][] data;
				if(fullUpdate) {
					for(int i=0; i<elementList.length; i++)
						elementList[i].clear();
					data = adapter.getData();
				} else
					data = adapter.getData(removedItems, adapter.getRecordCount());

				if(normalize) {
					double max;
					for(int j,i=0; i<adapter.getSeriesCount(); i++) {
						elementList[i].clear();	//TODO: Only temparary
						max = adapter.getYSeriesMax(i, removedItems, adapter.getRecordCount()).doubleValue() / 100;
						for(j=0; j<data.length; j++) {
							elementList[i].add(new DataPoint(adapter instanceof BlockAdapter ? j : NumberType.obj2num(data[j][0]).doubleValue(),
				  					  					NumberType.obj2num(data[j][i+1]).doubleValue() / max));
						}
					}
				} else {
					for(int j,i=0; i<adapter.getSeriesCount(); i++) {
						elementList[i].clear();	//TODO: Only temparary
						for(j=0; j<data.length; j++) {
							elementList[i].add(new DataPoint(adapter instanceof BlockAdapter ? j : NumberType.obj2num(data[j][0]).doubleValue(),
				  					  					NumberType.obj2num(data[j][i+1]).doubleValue()));
						}
					}
				}
			}
		});
		this.repaint();
	}

	private IAdapter adapter;
	private boolean fullUpdate;
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.graphs.bargraph"; //$NON-NLS-1$
	private static final double WIDTH_PERCENT = 0.8;
}
