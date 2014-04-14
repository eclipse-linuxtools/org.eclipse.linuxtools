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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.osgi.framework.Version;

public class MassifLaunchDelegate implements IValgrindLaunchDelegate {
	protected static final String OUT_PREFIX = "massif_";	 //$NON-NLS-1$
	protected static final String OUT_FILE = OUT_PREFIX + "%p.txt"; //$NON-NLS-1$
	private static final FileFilter MASSIF_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(OUT_PREFIX);
		}
	};
	
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String NO = "no"; //$NON-NLS-1$
	private static final String YES = "yes"; //$NON-NLS-1$
	private static final Version VER_3_6_0 = new Version(3, 6, 0);


	private MassifOutput output;

	@Override
	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IPath outDir, IProgressMonitor monitor)
	throws CoreException {
		MassifPlugin.getDefault().setSourceLocator(launch.getSourceLocator());
		try {
			monitor.beginTask(Messages.getString("MassifLaunchDelegate.Parsing_Massif_Output"), 3); //$NON-NLS-1$
			
			File[] massifOutputs = outDir.toFile().listFiles(MASSIF_FILTER);
			
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

	private void parseOutput(File[] massifOutputs, IProgressMonitor monitor) throws IOException {
		output = new MassifOutput();
		for (File file : massifOutputs) {
			MassifParser parser = new MassifParser(file);
			output.putSnapshots(parser.getPid(), parser.getSnapshots());
		}
		monitor.worked(2);
	}

	@Override
	public String[] getCommandArray(ILaunchConfiguration config, Version ver, IPath logDir)
	throws CoreException {
		ArrayList<String> opts = new ArrayList<>();

		opts.add(MassifCommandConstants.OPT_MASSIF_OUTFILE + EQUALS + logDir.append(OUT_FILE).toOSString());

		opts.add(MassifCommandConstants.OPT_HEAP + EQUALS + (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAP, MassifLaunchConstants.DEFAULT_MASSIF_HEAP) ? YES : NO));
		opts.add(MassifCommandConstants.OPT_HEAPADMIN + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAPADMIN, MassifLaunchConstants.DEFAULT_MASSIF_HEAPADMIN));
		opts.add(MassifCommandConstants.OPT_STACKS + EQUALS + (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, MassifLaunchConstants.DEFAULT_MASSIF_STACKS) ? YES : NO));
		opts.add(MassifCommandConstants.OPT_DEPTH + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_DEPTH, MassifLaunchConstants.DEFAULT_MASSIF_DEPTH));
		List<String> allocFns = config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALLOCFN, MassifLaunchConstants.DEFAULT_MASSIF_ALLOCFN);
		for (String func : allocFns) {
			opts.add(MassifCommandConstants.OPT_ALLOCFN + EQUALS + func);
		}
		List<String> ignoreFns = config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_IGNOREFN, MassifLaunchConstants.DEFAULT_MASSIF_IGNOREFN);
		for (String func : ignoreFns) {
			opts.add(MassifCommandConstants.OPT_IGNOREFN + EQUALS + func);
		}
		opts.add(MassifCommandConstants.OPT_THRESHOLD + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_THRESHOLD, MassifLaunchConstants.DEFAULT_MASSIF_THRESHOLD) / 10.0);
		opts.add(MassifCommandConstants.OPT_PEAKINACCURACY + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_PEAKINACCURACY, MassifLaunchConstants.DEFAULT_MASSIF_PEAKINACCURACY) / 10.0);
		opts.add(MassifCommandConstants.OPT_TIMEUNIT + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT, MassifLaunchConstants.DEFAULT_MASSIF_TIMEUNIT));
		opts.add(MassifCommandConstants.OPT_DETAILEDFREQ + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, MassifLaunchConstants.DEFAULT_MASSIF_DETAILEDFREQ));
		opts.add(MassifCommandConstants.OPT_MAXSNAPSHOTS + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_MAXSNAPSHOTS, MassifLaunchConstants.DEFAULT_MASSIF_MAXSNAPSHOTS));
		if (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_BOOL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_BOOL)) {
			opts.add(MassifCommandConstants.OPT_ALIGNMENT + EQUALS + config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_VAL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_VAL));
		}
		
		// VG >= 3.6.0
		if (ver == null || ver.compareTo(VER_3_6_0) >= 0) {
			opts.add(MassifCommandConstants.OPT_PAGESASHEAP + EQUALS + (config.getAttribute(MassifLaunchConstants.ATTR_MASSIF_PAGESASHEAP, MassifLaunchConstants.DEFAULT_MASSIF_PAGESASHEAP) ? YES : NO));
		}

		return opts.toArray(new String[opts.size()]);
	}
	
	@Override
	public void initializeView(IValgrindToolView view, String contentDescription, IProgressMonitor monitor) {
		if (output != null && view instanceof MassifViewPart) {
			((MassifViewPart) view).setChartName(contentDescription);
			((MassifViewPart) view).setOutput(output);
			// initialize to first pid
			((MassifViewPart) view).setPid(output.getPids()[0]);
		}
		monitor.worked(1);
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	private void abort(String message, Throwable exception, int code) throws CoreException {
		IStatus status;
		if (exception != null) {
			MultiStatus multiStatus = new MultiStatus(MassifPlugin.PLUGIN_ID, code, message, exception);
			multiStatus.add(new Status(IStatus.ERROR, MassifPlugin.PLUGIN_ID, code, exception.getLocalizedMessage(), exception));
			status= multiStatus;
		} else {
			status= new Status(IStatus.ERROR, MassifPlugin.PLUGIN_ID, code, message, null);
		}
		throw new CoreException(status);
	}
}
