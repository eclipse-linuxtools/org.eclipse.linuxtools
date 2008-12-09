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
package org.eclipse.linuxtools.valgrind.massif;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.ui.ValgrindViewPart;

public class MassifLaunchDelegate extends ValgrindLaunchConfigurationDelegate
implements IValgrindLaunchDelegate {
	public static final String TOOL_ID = ValgrindLaunchPlugin.PLUGIN_ID + ".massif"; //$NON-NLS-1$

	protected static final String OUT_PREFIX = "massif_";	 //$NON-NLS-1$
	protected static final String OUT_FILE = OUT_PREFIX + "%p.txt"; //$NON-NLS-1$
	protected static final FileFilter MASSIF_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(OUT_PREFIX);
		}
	};

	// Valgrind program arguments
	public static final String OPT_MASSIF_OUTFILE = "--massif-out-file"; //$NON-NLS-1$
	public static final String OPT_HEAP = "--heap"; //$NON-NLS-1$
	public static final String OPT_HEAPADMIN = "--heap-admin"; //$NON-NLS-1$
	public static final String OPT_STACKS = "--stacks"; //$NON-NLS-1$
	public static final String OPT_DEPTH = "--depth"; //$NON-NLS-1$
	public static final String OPT_ALLOCFN = "--alloc-fn"; //$NON-NLS-1$
	public static final String OPT_THRESHOLD = "--threshold"; //$NON-NLS-1$
	public static final String OPT_PEAKINACCURACY = "--peak-inaccuracy"; //$NON-NLS-1$
	public static final String OPT_TIMEUNIT = "--time-unit"; //$NON-NLS-1$
	public static final String OPT_DETAILEDFREQ = "--detailed-freq"; //$NON-NLS-1$
	public static final String OPT_MAXSNAPSHOTS = "--max-snapshots"; //$NON-NLS-1$
	public static final String OPT_ALIGNMENT = "--alignment"; //$NON-NLS-1$

	protected MassifSnapshot[] snapshots;

	public void launch(ValgrindCommand command, ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor)
	throws CoreException {
		MassifPlugin.getDefault().setConfig(config);
		MassifPlugin.getDefault().setSourceLocator(launch.getSourceLocator());
		try {
			command.getProcess().waitFor();

			File[] massifOutputs = command.getDatadir().listFiles(MASSIF_FILTER);

			parseOutput(massifOutputs);
		} catch (InterruptedException e) {
		} catch (IOException e) {
			e.printStackTrace();
			abort(Messages.getString("MassifLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}

	}

	protected void parseOutput(File[] massifOutputs) throws IOException {
		ArrayList<MassifSnapshot> list = new ArrayList<MassifSnapshot>();
		for (File output : massifOutputs) {
			MassifParser parser = new MassifParser(output);
			list.addAll(Arrays.asList(parser.getSnapshots()));
		}

		snapshots = list.toArray(new MassifSnapshot[list.size()]);
		
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindToolView massifPart = view.getDynamicView();
		if (massifPart instanceof MassifViewPart) {
			((MassifViewPart) massifPart).setSnapshots(snapshots);
		}
	}

	@SuppressWarnings("unchecked")
	public String[] getCommandArray(ValgrindCommand command, ILaunchConfiguration config)
	throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();

		try {
			opts.add(OPT_MASSIF_OUTFILE + EQUALS + command.getDatadir().getCanonicalPath() + File.separator + OUT_FILE);

			opts.add(OPT_HEAP + EQUALS + (config.getAttribute(MassifToolPage.ATTR_MASSIF_HEAP, true) ? YES : NO));
			opts.add(OPT_HEAPADMIN + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_HEAPADMIN, 8));
			opts.add(OPT_STACKS + EQUALS + (config.getAttribute(MassifToolPage.ATTR_MASSIF_STACKS, false) ? YES : NO));
			opts.add(OPT_DEPTH + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_DEPTH, 30));
			List<String> allocFns = config.getAttribute(MassifToolPage.ATTR_MASSIF_ALLOCFN, Collections.EMPTY_LIST);
			for (String func : allocFns) {
				opts.add(OPT_ALLOCFN + EQUALS + func);
			}
			opts.add(OPT_THRESHOLD + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_THRESHOLD, 10) / 10.0);
			opts.add(OPT_PEAKINACCURACY + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_PEAKINACCURACY, 10) / 10.0);
			opts.add(OPT_TIMEUNIT + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_TIMEUNIT, MassifToolPage.TIME_I));
			opts.add(OPT_DETAILEDFREQ + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_DETAILEDFREQ, 10));
			opts.add(OPT_MAXSNAPSHOTS + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_MAXSNAPSHOTS, 100));
			opts.add(OPT_ALIGNMENT + EQUALS + config.getAttribute(MassifToolPage.ATTR_MASSIF_ALIGNMENT, 8));
		} catch (IOException e) {
			abort(Messages.getString("MassifLaunchDelegate.Retrieving_massif_data_dir"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}

		return opts.toArray(new String[opts.size()]);
	}

//	public void restoreState(HistoryEntry entry) throws CoreException {
//		try {
//			Map<?, ?> config = entry.getAttributes();
//			MassifPlugin.getDefault().setConfig(config);
//			
//			// retrieve or create sourceLocator
//			ILaunch launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
//			setDefaultSourceLocator(launch, config);			
//			MassifPlugin.getDefault().setSourceLocator(launch.getSourceLocator());
//			
//			File[] massifOutputs = entry.getDatadir().listFiles(MASSIF_FILTER);
//			parseOutput(massifOutputs);
//		} catch (IOException e) {
//			e.printStackTrace();
//			abort(Messages.getString("MassifLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
//		}
//	}
//
//	public void saveState(HistoryEntry entry) throws CoreException {
//	}

}
