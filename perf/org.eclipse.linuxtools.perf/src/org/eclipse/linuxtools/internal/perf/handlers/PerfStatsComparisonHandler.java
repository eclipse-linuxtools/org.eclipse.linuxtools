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

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.ui.StatComparisonView;

/**
 * Command handler for comparing perf statistics files.
 */
public class PerfStatsComparisonHandler extends AbstractComparisonHandler {

	@Override
	protected boolean isValidFile(IFile file) {
		return file == null ? false : PerfSaveStatsHandler.DATA_EXT.equals(file
				.getFileExtension());
	}

	@Override
	protected void handleComparison(IFile oldData, IFile newData) {
		String title = MessageFormat.format(Messages.ContentDescription_0,
				new Object[] { oldData.getName(), newData.getName() });

		// get corresponding files
		IPath oldDatum = oldData.getFullPath();
		IPath newDatum = newData.getFullPath();

		// create comparison data and run comparison.
		StatComparisonData diffData = new StatComparisonData(title, oldDatum, newDatum);
		diffData.runComparison();

		PerfPlugin.getDefault().setStatDiffData(diffData);
		StatComparisonView.refreshView();
	}

}
