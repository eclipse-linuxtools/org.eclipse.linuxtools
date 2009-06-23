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

import org.eclipse.birt.chart.computation.DataPointHints;
import org.eclipse.birt.chart.device.ICallBackNotifier;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.event.WrappedStructureSource;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.factory.RunTimeContext;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.CallBackValue;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.events.MouseEvent;

public class ChartSVG implements ICallBackNotifier {

	protected Chart cm = null;

	protected GeneratedChartState state = null;

	protected IDeviceRenderer deviceRenderer = null;

	public ChartSVG(Chart chart) {
		cm = chart;
	}

	public void renderSVG(IPath svgPath) {
		try {
			RunTimeContext rtc = new RunTimeContext();
			
			deviceRenderer = PluginSettings.instance().getDevice("dv.SVG"); //$NON-NLS-1$
			Generator gr = Generator.instance();
			Bounds bo = BoundsImpl.create(0, 0, 800, 600);
			state = gr.build(deviceRenderer.getDisplayServer(), cm, bo, null,
					rtc, null);
			deviceRenderer.setProperty(IDeviceRenderer.FILE_IDENTIFIER, svgPath
					.toOSString());
			deviceRenderer.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);

			gr.render(deviceRenderer, state);
		} catch (ChartException e) {
			e.printStackTrace();
		}
	}

	public void callback(Object event, Object source, CallBackValue value) {
		// give Valgrind view focus
		ValgrindUIPlugin.getDefault().showView();
		MouseEvent mEvent = (MouseEvent) event;
		
		DataPointHints point = ((DataPointHints)((WrappedStructureSource)source).getSource());
		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		// select the corresponding snapshot in the TableViewer
		TableViewer viewer = view.getTableViewer();
		view.setTopControl(viewer.getControl());
		
		MassifSnapshot snapshot = (MassifSnapshot) viewer.getElementAt(point.getIndex());
		
		switch (mEvent.count) {
		case 1: // single click
			viewer.setSelection(new StructuredSelection(snapshot));
			break;
		case 2: // double click
//			if (snapshot.isDetailed()) {
//				ChartLocationsDialog dialog = new ChartLocationsDialog(getShell());
//				dialog.setInput(snapshot);
//				
//				if (dialog.open() == Window.OK) {
//					dialog.openEditorForResult();
//				}				
//			}
		}
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
	}

	public void repaintChart() {
	}
	
}
