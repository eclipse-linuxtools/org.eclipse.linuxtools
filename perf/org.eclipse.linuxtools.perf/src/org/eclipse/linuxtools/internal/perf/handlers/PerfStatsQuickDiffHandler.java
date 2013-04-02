/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.ui.StatComparisonView;

/**
 * Command handler for quick comparison between current and previous sessions.
 */
public class PerfStatsQuickDiffHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) {

		// get default files
		PerfPlugin plugin = PerfPlugin.getDefault();
		File curStatData = plugin.getPerfFile(PerfPlugin.PERF_DEFAULT_STAT);
		File prevStatData = plugin.getPerfFile(PerfPlugin.PERF_DEAFULT_OLD_STAT);

		String title = MessageFormat.format(Messages.ContentDescription_0,
				new Object[] { prevStatData.getName(), curStatData.getName() });

		// create comparison data and run comparison
		StatComparisonData diffData = new StatComparisonData(title,
				prevStatData, curStatData);
		diffData.runComparison();

		// set comparison data and fill view
		plugin.setStatDiffData(diffData);
		StatComparisonView.refreshView();

		return null;
	}

	@Override
	public boolean isEnabled() {
		PerfPlugin plugin = PerfPlugin.getDefault();
		IPath workingDir = plugin.getWorkingDir();
		if (workingDir != null) {
			File curStatData = plugin.getPerfFile(PerfPlugin.PERF_DEFAULT_STAT);
			File prevStatData = plugin.getPerfFile(PerfPlugin.PERF_DEAFULT_OLD_STAT);
			return (curStatData.exists() && prevStatData.exists());
		}
		return false;
	}

	@Override
	public boolean isHandled() {
		return isEnabled();
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}
}
