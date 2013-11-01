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

import static org.junit.Assert.assertNotNull;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

/**
 * SWTBot test for ReportComparisonView.
 */
public class ReportComparisonViewTest extends AbstractSWTBotTest {

	@Override
	protected void setPerfOptions(SWTWorkbenchBot bot) {
		SWTBotCheckBox chkBox = bot.checkBox("Show Source Disassembly View");
		assertNotNull(chkBox);
		chkBox.select();
	}

	@Override
	protected void openStubView() {
	}

	@Override
	protected void testPerfView() {
		compareWithEachOther("perf_old.data", "perf_new.data");
	}

}
