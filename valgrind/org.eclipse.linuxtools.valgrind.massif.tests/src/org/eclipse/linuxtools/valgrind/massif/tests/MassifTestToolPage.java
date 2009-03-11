package org.eclipse.linuxtools.valgrind.massif.tests;

import org.eclipse.linuxtools.valgrind.massif.MassifToolPage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;

public class MassifTestToolPage extends MassifToolPage {

	public Button getHeapButton() {
		return heapButton;
	}

	public Spinner getHeapAdminSpinner() {
		return heapAdminSpinner;
	}

	public Button getStacksButton() {
		return stacksButton;
	}

	public Spinner getDepthSpinner() {
		return depthSpinner;
	}

	public List getAllocFnList() {
		return allocFnList;
	}

	public Spinner getThresholdSpinner() {
		return thresholdSpinner;
	}

	public Spinner getPeakInaccuracySpinner() {
		return peakInaccuracySpinner;
	}

	public Combo getTimeUnitCombo() {
		return timeUnitCombo;
	}

	public Spinner getDetailedFreqSpinner() {
		return detailedFreqSpinner;
	}

	public Spinner getMaxSnapshotsSpinner() {
		return maxSnapshotsSpinner;
	}

	public Spinner getAlignmentSpinner() {
		return alignmentSpinner;
	}

}
