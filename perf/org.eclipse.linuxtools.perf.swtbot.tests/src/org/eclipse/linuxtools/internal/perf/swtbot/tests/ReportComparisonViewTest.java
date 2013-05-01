/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.swtbot.tests;

import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.ReportComparisonView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.Test;

/**
 * SWTBot test for ReportComparisonView.
 */
public class ReportComparisonViewTest extends AbstractStyledTextViewTest {

	@Override
	@Test
	public void runPerfViewTest() throws Exception {
		/*
		 * No need to create project or open launch dialog,
		 * just need to open view and run test.
		 */
		openStubView();
		testPerfView();
	}

	@Override
	protected String getViewId() {
		return "Perf Comparison";
	}

	@Override
	protected String getExpectedText() {
		return PerfPlugin.getDefault().getReportDiffData().getPerfData();
	}

	@Override
	protected void setPerfOptions(SWTWorkbenchBot bot) {
		// no perf options needed
	}

	@Override
	protected void openStubView() {
		PerfPlugin.getDefault().setReportDiffData(new StubPerfData());
		ReportComparisonView.refreshView();
	}

}
