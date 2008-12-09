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
import org.xml.sax.SAXException;

public class MemcheckLaunchDelegate extends ValgrindLaunchConfigurationDelegate implements IValgrindLaunchDelegate {
	public static final String TOOL_ID = ValgrindLaunchPlugin.PLUGIN_ID + ".memcheck"; //$NON-NLS-1$
	
	// Valgrind program arguments
	public static final String OPT_LEAKCHECK = "--leak-check"; //$NON-NLS-1$
	public static final String OPT_SHOWREACH = "--show-reachable"; //$NON-NLS-1$
	public static final String OPT_LEAKRES = "--leak-resolution"; //$NON-NLS-1$
	public static final String OPT_FREELIST = "--freelist-vol"; //$NON-NLS-1$
	public static final String OPT_GCCWORK = "--workaround-gcc296-bugs"; //$NON-NLS-1$
	public static final String OPT_PARTIAL = "--partial-loads-ok"; //$NON-NLS-1$
	public static final String OPT_UNDEF = "--undef-value-errors"; //$NON-NLS-1$

	protected ArrayList<ValgrindError> errors;

	public void launch(ValgrindCommand command, ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// wait for Valgrind to exit
		try {
			command.getProcess().waitFor();

			File[] logs = command.getDatadir().listFiles(LOG_FILTER);
			parseOutput(logs);
		} catch (ParserConfigurationException e) {
			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (IOException e) {
			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (SAXException e) {
			abort(Messages.getString("MemcheckLaunchDelegate.Error_parsing_output"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (InterruptedException e) {
		}

	}

	protected void parseOutput(File[] logs)
			throws ParserConfigurationException, IOException, CoreException,
			SAXException {
		errors = new ArrayList<ValgrindError>(logs.length);
		for (File log : logs) {
			ValgrindXMLParser parser;
			parser = new ValgrindXMLParser(new FileInputStream(log));

			errors.addAll(parser.getErrors());
		}

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindToolView memcheckPart = view.getDynamicView();
		if (memcheckPart instanceof MemcheckViewPart) {
			((MemcheckViewPart) memcheckPart).setErrors(errors.toArray(new ValgrindError[errors.size()]));
		}
	}
	
	public String[] getCommandArray(ValgrindCommand command, ILaunchConfiguration config) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();
		opts.add(ValgrindCommand.OPT_XML + EQUALS + YES);
		
		opts.add(OPT_LEAKCHECK + EQUALS + YES);//config.getAttribute(ATTR_MEMCHECK_LEAKCHECK, "summary"));
		opts.add(OPT_SHOWREACH + EQUALS + (config.getAttribute(MemcheckToolPage.ATTR_MEMCHECK_SHOWREACH, false) ? YES : NO));
		opts.add(OPT_LEAKRES + EQUALS + config.getAttribute(MemcheckToolPage.ATTR_MEMCHECK_LEAKRES, "low")); //$NON-NLS-1$
		opts.add(OPT_FREELIST + EQUALS + config.getAttribute(MemcheckToolPage.ATTR_MEMCHECK_FREELIST, 10000000));
		opts.add(OPT_GCCWORK + EQUALS + (config.getAttribute(MemcheckToolPage.ATTR_MEMCHECK_GCCWORK, false) ? YES : NO));
		opts.add(OPT_PARTIAL + EQUALS + (config.getAttribute(MemcheckToolPage.ATTR_MEMCHECK_PARTIAL, false) ? YES : NO));
		opts.add(OPT_UNDEF + EQUALS + (config.getAttribute(MemcheckToolPage.ATTR_MEMCHECK_UNDEF, true) ? YES : NO));

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
