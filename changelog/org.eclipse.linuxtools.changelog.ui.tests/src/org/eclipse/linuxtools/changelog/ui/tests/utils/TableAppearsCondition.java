/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

/**
 * Condition for an appearing table (SWTBot tests).
 *
 */
public class TableAppearsCondition implements ICondition {

    private SWTBot bot;

    @Override
    public boolean test() {
        try {
            SWTBotTable table = bot.table();
            // table available
            // make sure rowcount > 0
            if (table.rowCount() > 0) {
                return true;
            }
        } catch (WidgetNotFoundException e) {
            // ignore
        }
        return false;
    }

    @Override
    public void init(SWTBot bot) {
        this.bot = bot;
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
