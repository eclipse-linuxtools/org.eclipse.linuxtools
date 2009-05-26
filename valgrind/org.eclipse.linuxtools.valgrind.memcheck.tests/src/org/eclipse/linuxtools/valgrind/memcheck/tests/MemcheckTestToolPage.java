/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
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
	
	public Button getTrackOriginsButton() {
		return trackOriginsButton;
	}
}
