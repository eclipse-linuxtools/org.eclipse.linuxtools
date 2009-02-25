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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
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
	
	private static final String COMMA = ","; //$NON-NLS-1$
	
	public void handleLaunch(ILaunchConfiguration config,	ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(Messages.getString("CachegrindLaunchDelegate.Parsing_Cachegrind_Output"), 3); //$NON-NLS-1$
			
			IPath outputPath = verifyOutputPath(config);
			File[] cachegrindOutputs = outputPath.toFile().listFiles(CACHEGRIND_FILTER);
			parseOutput(cachegrindOutputs, monitor);
		} catch (IOException e) {
			e.printStackTrace();
			abort(Messages.getString("CachegrindLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
	}

	protected void parseOutput(File[] cachegrindOutputs, IProgressMonitor monitor) throws IOException {
		CachegrindOutput[] outputs = new CachegrindOutput[cachegrindOutputs.length];
		
		for (int i = 0; i < cachegrindOutputs.length; i++) {
			outputs[i] = new CachegrindOutput();
			CachegrindParser.getParser().parse(outputs[i], cachegrindOutputs[i]);
		}
		monitor.worked(2);
		
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindToolView cachegrindPart = view.getDynamicView();
		if (cachegrindPart instanceof CachegrindViewPart) {
			((CachegrindViewPart) cachegrindPart).setOutputs(outputs);
		}
		monitor.worked(1);
	}
	
	public String[] getCommandArray(ILaunchConfiguration config) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();
		
		IPath outputPath = verifyOutputPath(config);
		opts.add(CachegrindCommandConstants.OPT_CACHEGRIND_OUTFILE + EQUALS + outputPath.append(OUT_FILE).toOSString());
		opts.add(CachegrindCommandConstants.OPT_CACHE_SIM + EQUALS + (config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_CACHE_SIM, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_CACHE_SIM) ? YES : NO));
		opts.add(CachegrindCommandConstants.OPT_BRANCH_SIM + EQUALS + (config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_BRANCH_SIM, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_BRANCH_SIM) ? YES : NO));
		if (config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1)) {
			opts.add(CachegrindCommandConstants.OPT_I1 + EQUALS + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_SIZE)
					+ COMMA + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_ASSOC)
					+ COMMA + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_LSIZE));
		}
		if (config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1)) {
			opts.add(CachegrindCommandConstants.OPT_D1 + EQUALS + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_SIZE)
					+ COMMA + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_ASSOC)
					+ COMMA + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_LSIZE));
		}
		if (config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2)) {
			opts.add(CachegrindCommandConstants.OPT_L2 + EQUALS + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_SIZE)
					+ COMMA + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_ASSOC)
					+ COMMA + config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_LSIZE));
		}
		return opts.toArray(new String[opts.size()]);
	}

}
