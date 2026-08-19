/*******************************************************************************
 * Copyright (c) 2014, 2024 Red Hat and others.
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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.GitCorePreferences;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit5.SWTBotJunit5Extension;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SWTBotJunit5Extension.class)
public abstract class AbstractSWTBotTest {

    protected static SWTWorkbenchBot bot;
    protected static SWTBotTree projectExplorerViewTree;

	@BeforeAll
	public static void beforeClass() {
        // EGit automatically shares every new project that is located inside
        // a git working tree, which the test workspace is when the build runs
        // from a git checkout. That would share "unshared" test projects and
        // map the GitTestProject to the enclosing repository, so turn it off
        // before any test project is created.
        InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID)
                .putBoolean(GitCorePreferences.core_autoShareProjects, false);
        // delay click speed
        //System.setProperty("org.eclipse.swtbot.playback.delay", "200");
        bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
        } catch (WidgetNotFoundException e) {
            // ignore
        }
        // Make sure project explorer is open and tree available
        ProjectExplorer.openView();
    }

}
