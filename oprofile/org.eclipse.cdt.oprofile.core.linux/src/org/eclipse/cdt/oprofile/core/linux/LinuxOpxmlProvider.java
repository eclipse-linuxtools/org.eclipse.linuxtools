/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.linux;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.IOpxmlProvider;
import org.eclipse.cdt.oprofile.core.OpInfo;
import org.eclipse.cdt.oprofile.core.ProfileImage;
import org.eclipse.cdt.oprofile.core.SampleSession;
import org.eclipse.cdt.oprofile.core.opxml.OpxmlConstants;
import org.eclipse.cdt.oprofile.core.opxml.SamplesProcessor;
import org.eclipse.cdt.oprofile.core.opxml.SessionsProcessor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * A class which implements the IOpxmlProvider interface for running opxml.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class LinuxOpxmlProvider implements IOpxmlProvider {
	
	public IRunnableWithProgress info(final OpInfo info) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				OpxmlRunner runner = new OpxmlRunner();
				String[] args = new String[] {
					OpxmlConstants.OPXML_INFO
				};
				runner.run(args, info);
			}
		};
		
		return runnable;
	}
	
	public IRunnableWithProgress samples(final SampleSession session) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {	
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				OpxmlRunner runner = new OpxmlRunner();
				String[] args = new String[] {
					OpxmlConstants.OPXML_SAMPLES,
					session.getEvent().getText(),
					session.getExecutableName() /* (session name) */
				};
				
				SamplesProcessor.CallData data = new SamplesProcessor.CallData(monitor, session);
				runner.run(args, data);
			}
		};
		
		return runnable;
	}
	
	public IRunnableWithProgress debugInfo(final ProfileImage image, final ArrayList infoList) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				OpxmlRunner runner = new OpxmlRunner();
				boolean ok = runner.run(new String[] { OpxmlConstants.OPXML_DEBUGINFO, image.getSampleFile() }, infoList);
			}
		};
		return runnable;
	}
	
	public IRunnableWithProgress checkEvents(final int ctr, final int event, final int um, final int[] eventValid) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				OpxmlRunner runner = new OpxmlRunner();
				String[] args = new String[] {
					OpxmlConstants.CHECKEVENTS_TAG,
					Integer.toString(ctr),
					Integer.toString(event),
					Integer.toString(um)
				};
				boolean ok = runner.run(args, eventValid);
			}
		};
		return runnable;
	}
	
	public IRunnableWithProgress sessions(final OpInfo info, final ArrayList sessionList) {
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				OpxmlRunner runner = new OpxmlRunner();
				String[] args = new String[] {
					OpxmlConstants.OPXML_SESSIONS,
				};
		
				SessionsProcessor.SessionInfo sinfo  = new SessionsProcessor.SessionInfo();
				 sinfo.info = info;
				 sinfo.list = sessionList;
				boolean ok = runner.run(args, sinfo);
			}
		};

		return runnable;
	}
}
