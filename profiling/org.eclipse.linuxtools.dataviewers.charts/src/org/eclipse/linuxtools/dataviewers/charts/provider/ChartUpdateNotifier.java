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
package org.eclipse.linuxtools.dataviewers.charts.provider;

import org.eclipse.birt.chart.device.EmptyUpdateNotifier;
import org.eclipse.birt.chart.device.ICallBackNotifier;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.CallBackValue;
import org.eclipse.swt.widgets.Canvas;

/**
 *  @author Marzia Maugeri <marzia.maugeri@st.com>
 */

public class ChartUpdateNotifier extends EmptyUpdateNotifier implements ICallBackNotifier{
	private Canvas preview = null;
	
	public ChartUpdateNotifier(Canvas preview,Chart designModel, Chart runtimeModel) {
		super(designModel, runtimeModel);
		this.preview = preview;
	}
	
	public Object peerInstance( )
	{
		return preview;
	}

	@Override
	public void callback(Object event, Object source, CallBackValue value) {
	
	}

}
