/*******************************************************************************
 * Copyright (c) 2014, 2019 Red Hat and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial implementation (pulled from other SWTBot tests in package)
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.swtbot;

import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractSWTBotTest {

    protected static SWTWorkbenchBot bot;
    protected static SWTBotTree projectExplorerViewTree;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // delay click speed
        //System.setProperty("org.eclipse.swtbot.playback.delay", "200");
        bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
            // hide Subclipse Usage stats popup if present/installed
            bot.shell("Subclipse Usage").activate();
            bot.button("Cancel").click();
        } catch (WidgetNotFoundException e) {
            // ignore
        }
        // Make sure project explorer is open and tree available
        ProjectExplorer.openView();
    }

}
