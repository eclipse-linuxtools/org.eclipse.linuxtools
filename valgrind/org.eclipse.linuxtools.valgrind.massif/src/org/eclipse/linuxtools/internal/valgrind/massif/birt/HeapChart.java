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
package org.eclipse.linuxtools.internal.valgrind.massif.birt;

import org.eclipse.birt.chart.model.attribute.ActionType;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.TriggerCondition;
import org.eclipse.birt.chart.model.attribute.impl.CallBackValueImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.Trigger;
import org.eclipse.birt.chart.model.data.impl.ActionImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TriggerImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.TimeUnit;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class HeapChart extends ChartWithAxesImpl {

	private static String[] byteUnits = { Messages.getString("HeapChart.B"), //$NON-NLS-1$
			Messages.getString("HeapChart.KiB"), //$NON-NLS-1$
			Messages.getString("HeapChart.MiB"), //$NON-NLS-1$
			Messages.getString("HeapChart.GiB"), //$NON-NLS-1$
			Messages.getString("HeapChart.TiB") //$NON-NLS-1$
	};
	private static String[] instrUnits = { Messages.getString("HeapChart.i"), //$NON-NLS-1$
			Messages.getString("HeapChart.Ki"), //$NON-NLS-1$
			Messages.getString("HeapChart.Mi"), //$NON-NLS-1$
			Messages.getString("HeapChart.Gi"), //$NON-NLS-1$
			Messages.getString("HeapChart.Ti") //$NON-NLS-1$
	};
	private static String[] secondUnits = { Messages.getString("HeapChart.ms"), //$NON-NLS-1$
			Messages.getString("HeapChart.s") //$NON-NLS-1$
	};

	protected static final int BYTE_MULT = 1024;
	protected static final int BYTE_LIMIT = byteUnits.length - 1;
	protected static final int INSTR_MULT = 1000;
	protected static final int INSTR_LIMIT = instrUnits.length - 1;
	protected static final int MS_MULT = 1000;
	protected static final int MS_LIMIT = secondUnits.length - 1;

	protected static final int SCALING_THRESHOLD = 20;

	protected String xUnits;
	protected String yUnits;

	public HeapChart(MassifSnapshot[] snapshots) {
		TimeUnit timeUnit = snapshots[0].getUnit();
		long xScaling = getXScaling(snapshots, timeUnit);
		long yScaling = getYScaling(snapshots);

		double[] time = new double[snapshots.length];
		double[] dataUseful = new double[snapshots.length];
		double[] dataExtra = new double[snapshots.length];
		double[] dataStacks = null;

		boolean isStack = isStackProfiled(snapshots);
		if (isStack) {
			dataStacks = new double[snapshots.length];
		}
		double[] dataTotal = new double[snapshots.length];
		for (int i = 0; i < snapshots.length; i++) {
			time[i] = snapshots[i].getTime() / xScaling;
			dataUseful[i] = snapshots[i].getHeapBytes() / yScaling;
			dataExtra[i] = snapshots[i].getHeapExtra() / yScaling;
			dataTotal[i] = dataUseful[i] + dataExtra[i];
			if (isStack) {
				dataStacks[i] = snapshots[i].getStacks() / yScaling;
				dataStacks[i] += dataStacks[i];
			}
		}

		initialize();
		setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
		setType("Line Chart"); //$NON-NLS-1$
		setSubType("Overlay"); //$NON-NLS-1$

		Font font = JFaceResources.getDialogFont();
		FontData fd = font.getFontData()[0];

		// Title
		FontDefinition titleFont = getTitle().getLabel().getCaption().getFont();
		titleFont.setName(fd.getName());
		titleFont.setSize(fd.getHeight() + 2);

		// Plot
		getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = getPlot();
		p.getClientArea().setBackground(
				ColorDefinitionImpl.create(255, 255, 225));

		// X-Axis
		Axis xAxisPrimary = getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
		xAxisPrimary.getTitle().getCaption().setValue(xUnits);
		xAxisPrimary.getTitle().setVisible(true);

		FontDefinition xAxisFont = xAxisPrimary.getTitle().getCaption()
				.getFont();
		xAxisFont.setName(fd.getName());
		xAxisFont.setSize(fd.getHeight());

		xAxisFont = xAxisPrimary.getLabel().getCaption().getFont();
		xAxisFont.setName(fd.getName());
		xAxisFont.setSize(fd.getHeight());

		// Y-Axis
		Axis yAxisPrimary = getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		yAxisPrimary.getTitle().getCaption().setValue(yUnits);
		yAxisPrimary.getTitle().setVisible(true);

		FontDefinition yAxisFont = yAxisPrimary.getTitle().getCaption()
				.getFont();
		yAxisFont.setName(fd.getName());
		yAxisFont.setSize(fd.getHeight());

		yAxisFont = yAxisPrimary.getLabel().getCaption().getFont();
		yAxisFont.setName(fd.getName());
		yAxisFont.setSize(fd.getHeight());

		// // Z-Axis
		// Axis zAxis = AxisImpl.create(Axis.ANCILLARY_BASE);
		// zAxis.setType(AxisType.LINEAR_LITERAL);
		// zAxis.setLabelPosition(Position.BELOW_LITERAL);
		// zAxis.setTitlePosition(Position.BELOW_LITERAL);
		// zAxis.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		// zAxis.setOrientation(Orientation.HORIZONTAL_LITERAL);
		// xAxisPrimary.getAncillaryAxes().add(zAxis);

		// Legend
		Legend legend = getLegend();
		legend.setPosition(Position.BELOW_LITERAL);
		legend.setOrientation(Orientation.HORIZONTAL_LITERAL);

		FontDefinition legendFont = legend.getText().getFont();
		legendFont.setName(fd.getName());
		legendFont.setSize(fd.getHeight());

		// Data Set
		NumberDataSet mainValues = NumberDataSetImpl.create(time);
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(dataUseful);
		NumberDataSet orthoValues2 = NumberDataSetImpl.create(dataExtra);
		NumberDataSet orthoValuesS = null;
		if (isStack) {
			orthoValuesS = NumberDataSetImpl.create(dataStacks);
		}
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

		if (isStack) {
			OrthogonalSampleData sdOrthogonalS = DataFactory.eINSTANCE
					.createOrthogonalSampleData();
			sdOrthogonalS.setDataSetRepresentation("");//$NON-NLS-1$
			sdOrthogonalS.setSeriesDefinitionIndex(2);
			sd.getOrthogonalSampleData().add(sdOrthogonalS);
		}

		OrthogonalSampleData sdOrthogonal3 = DataFactory.eINSTANCE
				.createOrthogonalSampleData();
		sdOrthogonal3.setDataSetRepresentation("");//$NON-NLS-1$
		sdOrthogonal3.setSeriesDefinitionIndex(isStack ? 3 : 2);
		sd.getOrthogonalSampleData().add(sdOrthogonal3);

		setSampleData(sd);

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
		}

		ls1.setPaletteLineColor(true);
		ls1.setSeriesIdentifier(Messages.getString("HeapChart.Useful_Heap")); //$NON-NLS-1$	
		ls1.getTriggers().add(getClickTrigger(ls1));
		ls1.getTriggers().add(getDblClickTrigger(ls1));

		// Y-Series
		LineSeries ls2 = (LineSeries) LineSeriesImpl.create();
		ls2.setDataSet(orthoValues2);
		ls2.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls2.getMarkers().size(); i++) {
			Marker marker = (Marker) ls2.getMarkers().get(i);
			marker.setType(MarkerType.DIAMOND_LITERAL);
		}
		ls2.setPaletteLineColor(true);
		ls2.setSeriesIdentifier(Messages.getString("HeapChart.Extra_Heap")); //$NON-NLS-1$
		ls2.getTriggers().add(getClickTrigger(ls2));
		ls2.getTriggers().add(getDblClickTrigger(ls2));

		// Y-Series
		LineSeries lsS = null;
		if (isStack) {
			lsS = (LineSeries) LineSeriesImpl.create();
			lsS.setDataSet(orthoValuesS);
			lsS.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
			for (int i = 0; i < lsS.getMarkers().size(); i++) {
				Marker marker = (Marker) lsS.getMarkers().get(i);
				marker.setType(MarkerType.DIAMOND_LITERAL);
			}
			lsS.setPaletteLineColor(true);
			lsS.setSeriesIdentifier(Messages.getString("HeapChart.Stacks")); //$NON-NLS-1$
			lsS.getTriggers().add(getClickTrigger(lsS));
			lsS.getTriggers().add(getDblClickTrigger(lsS));
		}

		// Y-Series
		LineSeries ls3 = (LineSeries) LineSeriesImpl.create();
		ls3.setDataSet(orthoValues3);
		ls3.getLineAttributes().setColor(ColorDefinitionImpl.CREAM());
		for (int i = 0; i < ls3.getMarkers().size(); i++) {
			Marker marker = (Marker) ls3.getMarkers().get(i);
			marker.setType(MarkerType.DIAMOND_LITERAL);
		}
		ls3.setPaletteLineColor(true);
		ls3.setSeriesIdentifier(Messages.getString("HeapChart.Total_Heap")); //$NON-NLS-1$
		ls3.getTriggers().add(getClickTrigger(ls3));
		ls3.getTriggers().add(getDblClickTrigger(ls3));

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-1);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(ls1);
		sdY.getSeries().add(ls2);
		if (isStack) {
			sdY.getSeries().add(lsS);
		}
		sdY.getSeries().add(ls3);

		// // Z-Series
		// SeriesDefinition sdZ = SeriesDefinitionImpl.create();
		// zAxis.getSeriesDefinitions().add(sdZ);
	}

	private Trigger getClickTrigger(LineSeries ls) {
		return TriggerImpl.create(TriggerCondition.ONCLICK_LITERAL, ActionImpl
				.create(ActionType.CALL_BACK_LITERAL, CallBackValueImpl
						.create(String.valueOf(ls
								.getSeriesIdentifier()))));
	}
	
	private Trigger getDblClickTrigger(LineSeries ls) {
		return TriggerImpl.create(TriggerCondition.ONDBLCLICK_LITERAL, ActionImpl
				.create(ActionType.CALL_BACK_LITERAL, CallBackValueImpl
						.create(String.valueOf(ls
								.getSeriesIdentifier()))));
	}

	private boolean isStackProfiled(MassifSnapshot[] snapshots) {
		return getMaxStack(snapshots) > 0;
	}

	private long getYScaling(MassifSnapshot[] snapshots) {
		long max = getMaxValue(snapshots);

		int count = 0;
		while (max > BYTE_MULT * SCALING_THRESHOLD && count < BYTE_LIMIT) {
			max /= BYTE_MULT;
			count++;
		}

		yUnits = byteUnits[count];

		return (long) Math.pow(BYTE_MULT, count);
	}

	private long getXScaling(MassifSnapshot[] snapshots, TimeUnit unit) {
		long max = snapshots[snapshots.length - 1].getTime();
		int mult, limit;
		String[] units;
		switch (unit) {
		case BYTES:
			mult = BYTE_MULT;
			limit = BYTE_LIMIT;
			units = byteUnits;
			break;
		case INSTRUCTIONS:
			mult = INSTR_MULT;
			limit = INSTR_LIMIT;
			units = instrUnits;
			break;
		default:
			mult = MS_MULT;
			limit = MS_LIMIT;
			units = secondUnits;
			break;
		}

		int count = 0;
		while (max > mult * SCALING_THRESHOLD && count < limit) {
			max /= mult;
			count++;
		}

		xUnits = units[count];

		return (long) Math.pow(mult, count);
	}

	private static long getMaxValue(MassifSnapshot[] snapshots) {
		long max = 0;
		for (MassifSnapshot snapshot : snapshots) {
			if (snapshot.getTotal() > max) {
				max = snapshot.getTotal();
			}
		}
		return max;
	}

	private static long getMaxStack(MassifSnapshot[] snapshots) {
		long max = 0;
		for (MassifSnapshot snapshot : snapshots) {
			if (snapshot.getTotal() > max) {
				max = snapshot.getStacks();
			}
		}
		return max;
	}

	public String getXUnits() {
		return xUnits;
	}

	public String getYUnits() {
		return yUnits;
	}

	public static String[] getByteUnits() {
		return byteUnits;
	}

	public static String[] getInstrUnits() {
		return instrUnits;
	}

	public static String[] getSecondUnits() {
		return secondUnits;
	}

}
