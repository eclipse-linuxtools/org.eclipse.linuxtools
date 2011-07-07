/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.viewer;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.dataviewers.charts.Activator;
import org.eclipse.linuxtools.dataviewers.charts.actions.ChartActionRenderer;
import org.eclipse.linuxtools.dataviewers.charts.provider.ChartUpdateNotifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;


/**
 * 
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 *
 */
public class ChartViewer implements PaintListener, ControlListener
{

	private transient Canvas preview = null;

	private Chart cm = null;

	private transient boolean bIsPainting = false;

	private transient Image buffer;

	private static int X_OFFSET = 3;

	private static int Y_OFFSET = 3;
	
	private GeneratedChartState gcs = null;
	
	private IDeviceRenderer deviceRenderer = null;
	
	private Bounds bo;

	public ChartViewer( )
	{
		try {
			deviceRenderer = PluginSettings.instance( ).getDevice( "dv.SWT" );
		} catch (ChartException e) {
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					e.getMessage(),
					e);
			Activator.getDefault().getLog().log(s);
		}
		
	}
	/**
	 * @param cm
	 *            Chart Model
	 */
	public void renderModel( Chart cm )
	{
		 this.cm = cm;

		if ( preview != null && !preview.isDisposed( ) )
		{
			updateBuffer( );
			preview.redraw( );
		}
	}
	
	public Bounds getBounds()
	{
		return bo;
	}
	
	public Chart getChart(){
		return cm;
	}

	/**
	 * Generate and render the chart model,the chart image is stored in a
	 * buffer. The buffer will be updated only when the chart model is changed.
	 */
	private void updateBuffer( )
	{
		if ( bIsPainting )
		{
			return;
		}
		Throwable paintError = null;

		Rectangle re = preview.getClientArea( );
		final Rectangle adjustedRe = new Rectangle( 0, 0, re.width, re.height );

		if ( adjustedRe.width - 2 * X_OFFSET <= 0
				|| adjustedRe.height - 2 * Y_OFFSET <= 0 )
		{
			if ( buffer != null && !buffer.isDisposed( ) )
			{
				buffer.dispose( );
				buffer = null;
			}
			return;
		}

		if ( cm == null )
		{
			buffer = null;
		}
		else
		{
			bIsPainting = true;
			Image oldBuffer = null;
			if ( buffer == null )
			{
				buffer = new Image( Display.getDefault( ), adjustedRe );
			}
			else
			{
				Rectangle ore = buffer.getBounds( );

				oldBuffer = buffer;

				if ( !adjustedRe.equals( ore ) )
				{
					buffer = new Image( Display.getDefault( ), adjustedRe );
				}
			}

			GC gc = new GC( buffer );

			// fill default background as white.
			gc.setForeground( Display.getDefault( )
					.getSystemColor( SWT.COLOR_WHITE ) );
			gc.fillRectangle( buffer.getBounds( ) );

			bo = BoundsImpl.create( X_OFFSET,
					Y_OFFSET,
					adjustedRe.width - 2 * X_OFFSET,
					adjustedRe.height - 2 * Y_OFFSET );


			try
			{
				deviceRenderer.setProperty( IDeviceRenderer.GRAPHICS_CONTEXT,gc );
				
				bo.scale( 72d / deviceRenderer.getDisplayServer( )
						.getDpiResolution( ) ); // CONVERT  TO POINTS

				// GENERATE AND RENDER THE CHART
				final Generator gr = Generator.instance( );
				
				gcs = gr.build(deviceRenderer.getDisplayServer( ), cm, bo, null, null, null);
				gcs.getRunTimeContext().setActionRenderer( new ChartActionRenderer());
				deviceRenderer.setProperty(IDeviceRenderer.UPDATE_NOTIFIER,
						new ChartUpdateNotifier(preview,cm, gcs.getChartModel()));
				gr.render(deviceRenderer, gcs);
			}
			catch ( Exception ex )
			{
				paintError = ex;
			}

			if ( paintError != null )
			{
				buffer = oldBuffer;
			}
			if ( oldBuffer != null && oldBuffer != buffer )
			{
				oldBuffer.dispose( );
			}
			bIsPainting = false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl( PaintEvent pe )
	{
		GC gc = pe.gc;
		if ( buffer != null )
		{
			gc.drawImage( buffer, 0, 0 );
		}
	}

	/**
	 * Set the preview canvas.
	 * 
	 * @param paintCanvas
	 */
	public void setViewer( Canvas paintCanvas )
	{
		this.preview = paintCanvas;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
	 */
	public void controlMoved( ControlEvent e )
	{
		if ( preview != null && !preview.isDisposed( ) )
		{
			updateBuffer( );
			preview.redraw( );
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
	 */
	public void controlResized( ControlEvent e )
	{
		if ( preview != null && !preview.isDisposed( ) )
		{
			updateBuffer( );
			preview.redraw( );
		}
	}
	
	public void setBuffer(Image buffer){
		this.buffer = buffer;
	}
		
}
