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
package org.eclipse.linuxtools.valgrind.memcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.core.CommandLineConstants;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.ui.ValgrindViewPart;
import org.xml.sax.SAXException;

public class MemcheckLaunchDelegate extends ValgrindLaunchConfigurationDelegate implements IValgrindLaunchDelegate {

	protected ArrayList<ValgrindError> errors;

	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(Messages.getString("MemcheckLaunchDelegate.Parsing_Memcheck_Output"), 3); //$NON-NLS-1$

			IPath outputPath = verifyOutputPath(config);
			File[] logs = outputPath.toFile().listFiles(LOG_FILTER);
			parseOutput(logs, monitor);
		} catch (ParserConfigurationException e) {
			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (IOException e) {
			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (SAXException e) {
			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} finally {
			monitor.done();
		}

	}

	protected void parseOutput(File[] logs, IProgressMonitor monitor)
			throws ParserConfigurationException, IOException, CoreException,
			SAXException {
		errors = new ArrayList<ValgrindError>(logs.length);
		for (File log : logs) {
			ValgrindXMLParser parser;
			parser = new ValgrindXMLParser(new FileInputStream(log));

			errors.addAll(parser.getErrors());
		}

		monitor.worked(2);
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindToolView memcheckPart = view.getDynamicView();
		if (memcheckPart instanceof MemcheckViewPart) {
			((MemcheckViewPart) memcheckPart).setErrors(errors.toArray(new ValgrindError[errors.size()]));
		}
		monitor.worked(1);
	}
	
	public String[] getCommandArray(ILaunchConfiguration config) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();
		opts.add(CommandLineConstants.OPT_XML + EQUALS + YES);
		
		opts.add(MemcheckCommandConstants.OPT_LEAKCHECK + EQUALS + YES);
		opts.add(MemcheckCommandConstants.OPT_SHOWREACH + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, MemcheckLaunchConstants.DEFAULT_MEMCHECK_SHOWREACH) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_LEAKRES + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKRES));
		opts.add(MemcheckCommandConstants.OPT_FREELIST + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREELIST));
		opts.add(MemcheckCommandConstants.OPT_GCCWORK + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_GCCWORK) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_PARTIAL + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_PARTIAL) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_UNDEF + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_ALIGNMENT + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT));

		return opts.toArray(new String[opts.size()]);
	}

//	public void restoreState(HistoryEntry entry) throws CoreException {		
//		try {
//			File[] outputFiles = entry.getDatadir().listFiles(LOG_FILTER);
//			
//			parseOutput(outputFiles);
//		} catch (ParserConfigurationException e) {
//			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
//			e.printStackTrace();
//		} catch (IOException e) {
//			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
//			e.printStackTrace();
//		} catch (SAXException e) {
//			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
//			e.printStackTrace();
//		}
//	}
//
//	public void saveState(HistoryEntry entry) throws CoreException {
//	}

}
