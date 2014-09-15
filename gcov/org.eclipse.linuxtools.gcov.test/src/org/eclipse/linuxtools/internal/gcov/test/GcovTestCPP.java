/*******************************************************************************
 * Copyright (c) 2011 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GcovTestCPP {

    private static SWTWorkbenchBot bot;

    private static final String PROJECT_NAME = "Gcov_CPP_test";
    private static final String PROJECT_TYPE = "C++ Project";

    @BeforeClass
    public static void beforeClass() throws Exception {
        bot = GcovTest.init(PROJECT_NAME, PROJECT_TYPE);
    }

    @AfterClass
    public static void afterClass() {
        GcovTest.cleanup(bot);
    }

    @Test
    public void openGcovFileDetails() throws Exception {
        GcovTest.openGcovFileDetails(bot, PROJECT_NAME);
    }

    @Test
    public void openGcovSummary() throws Exception {
        GcovTest.openGcovSummary(bot, PROJECT_NAME, false);
    }

    @Test
    public void testGcovSummaryByLaunch() {
        GcovTest.openGcovSummaryByLaunch(bot, PROJECT_NAME);
    }
}
