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

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.AxisImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot.TimeUnit;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class ChartBuilder {
	
	private static String[] byteUnits = { 
		Messages.getString("ChartBuilder.B"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.KiB"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.MiB"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.GiB"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.TiB") //$NON-NLS-1$
	};
	private static String[] instrUnits = {
		Messages.getString("ChartBuilder.i"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.Ki"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.Mi"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.Gi"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.Ti") //$NON-NLS-1$
	};
	private static String[] secondUnits = {
		Messages.getString("ChartBuilder.ms"), //$NON-NLS-1$
		Messages.getString("ChartBuilder.s") //$NON-NLS-1$
	};
	
	protected static final int BYTE_MULT = 1024;
	protected static final int BYTE_LIMIT = byteUnits.length - 1;
	protected static final int INSTR_MULT = 1000;
	protected static final int INSTR_LIMIT = instrUnits.length - 1;
	protected static final int MS_MULT = 1000;
	protected static final int MS_LIMIT = secondUnits.length - 1;
	
	protected static final int SCALING_THRESHOLD = 20;
		
	@SuppressWarnings("unchecked")
	public static final Chart createLine(MassifSnapshot[] snapshots) {
		TimeUnit timeUnit = snapshots[0].getUnit();
		int xScaling = getXScaling(snapshots, timeUnit);
		int xMultiplier =  getMultiplier(timeUnit);
		int yScaling = getYScaling(snapshots);
		
		int xDiv = (int) Math.pow(xMultiplier, xScaling);
		int yDiv = (int) Math.pow(BYTE_MULT, yScaling);
		
		double[] time = new double[snapshots.length];
		double[] dataUseful = new double[snapshots.length];
		double[] dataExtra = new double[snapshots.length];
		double[] dataTotal = new double[snapshots.length];
		for (int i = 0; i < snapshots.length; i++) {
			time[i] = snapshots[i].getTime() / xDiv;
			dataUseful[i] = snapshots[i].getHeapBytes() / yDiv;
			dataExtra[i] = snapshots[i].getHeapExtra() / yDiv;
			dataTotal[i] = dataUseful[i] + dataExtra[i];
		}

		ChartWithAxes cwaLine = ChartWithAxesImpl.create();
		cwaLine.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
		cwaLine.setType("Line Chart"); //$NON-NLS-1$
		cwaLine.setSubType("Overlay"); //$NON-NLS-1$

		Font font = JFaceResources.getDialogFont();
		FontData fd = font.getFontData()[0];
		
		// Title
		FontDefinition titleFont = cwaLine.getTitle().getLabel().getCaption().getFont();
		titleFont.setName(fd.getName());
		titleFont.setSize(fd.getHeight() + 2);
		
		// Plot
		cwaLine.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = cwaLine.getPlot();
		p.getClientArea().setBackground(
				ColorDefinitionImpl.create(255, 255, 225));

		// X-Axis
		Axis xAxisPrimary = cwaLine.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
		xAxisPrimary.getTitle().getCaption().setValue(getScaledUnit(timeUnit, xScaling));
		xAxisPrimary.getTitle().setVisible(true);
		
		FontDefinition xAxisFont = xAxisPrimary.getTitle().getCaption().getFont();
		xAxisFont.setName(fd.getName());
		xAxisFont.setSize(fd.getHeight());
		
		xAxisFont = xAxisPrimary.getLabel().getCaption().getFont();
		xAxisFont.setName(fd.getName());
		xAxisFont.setSize(fd.getHeight());
		
		// Y-Axis
		Axis yAxisPrimary = cwaLine.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		yAxisPrimary.getTitle().getCaption().setValue(getScaledUnit(TimeUnit.BYTES, yScaling));
		yAxisPrimary.getTitle().setVisible(true);
		
		FontDefinition yAxisFont = yAxisPrimary.getTitle().getCaption().getFont();
		yAxisFont.setName(fd.getName());
		yAxisFont.setSize(fd.getHeight());

		yAxisFont = yAxisPrimary.getLabel().getCaption().getFont();
		yAxisFont.setName(fd.getName());
		yAxisFont.setSize(fd.getHeight());
		
		// Z-Axis
		Axis zAxis = AxisImpl.create(Axis.ANCILLARY_BASE);
		zAxis.setType(AxisType.LINEAR_LITERAL);
		zAxis.setLabelPosition(Position.BELOW_LITERAL);
		zAxis.setTitlePosition(Position.BELOW_LITERAL);
		zAxis.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		zAxis.setOrientation(Orientation.HORIZONTAL_LITERAL);
		xAxisPrimary.getAncillaryAxes().add(zAxis);

		// Legend
		Legend legend = cwaLine.getLegend();
		legend.setPosition(Position.BELOW_LITERAL);
		legend.setOrientation(Orientation.HORIZONTAL_LITERAL);
		
		FontDefinition legendFont = legend.getText().getFont();
		legendFont.setName(fd.getName());
		legendFont.setSize(fd.getHeight());
		
		// Data Set
		NumberDataSet mainValues = NumberDataSetImpl.create(time);
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(dataUseful);
		NumberDataSet orthoValues2 = NumberDataSetImpl.create(dataExtra);
		NumberDataSet orthoValues3 = NumberDataSetImpl.create(dataTotal);

		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");//$NON-NLS-1$
		sd.getBaseSampleData().add(sdBase);

		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE
		.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);

		OrthogonalSampleData sdOrthogonal2 = DataFactory.eINSTANCE
		.createOrthogonalSampleData();
		sdOrthogonal2.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal2.setSeriesDefinitionIndex(1);
		sd.getOrthogonalSampleData().add(sdOrthogonal2);

		OrthogonalSampleData sdOrthogonal3 = DataFactory.eINSTANCE
		.createOrthogonalSampleData();
		sdOrthogonal3.setDataSetRepresentation("Total Heap");//$NON-NLS-1$
		sdOrthogonal3.setSeriesDefinitionIndex(2);
		sd.getOrthogonalSampleData().add(sdOrthogonal3);

		cwaLine.setSampleData(sd);

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(mainValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		// Y-Series
		LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
		ls1.setDataSet(orthoValues1);
		ls1.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls1.getMarkers().size(); i++) {
			Marker marker = (Marker) ls1.getMarkers().get(i);
			marker.setType(MarkerType.DIAMOND_LITERAL);
			marker.setSize(3);
		}
		ls1.setPaletteLineColor(true);
		ls1.setSeriesIdentifier(Messages.getString("ChartBuilder.Useful_Heap")); //$NON-NLS-1$

		// Y-Series
		LineSeries ls2 = (LineSeries) LineSeriesImpl.create();
		ls2.setDataSet(orthoValues2);
		ls2.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls2.getMarkers().size(); i++) {
			Marker marker = (Marker) ls2.getMarkers().get(i);
			marker.setType(MarkerType.DIAMOND_LITERAL);
			marker.setSize(3);
		}
		ls2.setPaletteLineColor(true);
		ls2.setSeriesIdentifier(Messages.getString("ChartBuilder.Extra_Heap")); //$NON-NLS-1$
		
		// Y-Series
		LineSeries ls3 = (LineSeries) LineSeriesImpl.create();
		ls3.setDataSet(orthoValues3);
		ls3.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls3.getMarkers().size(); i++) {
			Marker marker = (Marker) ls3.getMarkers().get(i);
			marker.setType(MarkerType.DIAMOND_LITERAL);
			marker.setSize(3);
		}
		ls3.setPaletteLineColor(true);
		ls3.setSeriesIdentifier(Messages.getString("ChartBuilder.Total_Heap")); //$NON-NLS-1$

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-1);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(ls1);
		sdY.getSeries().add(ls2);
		sdY.getSeries().add(ls3);
		
		// Z-Series
		SeriesDefinition sdZ = SeriesDefinitionImpl.create();
		zAxis.getSeriesDefinitions().add(sdZ);

		//		// Rotate the chart
		//		cwa3DLine.setRotation(Rotation3DImpl.create(new Angle3D[] { Angle3DImpl
		//				.create(-10, 25, 0) }));

		return cwaLine;
	}

	private static String getScaledUnit(TimeUnit unit, int scaling) {
		String name;
		int ix = scaling;
		switch (unit) {
		case BYTES:
			name = byteUnits[ix];
			break;
		case INSTRUCTIONS:
			name = instrUnits[ix];
			break;
		default:
			name = secondUnits[ix];
			break;
		}
		return name;
	}

	private static int getMultiplier(TimeUnit unit) {
		int mult;
		switch (unit) {
		case BYTES:
			mult = BYTE_MULT;
			break;
		case INSTRUCTIONS:
			mult = INSTR_MULT;
			break;
		default:
			mult = MS_MULT;
			break;
		}
		return mult;
	}

	private static int getYScaling(MassifSnapshot[] snapshots) {
		int max = getMaxValue(snapshots);
		
		int count = 0;
		while (max > BYTE_MULT * SCALING_THRESHOLD && count < BYTE_LIMIT) {
			max /= BYTE_MULT;
			count++;
		}
		
		return count;
	}

	private static int getXScaling(MassifSnapshot[] snapshots, TimeUnit unit) {
		int max = snapshots[snapshots.length - 1].getTime();
		int mult, limit;
		switch (unit) {
		case BYTES:
			mult = BYTE_MULT;
			limit = BYTE_LIMIT;
			break;
		case INSTRUCTIONS:
			mult = INSTR_MULT;
			limit = INSTR_LIMIT;
			break;
		default:
			mult = MS_MULT;
			limit = MS_LIMIT;
			break;
		}
		
		int count = 0;
		while (max > mult * SCALING_THRESHOLD && count < limit) {
			max /= mult;
			count++;
		}
		
		return count;
	}

	private static int getMaxValue(MassifSnapshot[] snapshots) {
		int max = 0;
		for (MassifSnapshot snapshot : snapshots) {
			if (snapshot.getTotal() > max) {
				max = snapshot.getTotal();
			}
		}
		return max;
	}

}
