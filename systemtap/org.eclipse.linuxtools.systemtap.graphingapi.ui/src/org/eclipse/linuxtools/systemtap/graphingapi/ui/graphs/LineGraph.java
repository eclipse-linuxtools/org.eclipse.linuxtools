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
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.ScrollAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.DataPoint;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.NumberType;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;



/**
 * A line graph implementation for the graphing system.
 * @author Ryan Morse
 * @author Henry Hughes
 */
public class LineGraph extends AGraph implements IScrollGraph {
	/**
	 * Default constructor for LineGraph. Simply invokes the constructor from <code>ScatterGraph</code>
	 * and fires the Update Event when its done, causing the graph to draw itself.
	 */
	public LineGraph(GraphComposite parent, int style, String title, ScrollAdapter adapter) {
		super(parent, style, title, adapter);
		this.adapter = adapter;
		handleUpdateEvent();
	}

	@Override
	public void paintElementList(GC gc) {
		DataPoint[] points = new DataPoint[0];

		Color temp = gc.getForeground();
		Color c;

		double xSize = super.getSize().x - (super.getXPadding()<<1);
		xSize /= (super.getLocalWidth());
		double ySize = super.getSize().y - (super.getYPadding()<<1);
		ySize /= (super.getLocalHeight());

		double px, py;
		double px2, py2;

		for(int j=0; j<elementList.length; j++) {
			points = elementList[j].toArray(points);
			c = new Color(getDisplay(), IGraphColorConstants.COLORS[j]);
			gc.setForeground(c);

			px2 = 0;
			py2 = super.getSize().y - super.getYPadding();
			for(DataPoint point: points) {
				px = (point.x-super.getLocalXMin());
				px *= xSize;
				px += super.getXPadding();

				py = super.getLocalYMax() - point.y;
				py *= ySize;
				py += super.getYPadding();

				gc.drawLine((int)px, (int)py, (int)px2, (int)py2);
				px2 = px;
				py2 = py;
			}
		}

		gc.setForeground(temp);
	}

	@Override
	public boolean isMultiGraph() {
		return adapter.getSeriesCount() > 0;
	}

	/**
	 * Updates the graph when the <code>DataSet</code> has more data, adding the new samples to the graph.
	 */
	@Override
	public void handleUpdateEvent() {
		if(null == adapter) return;

		this.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Object[][] data = adapter.getData(removedItems, adapter.getRecordCount());
				if(normalize) {
					double max;
					for(int j,i=0; i<adapter.getSeriesCount(); i++) {
						elementList[i].clear();	//TODO: Only temparary
						max = adapter.getYSeriesMax(i, removedItems, adapter.getRecordCount()).doubleValue() / 100;
						for(j=0; j<data.length; j++) {
							elementList[i].add(new DataPoint(NumberType.obj2num(data[j][0]).doubleValue(),
				  					  					NumberType.obj2num(data[j][i+1]).doubleValue() / max));
						}
					}
				} else {
					for(int j,i=0; i<adapter.getSeriesCount(); i++) {
						elementList[i].clear();	//TODO: Only temparary
						for(j=0; j<data.length; j++) {
							elementList[i].add(new DataPoint(NumberType.obj2num(data[j][0]).doubleValue(),
				  					  					NumberType.obj2num(data[j][i+1]).doubleValue()));
						}
					}
				}
			}
		});
		this.repaint();
	}

	private ScrollAdapter adapter;
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.graphs.linegraph"; //$NON-NLS-1$
}