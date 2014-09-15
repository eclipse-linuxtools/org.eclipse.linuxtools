/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial implementation (pulled from other SWTBot tests in package)
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.swtbot;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractSWTBotTest {

    private static final Logger fLogger = Logger.getRootLogger();
    protected static SWTWorkbenchBot bot;
    protected static SWTBotTree projectExplorerViewTree;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // delay click speed
        //System.setProperty("org.eclipse.swtbot.playback.delay", "200");
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
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
        projectExplorerViewTree = ProjectExplorer.getTree();
    }

}
