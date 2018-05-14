/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.swtbot.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.SourceDisassemblyView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

/**
 * SWTBot test for SourceDisassemblyView.
 */
public class SourceDisassemblyViewTest extends AbstractStyledTextViewTest {

    @Override
    protected void setPerfOptions(SWTWorkbenchBot bot) {
        SWTBotCheckBox chkBox = bot.checkBox("Show Source Disassembly View");
        assertNotNull(chkBox);
        chkBox.select();
    }

    @Override
    protected void openStubView() {
        PerfPlugin.getDefault().setSourceDisassemblyData(new StubPerfData());
        SourceDisassemblyView.refreshView();

    }

    @Override
    protected String getViewId() {
        // supply secondary id
        return "Perf Source Disassembly";

    }

    @Override
    protected String getExpectedText() {
        return PerfPlugin.getDefault().getSourceDisassemblyData().getPerfData();
    }
}
