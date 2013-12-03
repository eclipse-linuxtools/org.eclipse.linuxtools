package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

/**
 * @since 3.0
 */
class BarChart extends Chart {

	private final int MIN_LABEL_SIZE = Messages.BarChartBuilder_LabelTrimTag.length();
	private final int fontSize;

	public boolean suspendUpdate = false;
	private String[] fullLabels = null;
	private IAxis xAxis = null;

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
	 * @param labels
	 */
	public void setCategorySeries(String[] labels) {
		xAxis.setCategorySeries(fullLabels = labels);
	}

	@Override
	public void updateLayout() {
		if (suspendUpdate) {
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
				// setCategorySeries triggers an unnecessary call to updateLayout, so prevent it.
				suspendUpdate = true;
				xAxis.setCategorySeries(labels);
				suspendUpdate = false;
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
