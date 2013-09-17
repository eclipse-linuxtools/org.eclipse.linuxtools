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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.ui.StatComparisonView;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.swt.widgets.Display;

/**
 * Command handler for quick comparison between current and previous sessions.
 */
public class PerfStatsQuickDiffHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) {

		// get default files
		PerfPlugin plugin = PerfPlugin.getDefault();
		IPath curStatData = plugin.getPerfFile(PerfPlugin.PERF_DEFAULT_STAT);
		IPath prevStatData = plugin.getPerfFile(PerfPlugin.PERF_DEAFULT_OLD_STAT);

		String title = MessageFormat.format(Messages.ContentDescription_0,
				new Object[] { prevStatData.toOSString(), curStatData.toOSString() });

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
		URI curStatDataURI = null;
		URI prevStatDataURI = null;
		if (workingDir != null) {
			IPath curStatData = plugin.getPerfFile(PerfPlugin.PERF_DEFAULT_STAT);
			IPath prevStatData = plugin.getPerfFile(PerfPlugin.PERF_DEAFULT_OLD_STAT);
			IRemoteFileProxy proxy = null;
			try {
				curStatDataURI = new URI(curStatData.toPortableString());
				prevStatDataURI = new URI(prevStatData.toPortableString());
				proxy = RemoteProxyManager.getInstance().getFileProxy(curStatDataURI);
			} catch (URISyntaxException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
			} catch (CoreException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
			}
			IFileStore curFileStore = proxy.getResource(curStatDataURI.getPath());
			IFileStore prevFileStore = proxy.getResource(prevStatDataURI.getPath());
			return (curFileStore.fetchInfo().exists() && prevFileStore.fetchInfo().exists());
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
