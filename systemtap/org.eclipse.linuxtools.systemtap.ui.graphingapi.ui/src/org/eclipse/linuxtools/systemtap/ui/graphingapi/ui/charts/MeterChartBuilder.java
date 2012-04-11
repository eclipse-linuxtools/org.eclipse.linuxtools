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
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;

import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.DialChart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.LineDecorator;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.component.DialRegion;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.DialRegionImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.DialChartImpl;
import org.eclipse.birt.chart.model.type.DialSeries;
import org.eclipse.birt.chart.model.type.impl.DialSeriesImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.NumberType;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */
public class MeterChartBuilder extends AbstractChartWithoutAxisBuilder {
	protected IDeviceRenderer render = null;
    protected DialChart chart = null;
    protected AbstractChartBuilder builder = null;
    protected GeneratedChartState state = null;
 //   private boolean bFirstPaint = true;
    
    public MeterChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
    super(adapter, parent, style, title);
    this.parent = parent;
    
    try {
        PlatformConfig pf = new PlatformConfig();
       	ChartEngine ce = ChartEngine.instance(pf);
      	 render = ce.getRenderer("dv.SWT");
     } catch (Exception e) {
      e.printStackTrace();
     }
     this.addPaintListener(new painter());
    }
    
   private class  painter implements PaintListener {
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
    

    

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildChart()
     */
    protected void createChart() {
    	data = adapter.getData();
        while (data.length == 0 ) {   data = adapter.getData();}
    	IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
		xSeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_X_SERIES_TICKS);
		ySeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS);
		maxItems = store.getInt(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS);
		viewableItems = store.getInt(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS);

        chart = (DialChart) DialChartImpl.create();
        chart.setDialSuperimposition( false );
        chart.setType( "Meter Chart" );//$NON-NLS-1$
        chart.setSubType( "Superimposed Meter Chart" );//$NON-NLS-1$
        //chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildLegend()
     */
    protected void buildLegend() {
    	createLegend();
        chart.getLegend().setItemType(LegendItemType.CATEGORIES_LITERAL);
        chart.getLegend().getClientArea().getOutline( ).setVisible( true );
        chart.getLegend().getText().getFont().setSize(12);
        chart.getLegend().setVisible(false);
    }
    
    protected void buildTitle() {
        chart.getTitle().getLabel().getCaption().setValue(title);
        chart.getTitle().getLabel().getCaption().getFont().setSize(14);
        chart.getTitle().getLabel().getCaption().getFont().setName(FONT_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.chart.AbstractChartBuilder#buildXAxis()
     */
    protected void buildXAxis() {
    	//labels = adapter.getLabels();
        /*xAxis = ((ChartWithAxes) chart).getPrimaryBaseAxes()[0];
        xAxis.getTitle().setVisible(true);
        xAxis.getTitle().getCaption().setValue(labels[0]);
        xAxis.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxis.setType(AxisType.TEXT_LITERAL);
        xAxis.getOrigin().setType(IntersectionType.VALUE_LITERAL);*/
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.chart.AbstractChartBuilder#buildYAxis()
     */
    protected void buildYAxis() {
      /*  yAxis = ((ChartWithAxes) chart).getPrimaryOrthogonalAxis(xAxis);

        yAxis.getMajorGrid().getLineAttributes().setVisible(true);
       // yAxis.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl
         //       .GREY());
        yAxis.getMajorGrid().getLineAttributes()
                .setStyle(LineStyle.DASHED_LITERAL);
        yAxis.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);

        yAxis.setType(AxisType.LINEAR_LITERAL);
        yAxis.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        
        //yAxis.getScale().setStep(1.0);
        System.out.println("reached linechartbuilder" + "built yaxis");*/
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.chart.AbstractChartBuilder#buildXSeries()
     */
    protected void buildXSeries() {
    	
          data = adapter.getData();
          
          int starting = 0;
          if (data.length > xSeriesTicks ) starting = data.length - xSeriesTicks;
          x= new String[data.length - starting];
          
          try {
          for (int j=starting,k=0; j < data.length; j++,k++ )
           	x[k] = data[j][0].toString();
          } catch (Exception e)
          {
        	  e.printStackTrace();
          }
        TextDataSet categoryValues = TextDataSetImpl.create(x);
       // NumberDataSet categoryValues = NumberDataSetImpl.create(new double[] { 0, 1, 2, 3 });
       
        Series seCategory = SeriesImpl.create();
       
        seCategory.setDataSet(categoryValues);
       
        // Apply the color palette
        sdX = SeriesDefinitionImpl.create();
       
        //sdX.getSeriesPalette().update(1);
       
      //  xAxis.getSeriesDefinitions().add(sdX);
       
        sdX.getSeries().add(seCategory);
        chart.getSeriesDefinitions( ).add( sdX );
       }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildYSeries()
     */
    protected void buildYSeries() {
   try{
        int starting = 0;
        if (data.length > xSeriesTicks ) starting = data.length - xSeriesTicks;
     //   int yseriescount = adapter.getSeriesCount();
        NumberDataSet orthoValuesDataSet;
        DialSeries ds1;
        SeriesDefinition sdY; 
        	
        
          //for (int i =1; i<=yseriescount;i++)
        int i=0;
          y= new Double[data.length - starting];
           for (int j=starting,k=0; j < data.length; j++,k++ )
           {
           	y[k] = NumberType.obj2num(data[j][i]).doubleValue();
           	if ( max < y[k]) max = y[k];
           	if ( min > y[k]) min = y[k];
           }
           orthoValuesDataSet = NumberDataSetImpl.create(y);
           ds1= (DialSeries) DialSeriesImpl.create();
           ds1.setDataSet(orthoValuesDataSet);
          
          ds1.getLabel().setVisible(true);
          ds1.setLabelPosition(Position.INSIDE_LITERAL);
          
                 
            ds1.setSeriesIdentifier(labels[i]);
            //ds1.getDial( ).setFill( GradientImpl.create( ColorDefinitionImpl.create( 225,255,225 ),ColorDefinitionImpl.create( 225, 225, 255 ),45,false ) );
        	ds1.getDial( ).getMinorGrid( ).getTickAttributes( ).setVisible( true );
        	ds1.getDial( ).getMinorGrid( ).getTickAttributes( ).setColor( ColorDefinitionImpl.BLACK( ) );
        	ds1.getDial( ).getMinorGrid( ).setTickStyle( TickStyle.BELOW_LITERAL );
            ds1.getNeedle( ).setDecorator( LineDecorator.CIRCLE_LITERAL );
            sdY = SeriesDefinitionImpl.create();
             sdY.getSeries().add(ds1);
             sdX.getSeriesDefinitions().add(sdY);
             ds1.getLabel( )
				.setOutline( LineAttributesImpl.create( ColorDefinitionImpl.GREY( )
						.darker( ),
						LineStyle.SOLID_LITERAL,
						1 ) );
		    // ds1.getLabel( ).setBackground( ColorDefinitionImpl.GREY( ).brighter( ) );
		     DialRegion dregion1 = DialRegionImpl.create( );
				dregion1.setFill( ColorDefinitionImpl.GREEN( ) );
				dregion1.setOutline( LineAttributesImpl.create( ColorDefinitionImpl.BLACK( )
						.darker( ),
						LineStyle.SOLID_LITERAL,
						1 ) );
				dregion1.setStartValue( NumberDataElementImpl.create( 70 ) );
				dregion1.setEndValue( NumberDataElementImpl.create( 90 ) );
				dregion1.setInnerRadius( 40 );
				dregion1.setOuterRadius( -1 );
				ds1.getDial( ).getDialRegions( ).add( dregion1 );

				DialRegion dregion2 = DialRegionImpl.create( );
				dregion2.setFill( ColorDefinitionImpl.YELLOW( ) );
				dregion2.setOutline( LineAttributesImpl.create( ColorDefinitionImpl.BLACK( )
						.darker( ),
						LineStyle.SOLID_LITERAL,
						1 ) );
				dregion2.setStartValue( NumberDataElementImpl.create( 40 ) );
				dregion2.setEndValue( NumberDataElementImpl.create( 70 ) );
				dregion2.setOuterRadius( 70 );
				ds1.getDial( ).getDialRegions( ).add( dregion2 );

				DialRegion dregion3 = DialRegionImpl.create( );
				dregion3.setFill( ColorDefinitionImpl.RED( ) );
				dregion3.setOutline( LineAttributesImpl.create( ColorDefinitionImpl.BLACK( )
						.darker( ),
						LineStyle.SOLID_LITERAL,
						1 ) );
				dregion3.setStartValue( NumberDataElementImpl.create( 0 ) );
				dregion3.setEndValue( NumberDataElementImpl.create( 40 ) );
				dregion3.setInnerRadius( 40 );
				dregion3.setOuterRadius( 90 );
				ds1.getDial( ).getDialRegions( ).add( dregion3 );
				ds1.getLabel().getCaption().getFont().setSize(6);
        
        
   }catch (Exception e)
   {
	   e.printStackTrace();
   }
    }
    
    
    public void updateDataSet() {
        // Associate with Data Set
    	try{
    	   data = adapter.getData();
    	   int starting = 0;
           if (data.length > xSeriesTicks ) starting = data.length - xSeriesTicks; 
           
    	
           x= new String[data.length - starting];
          for (int j=starting,k =0; j < data.length; j++,k++ )
          	x[k] = data[j][0].toString();
          TextDataSet categoryValues = TextDataSetImpl.create(x);
          
          SeriesDefinition sd = (SeriesDefinition) chart.getSeriesDefinitions().get(0);
          ((Series) sd.getSeries().get(0)).setDataSet(categoryValues);

          int i=1;
       y= new Double[data.length - starting];
       for (int j=starting,k=0; j < data.length; j++,k++ )
       {
       	y[k] = NumberType.obj2num(data[j][i]).doubleValue();
       	if ( max < y[k]) max = y[k];
       }
       
       NumberDataSet orthoValuesDataSet1 = NumberDataSetImpl.create(y);
       SeriesDefinition sdY = (SeriesDefinition) sd.getSeriesDefinitions().get(0);
       ((Series) sdY.getSeries().get(0)).setDataSet(orthoValuesDataSet1);
    //   ((PieSeries) sdY.getSeries().get(0)).setExplosion(max.intValue());
       
       }catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }



public void handleUpdateEvent() {
	// TODO Auto-generated method stub
	
	try{
	updateDataSet();
	repaint();
	}catch(Exception e)
	{
		e.printStackTrace();
	}
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

protected void createLegend() {
	labels = adapter.getLabels();
	String[] labels2 = new String[labels.length-1];
//	Color[] colors = new Color[labels2.length];

	for(int i=0; i<labels2.length; i++) {
		labels2[i] = labels[i+1];
	//	colors[i] = new Color(this.getDisplay(), IGraphColorConstants.COLORS[i]);
	}
	
	//legend = new GraphLegend(this, labels2, colors);
}

String x[];
Double y[];
boolean fullUpdate;
Object data[][];
Composite parent = null;
String labels[];
SeriesDefinition sdX;
protected static int xSeriesTicks;
protected static int ySeriesTicks;
protected static int maxItems;
protected static int viewableItems;
public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.meterchartbuilder";
Double min = 0.0;
Double max = 0.0;

}
