package org.eclipse.linuxtools.valgrind.memcheck.tests;

import org.eclipse.linuxtools.valgrind.memcheck.MemcheckToolPage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Spinner;

public class MemcheckTestToolPage extends MemcheckToolPage {
	public Combo getLeakResCombo() {
		return leakResCombo;
	}


	public Button getShowReachableButton() {
		return showReachableButton;
	}


	public Spinner getFreelistSpinner() {
		return freelistSpinner;
	}


	public Button getPartialLoadsButton() {
		return partialLoadsButton;
	}


	public Button getUndefValueButton() {
		return undefValueButton;
	}


	public Button getGccWorkaroundButton() {
		return gccWorkaroundButton;
	}


	public Spinner getAlignmentSpinner() {
		return alignmentSpinner;
	}
}
