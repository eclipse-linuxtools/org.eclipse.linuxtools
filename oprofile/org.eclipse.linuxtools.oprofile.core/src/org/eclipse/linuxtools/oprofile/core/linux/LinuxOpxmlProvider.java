/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.linux;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.oprofile.core.IOpxmlProvider;
import org.eclipse.linuxtools.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.opxml.OpxmlConstants;
import org.eclipse.linuxtools.oprofile.core.opxml.modeldata.ModelDataProcessor;
import org.eclipse.linuxtools.oprofile.core.opxml.sessions.SessionsProcessor;

/**
 * A class which implements the IOpxmlProvider interface for running opxml.
 */
public class LinuxOpxmlProvider implements IOpxmlProvider {
	private String _pathToOpxml;
	
	public LinuxOpxmlProvider(String pathToOpxml) {
		_pathToOpxml = pathToOpxml;
	}
	
	public IRunnableWithProgress info(final OpInfo info) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				OpxmlRunner runner = new OpxmlRunner(_pathToOpxml);
				String[] args = new String[] {
					OpxmlConstants.OPXML_INFO
				};
				runner.run(args, info);
			}
		};
		
		return runnable;
	}
	
	public IRunnableWithProgress modelData(final String eventName, final String sessionName, final OpModelImage image) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {	
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				OpxmlRunner runner = new OpxmlRunner(_pathToOpxml);

				String[] args = new String[] {
						OpxmlConstants.OPXML_MODELDATA,
						eventName,
						sessionName
				};
				
				ModelDataProcessor.CallData data = new ModelDataProcessor.CallData(image);
				runner.run(args, data);
			}
		};
		
		return runnable;
	}
		
	public IRunnableWithProgress checkEvents(final int ctr, final int event, final int um, final int[] eventValid) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				OpxmlRunner runner = new OpxmlRunner(_pathToOpxml);
				String[] args = new String[] {
					OpxmlConstants.CHECKEVENTS_TAG,
					Integer.toString(ctr),
					Integer.toString(event),
					Integer.toString(um)
				};
				
				runner.run(args, eventValid);
			}
		};
		return runnable;
	}
	
	public IRunnableWithProgress sessions(final ArrayList<OpModelEvent> sessionList) {
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				OpxmlRunner runner = new OpxmlRunner(_pathToOpxml);
				String[] args = new String[] {
					OpxmlConstants.OPXML_SESSIONS,
				};
		
				SessionsProcessor.SessionInfo sinfo  = new SessionsProcessor.SessionInfo(sessionList);
				runner.run(args, sinfo);
			}
		};

		return runnable;
	}
}
