/***********************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 * Elliott Baron <ebaron@redhat.com> - Modified implementation
 ***********************************************************************/
package org.eclipse.linuxtools.valgrind.massif.birt;

import org.eclipse.birt.chart.device.ICallBackNotifier;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.factory.RunTimeContext;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.CallBackValue;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public class ChartRenderer implements PaintListener, ControlListener, ICallBackNotifier {

	private transient Canvas canvas = null;

	private transient boolean bIsPainting = false;

	private transient Image buffer;

	private static int X_OFFSET = 3;

	private static int Y_OFFSET = 3;

	protected Chart cm = null;

	protected GeneratedChartState state = null;

	protected IDeviceRenderer deviceRenderer = null;

	private boolean needsGeneration = true;

	public ChartRenderer(Chart cm, Canvas canvas) throws ChartException {
		this.cm = cm;
		this.canvas = canvas;

		deviceRenderer = PluginSettings.instance().getDevice("dv.SWT"); //$NON-NLS-1$
		deviceRenderer.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);

		canvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				deviceRenderer.dispose();
			}				
		});
	}

	public void renderModel() {
		if (canvas != null && !canvas.isDisposed()) {
			updateBuffer();
			canvas.redraw();
		}
	}

	/**
	 * Generate and render the chart model,the chart image is stored in a
	 * buffer. The buffer will be updated only when the chart model is changed.
	 */
	private void updateBuffer() {
		if (bIsPainting) {
			return;
		}
		Throwable paintError = null;

		Rectangle re = canvas.getClientArea();
		final Rectangle adjustedRe = new Rectangle(0, 0, re.width, re.height);

		if (adjustedRe.width - 2 * X_OFFSET <= 0
				|| adjustedRe.height - 2 * Y_OFFSET <= 0) {
			if (buffer != null && !buffer.isDisposed()) {
				buffer.dispose();
				buffer = null;
			}
			return;
		}

		if (cm == null) {
			buffer = null;
		} else {
			bIsPainting = true;
			Image oldBuffer = null;
			if (buffer == null) {
				buffer = new Image(Display.getDefault(), adjustedRe);
			} else {
				Rectangle ore = buffer.getBounds();

				oldBuffer = buffer;

				if (!adjustedRe.equals(ore)) {
					buffer = new Image(Display.getDefault(), adjustedRe);
				}
			}

			GC gc = new GC(buffer);

			// fill default backgournd as white.
			gc.setForeground(Display.getDefault().getSystemColor(
					SWT.COLOR_WHITE));
			gc.fillRectangle(buffer.getBounds());

			Bounds bo = BoundsImpl.create(X_OFFSET, Y_OFFSET,
					adjustedRe.width - 2 * X_OFFSET, adjustedRe.height - 2
					* Y_OFFSET);

			try {
				deviceRenderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);

				bo.scale(72d / deviceRenderer.getDisplayServer()
						.getDpiResolution()); // convert to points

				// generate and render the cart
				Generator gr = Generator.instance();
				if (needsGeneration) {
					needsGeneration = false;
					RunTimeContext rtc = new RunTimeContext();

					state = gr.build(deviceRenderer
							.getDisplayServer(), cm, bo, null, rtc, null);

				}
				gr.render(deviceRenderer, state);
			} catch (Exception ex) {
				paintError = ex;
			} finally {
				gc.dispose();
			}

			if (paintError != null) {
				buffer = oldBuffer;
			}
			if (oldBuffer != null && oldBuffer != buffer) {
				oldBuffer.dispose();
			}
			bIsPainting = false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events
	 * .PaintEvent)
	 */
	public void paintControl(PaintEvent pe) {
		GC gc = pe.gc;
		if (buffer != null) {
			gc.drawImage(buffer, 0, 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events
	 * .ControlEvent)
	 */
	public void controlMoved(ControlEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt
	 * .events.ControlEvent)
	 */
	public void controlResized(ControlEvent e) {
		if (canvas != null && !canvas.isDisposed()) {
			updateBuffer();
			canvas.redraw();
		}
	}

	public void callback(Object event, Object source, CallBackValue value) {
		System.out.println("Event: " + event);
		System.out.println("Source: " + source);
		System.out.println();
	}

	public Chart getDesignTimeModel() {
		return cm;
	}

	public Chart getRunTimeModel() {
		return state.getChartModel();
	}

	public Object peerInstance() {
		return this;
	}

	public void regenerateChart() {
		needsGeneration = true;
		renderModel();
	}

	public void repaintChart() {
		renderModel();
	}
}
