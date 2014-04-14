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
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.osgi.framework.Version;

public class CachegrindLaunchDelegate implements IValgrindLaunchDelegate {
	protected static final String OUT_PREFIX = "cachegrind_"; //$NON-NLS-1$
	protected static final String OUT_FILE = OUT_PREFIX + "%p.txt"; //$NON-NLS-1$
	protected static final FileFilter CACHEGRIND_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(OUT_PREFIX);
		}
	};
	
	private static final String COMMA = ","; //$NON-NLS-1$
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String NO = "no"; //$NON-NLS-1$
	private static final String YES = "yes"; //$NON-NLS-1$
	private CachegrindOutput[] outputs;
	
	@Override
	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IPath logDir, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(Messages.getString("CachegrindLaunchDelegate.Parsing_Cachegrind_Output"), 3); //$NON-NLS-1$
			
			File[] cachegrindOutputs = logDir.toFile().listFiles(CACHEGRIND_FILTER);
			
			if (cachegrindOutputs.length > 0) {
				parseOutput(cachegrindOutputs, monitor);
			}
		} catch (IOException e) {
			e.printStackTrace();
			abort(Messages.getString("CachegrindLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
	}

	private void parseOutput(File[] cachegrindOutputs, IProgressMonitor monitor) throws IOException {
		outputs = new CachegrindOutput[cachegrindOutputs.length];
		
		for (int i = 0; i < cachegrindOutputs.length; i++) {
			outputs[i] = new CachegrindOutput();
			CachegrindParser.getParser().parse(outputs[i], cachegrindOutputs[i]);
		}
		monitor.worked(2);
	}
	
	@Override
	public String[] getCommandArray(ILaunchConfiguration config, Version ver, IPath logDir) throws CoreException {
		ArrayList<String> opts = new ArrayList<>();
		
		opts.add(CachegrindCommandConstants.OPT_CACHEGRIND_OUTFILE + EQUALS + logDir.append(OUT_FILE).toOSString());
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
	
	@Override
	public void initializeView(IValgrindToolView view, String contentDescription, IProgressMonitor monitor)
			throws CoreException {
		if (outputs != null && view instanceof CachegrindViewPart) {
			((CachegrindViewPart) view).setOutputs(outputs);
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
			MultiStatus multiStatus = new MultiStatus(CachegrindPlugin.PLUGIN_ID, code, message, exception);
			multiStatus.add(new Status(IStatus.ERROR, CachegrindPlugin.PLUGIN_ID, code, exception.getLocalizedMessage(), exception));
			status= multiStatus;
		} else {
			status= new Status(IStatus.ERROR, CachegrindPlugin.PLUGIN_ID, code, message, null);
		}
		throw new CoreException(status);
	}

}
