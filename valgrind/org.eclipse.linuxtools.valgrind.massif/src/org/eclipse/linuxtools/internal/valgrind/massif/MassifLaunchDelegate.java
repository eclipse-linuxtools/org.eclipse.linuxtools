/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.massif;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;

public class MassifLaunchDelegate extends ValgrindLaunchConfigurationDelegate
implements IValgrindLaunchDelegate {
	protected static final String OUT_PREFIX = "massif_";	 //$NON-NLS-1$
	protected static final String OUT_FILE = OUT_PREFIX + "%p.txt"; //$NON-NLS-1$
	protected static final FileFilter MASSIF_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(OUT_PREFIX);
		}
	};

	protected MassifOutput output;

	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor)
	throws CoreException {
		MassifPlugin.getDefault().setConfig(config);
		MassifPlugin.getDefault().setSourceLocator(launch.getSourceLocator());
		try {
			monitor.beginTask(Messages.getString("MassifLaunchDelegate.Parsing_Massif_Output"), 3); //$NON-NLS-1$
			
			IPath outputPath = verifyOutputPath(config);
			File[] massifOutputs = outputPath.toFile().listFiles(MASSIF_FILTER);
			
			if (massifOutputs.length > 0) {
				parseOutput(massifOutputs, monitor);
			}
		} catch (IOException e) {
			e.printStackTrace();
			abort(Messages.getString("MassifLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
	}

	protected void parseOutput(File[] massifOutputs, IProgressMonitor monitor) throws IOException {
		output = new MassifOutput();
		for (File file : massifOutputs) {
			MassifParser parser = new MassifParser(file);
			output.putSnapshots(parser.getPid(), parser.getSnapshots());
		}
		monitor.worked(2);
		
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindToolView massifPart = view.getDynamicView();
		if (massifPart instanceof MassifViewPart) {
			((MassifViewPart) massifPart).setOutput(output);
			// initialize to first pid
			((MassifViewPart) massifPart).setPid(output.getPids()[0]);
		}
		monitor.worked(1);
	}

	@SuppressWarnings("unchecked")
	public String[] getCommandArray(ILaunchConfiguration config)
	throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();

		IPath outputPath = verifyOutputPath(config);
		opts.add(MassifCommandConstants.OPT_MASSIF_OUTFILE + EQUALS + outputPath.append(OUT_FILE).toOSString());

		opts.add(MassifCommandConstants.OPT_HEAP + EQUALS + (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAP, MassifLaunchConstants.DEFAULT_MASSIF_HEAP) ? YES : NO));
		opts.add(MassifCommandConstants.OPT_HEAPADMIN + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAPADMIN, MassifLaunchConstants.DEFAULT_MASSIF_HEAPADMIN));
		opts.add(MassifCommandConstants.OPT_STACKS + EQUALS + (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, MassifLaunchConstants.DEFAULT_MASSIF_STACKS) ? YES : NO));
		opts.add(MassifCommandConstants.OPT_DEPTH + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_DEPTH, MassifLaunchConstants.DEFAULT_MASSIF_DEPTH));
		List<String> allocFns = config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALLOCFN, MassifLaunchConstants.DEFAULT_MASSIF_ALLOCFN);
		for (String func : allocFns) {
			opts.add(MassifCommandConstants.OPT_ALLOCFN + EQUALS + func);
		}
		opts.add(MassifCommandConstants.OPT_THRESHOLD + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_THRESHOLD, MassifLaunchConstants.DEFAULT_MASSIF_THRESHOLD) / 10.0);
		opts.add(MassifCommandConstants.OPT_PEAKINACCURACY + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_PEAKINACCURACY, MassifLaunchConstants.DEFAULT_MASSIF_PEAKINACCURACY) / 10.0);
		opts.add(MassifCommandConstants.OPT_TIMEUNIT + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT, MassifLaunchConstants.DEFAULT_MASSIF_TIMEUNIT));
		opts.add(MassifCommandConstants.OPT_DETAILEDFREQ + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, MassifLaunchConstants.DEFAULT_MASSIF_DETAILEDFREQ));
		opts.add(MassifCommandConstants.OPT_MAXSNAPSHOTS + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_MAXSNAPSHOTS, MassifLaunchConstants.DEFAULT_MASSIF_MAXSNAPSHOTS));
		if (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_BOOL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_BOOL)) {
			opts.add(MassifCommandConstants.OPT_ALIGNMENT + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_VAL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_VAL));
		}

		return opts.toArray(new String[opts.size()]);
	}
}
