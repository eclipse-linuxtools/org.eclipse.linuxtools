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
package org.eclipse.linuxtools.dataviewers.charts.actions;

import org.eclipse.birt.chart.render.ActionRendererAdapter;
import org.eclipse.birt.chart.computation.DataPointHints;
import org.eclipse.birt.chart.event.StructureSource;
import org.eclipse.birt.chart.event.StructureType;
import org.eclipse.birt.chart.model.attribute.ActionType;
import org.eclipse.birt.chart.model.attribute.TooltipValue;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.data.Action;

import com.ibm.icu.util.ULocale;


/**
 * Simple implementation for IActionRenderer
 */
public class ChartActionRenderer extends ActionRendererAdapter {


 public void processAction( Action action, StructureSource source )
 {
	 if ( ActionType.SHOW_TOOLTIP_LITERAL.equals( action.getType( ) ) )
	 {
		 TooltipValue tv = (TooltipValue) action.getValue( );
		 if ( StructureType.SERIES_DATA_POINT.equals( source.getType( ) ) )
		 {
			 final DataPointHints dph = (DataPointHints) source.getSource( );
			 String MyToolTip = "Value is " + 
			 	JavaNumberFormatSpecifierImpl.create("0.00").format(
			 			((Double)dph.getOrthogonalValue()).doubleValue(),ULocale.getDefault());
			 tv.setText( MyToolTip );
		}
	 }
 	}
}
