package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

/**
 * @since 3.0
 */
class BarChart extends Chart {

	private final static int MIN_LABEL_SIZE = Messages.BarChartBuilder_LabelTrimTag.length();
	private final int fontSize;

	private String[] fullLabels = null;
	private IAxis xAxis = null;

	private boolean updateSuspended = false;
	public void suspendUpdate(boolean suspend) {
		if (updateSuspended == suspend) {
			return;
		}
		updateSuspended = suspend;

		// make sure that chart is updated
		if (!suspend) {
			updateLayout();
		}
	}
	public boolean isUpdateSuspended() {
		return updateSuspended;
	}

	public BarChart(Composite parent, int style) {
		super(parent, style);
		fontSize = getFont().getFontData()[0].getHeight();
		xAxis = getAxisSet().getXAxis(0);
		xAxis.enableCategory(true);
		xAxis.setCategorySeries(new String[]{""}); //$NON-NLS-1$
	}

	/**
	 * Sets the BarChart's x-axis category labels such that labels won't get
	 * cut off if there isn't enough room to display them fully. Use this
	 * instead of accessing the chart's x-axis and setting its category
	 * series directly.
	 * @param series
	 */
	public void setCategorySeries(String[] series) {
		xAxis.setCategorySeries(series);
		fullLabels = xAxis.getCategorySeries();
	}

	/**
	 * Returns a list of the full (non-trimmed) label names of each bar.
	 * Use this instead of accessing the x-axis' category series, which
	 * may contain trimmed label names.
	 */
	public String[] getCategorySeries() {
		String[] copiedCategorySeries = null;

		if (fullLabels != null) {
			copiedCategorySeries = new String[fullLabels.length];
			System.arraycopy(fullLabels, 0, copiedCategorySeries, 0,
					fullLabels.length);
		}

		return copiedCategorySeries;
	}

	@Override
	public void updateLayout() {
		if (isUpdateSuspended()) {
			return;
		}

		// If the x-axis and its labels are set, ensure that their contents fit the width of each label.
		if (fullLabels != null) {
			String[] labels = xAxis.getCategorySeries();
			if (labels != null && labels.length > 0) {
				String[] trimmedLabels = null;
				trimmedLabels = fitLabels(fullLabels);

				// Only update labels if their trimmed contents are different than their current contents.
				for (int i = 0; i < fullLabels.length; i++) {
					if (!trimmedLabels[i].equals(labels[i])) {
						labels = trimmedLabels;
						break;
					}
				}
				if (labels == trimmedLabels) {
					// setCategorySeries triggers an unnecessary call to updateLayout, so prevent it.
					updateSuspended = true;
					xAxis.setCategorySeries(labels);
					updateSuspended = false;
				}
			}
		}
		super.updateLayout();
	}

	/**
	 * Given an array of label names, return a new set of names that have been trimmed down
	 * in order to fit in the chart axis without getting cut off.
	 * @param labels An array of label names that may be trimmed.
	 * @return A new array containing label names that have been trimmed to fit in the axis display.
	 */
	private String[] fitLabels(String[] labels) {
		Range range = xAxis.getRange();
		int maxLabelSize = (int) Math.max(getClientArea().width / (Math.max(range.upper - range.lower, 1) * fontSize), MIN_LABEL_SIZE);

		String[] trimlabels = new String[labels.length];
		for (int i = 0; i < labels.length; i++) {
			if (labels[i].length() > maxLabelSize) {
				trimlabels[i] = labels[i].substring(0, maxLabelSize - MIN_LABEL_SIZE)
						.concat(Messages.BarChartBuilder_LabelTrimTag);
			} else {
				trimlabels[i] = labels[i];
			}
		}
		return trimlabels;
	}

}
