/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.perf.ReportComparisonData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.ReportComparisonView;

/**
 * Handler for comparison between perf data files.
 */
public class PerfComparisonHandler extends AbstractComparisonHandler {

	@Override
	protected boolean isValidFile(IFile file) {
		if (file != null) {
			return PerfSaveSessionHandler.DATA_EXT.equals(file
					.getFileExtension())
					|| "old".equals(file.getFileExtension()); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	protected void handleComparison(IFile oldData, IFile newData) {
		String title = MessageFormat.format(Messages.ContentDescription_0,
				new Object[] { oldData.getName(), newData.getName() });

		// get corresponding files
		File oldDatum = oldData.getLocation().toFile();
		File newDatum = newData.getLocation().toFile();

		// create comparison data and run comparison.
		ReportComparisonData diffData = new ReportComparisonData(title, oldDatum, newDatum);
		diffData.parse();

		PerfPlugin.getDefault().setReportDiffData(diffData);
		ReportComparisonView.refreshView();
	}

}
