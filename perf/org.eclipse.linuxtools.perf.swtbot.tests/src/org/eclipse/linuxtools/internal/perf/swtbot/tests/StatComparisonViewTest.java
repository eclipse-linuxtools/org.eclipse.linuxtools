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
import org.eclipse.linuxtools.internal.perf.ui.StatComparisonView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.Test;

/**
 * SWTBot test for StatComparisonView.
 */
public class StatComparisonViewTest extends AbstractStyledTextViewTest{

	@Override
	protected void setPerfOptions(SWTWorkbenchBot bot) {
		// no perf options needed
	}

	@Override
	@Test
	public void runPerfViewTest() {
		/*
		 * No need to create project or open launch dialog,
		 * just need to open view.
		 */
		openStubView();
		testPerfView();
	}

	@Override
	protected void openStubView() {
		PerfPlugin.getDefault().setStatDiffData(new StubPerfData());
		StatComparisonView.refreshView();
	}

	@Override
	protected String getViewId() {
		// supply secondary id
		return "Perf Statistics Comparison";
	}

	@Override
	protected String getExpectedText() {
		return PerfPlugin.getDefault().getStatDiffData().getPerfData();
	}


}
