/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.cachegrind;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.ui.ValgrindViewPart;

public class CachegrindLaunchDelegate extends
		ValgrindLaunchConfigurationDelegate implements IValgrindLaunchDelegate {
	protected static final String OUT_PREFIX = "cachegrind_"; //$NON-NLS-1$
	protected static final String OUT_FILE = OUT_PREFIX + "%p.txt"; //$NON-NLS-1$
	protected static final FileFilter CACHEGRIND_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(OUT_PREFIX);
		}
	};
	
	// Valgrind program arguments
	public static final String OPT_CACHEGRIND_OUTFILE = "--cachegrind-out-file"; //$NON-NLS-1$
	
	public String[] getCommandArray(ValgrindCommand command,
			ILaunchConfiguration config) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();
		try {
			opts.add(OPT_CACHEGRIND_OUTFILE + EQUALS + command.getDatadir().getCanonicalPath() + File.separator + OUT_FILE);
		} catch (IOException e) {
			e.printStackTrace();
			abort(Messages.getString("CachegrindLaunchDelegate.Retrieving_cachegrind_data_dir_failed"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		return opts.toArray(new String[opts.size()]);
	}

	public void launch(ValgrindCommand command, ILaunchConfiguration config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			command.getProcess().waitFor();
			
			File[] cachegrindOutputs = command.getDatadir().listFiles(CACHEGRIND_FILTER);
			parseOutput(cachegrindOutputs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			abort(Messages.getString("CachegrindLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}	
	}

	protected void parseOutput(File[] cachegrindOutputs) throws IOException {
		CachegrindOutput[] outputs = new CachegrindOutput[cachegrindOutputs.length];
		
		for (int i = 0; i < cachegrindOutputs.length; i++) {
			outputs[i] = new CachegrindOutput();
			CachegrindParser.getParser().parse(outputs[i], cachegrindOutputs[i]);
		}
		
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindToolView cachegrindPart = view.getDynamicView();
		if (cachegrindPart instanceof CachegrindViewPart) {
			((CachegrindViewPart) cachegrindPart).setOutputs(outputs);
		}
	}

}
