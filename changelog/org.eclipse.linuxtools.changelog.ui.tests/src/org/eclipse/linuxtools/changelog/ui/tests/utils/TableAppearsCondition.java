/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
