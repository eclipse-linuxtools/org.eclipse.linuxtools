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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

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

		IResource curStatFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(curStatData);
		IResource prevStatFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(prevStatData);

		// Inject our own selections into the context
		IEvaluationContext ctx = (IEvaluationContext) event.getApplicationContext();
		ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME,
				new StructuredSelection(new IResource [] {prevStatFile, curStatFile}));

		ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command cmd = cmdService.getCommand("org.eclipse.linuxtools.perf.CompareAction"); //$NON-NLS-1$
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
		}
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
