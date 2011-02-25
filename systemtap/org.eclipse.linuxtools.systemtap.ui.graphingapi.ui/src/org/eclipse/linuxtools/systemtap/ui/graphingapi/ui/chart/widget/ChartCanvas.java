/****************************************************************
 * Licensed Material - Property of IBM
 *
 * ****-*** 
 *
 * (c) Copyright IBM Corp. 2006.  All rights reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.chart.widget;


import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;


import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.AbstractChartBuilder;

public class ChartCanvas extends Canvas{
    protected IDeviceRenderer render = null;
    protected Chart chart = null;
    protected AbstractChartBuilder builder = null;
    protected GeneratedChartState state = null;
 //   private boolean bFirstPaint = true;

    public ChartCanvas(Composite parent, int style) {
     super(parent, style | SWT.V_SCROLL | SWT.H_SCROLL);
     try {
     // INITIALIZE THE SWT RENDERING DEVICE
    	 //ChartEngine ce = ChartEngine.instance();
 //    PluginSettings ps = PluginSettings.instance();
     
     
     PlatformConfig pf = new PlatformConfig();

    	 //pf.setProperty(
    	 //"STANDALONE", true);

    	 // Returns a singleton instance of the Chart Engine

    	 ChartEngine ce = ChartEngine.instance(pf);

    	 // Returns a singleton instance of the Generator

    	// IGenerator gr = ce.getGenerator();

     
     try
     {
    	 render = ce.getRenderer("dv.SWT");
     }
     catch ( ChartException ex )
     {
     ex.printStackTrace( );
     } 
     //render = ps.getDevice("dv.PNG");
  
  } catch (Exception e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  

  addPaintListener(new PaintListener() {

    /**
     * The SWT paint callback
     */
    public void paintControl(PaintEvent pe)
    {
             if (chart == null) return;
       try{
       render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, pe.gc);
        Composite co = (Composite) pe.getSource();
        Rectangle re = co.getClientArea();
        Bounds bo = BoundsImpl.create(re.x, re.y, re.width, re.height);
        bo.scale(72d / render.getDisplayServer().getDpiResolution());
        Generator gr = Generator.instance();
       state = gr.build(render.getDisplayServer(),
           chart,
           bo,
           null,
           null,
           null);
   gr.render(render, state);
   } catch (ChartException e) {
   e.printStackTrace();
  }
     }
  }
  );
    }

 public Chart getChart() {
  return chart;
 }

 public void setChart(AbstractChartBuilder builder) {
  this.chart = builder.getChart();
  this.builder = builder;
  
  this.redraw();
 }

public void handleUpdateEvent() {
	// TODO Auto-generated method stub
	//builder.build();
	builder.updateDataSet();
	chart = builder.getChart();
	chart = null;
	this.redraw();
	this.update();
}

public synchronized void repaint() {
    getDisplay().syncExec(new Runnable() {
            boolean stop = false;
            public void run() {
                    if(stop) return;
                    try {
                            redraw();
                    } catch (Exception e) {
                            stop = true;
                    }
            }
    });
}


}

