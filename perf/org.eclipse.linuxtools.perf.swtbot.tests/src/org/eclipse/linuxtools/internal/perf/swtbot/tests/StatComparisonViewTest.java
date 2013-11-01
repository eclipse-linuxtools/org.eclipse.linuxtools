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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotSpinner;

/**
 * SWTBot test for StatComparisonView.
 */
public class StatComparisonViewTest extends AbstractSWTBotTest {

	@Override
	protected void setPerfOptions(SWTWorkbenchBot bot) {
		SWTBotCheckBox chkBox = bot.checkBox("Show Stat View");
		assertNotNull(chkBox);
		chkBox.select();

		SWTBotSpinner spinner = bot.spinner();
		assertNotNull(spinner);
		spinner.setSelection(3);
	}

	@Override
	protected void testPerfView() {
		compareWithEachOther("perf_old.stat", "perf_new.stat");
	}

	@Override
	protected void openStubView() {
	}

}
