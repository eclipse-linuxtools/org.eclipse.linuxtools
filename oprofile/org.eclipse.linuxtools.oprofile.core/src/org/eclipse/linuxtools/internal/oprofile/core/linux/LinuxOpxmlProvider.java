/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.linux;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.oprofile.core.IOpxmlProvider;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlConstants;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata.ModelDataProcessor;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionsProcessor;

/**
 * A class which implements the IOpxmlProvider interface for running opxml.
 */
public class LinuxOpxmlProvider implements IOpxmlProvider {

    @Override
    public IRunnableWithProgress info(final OpInfo info) {
        return new OpInfoRunner(info);
    }

    //public because it is used in OpInfo.java:getInfo()
    public class OpInfoRunner implements IRunnableWithProgress {
        private boolean b;
        private final OpInfo info;

        public OpInfoRunner(OpInfo info) {
            this.info = info;
        }

        public boolean run0(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            run(monitor);
            return b;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            OpxmlRunner runner = new OpxmlRunner();
            String[] args = new String[] { OpxmlConstants.OPXML_INFO };
            b = runner.run(args, info);
        }
    }

    @Override
    public IRunnableWithProgress modelData(final String eventName, final String sessionName, final OpModelImage image) {
        return new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                OpxmlRunner runner = new OpxmlRunner();

                String[] args = new String[] { OpxmlConstants.OPXML_MODELDATA,
                        eventName, sessionName };

                ModelDataProcessor.CallData data = new ModelDataProcessor.CallData(
                        image);
                runner.run(args, data);
            }
        };
    }

    @Override
    public IRunnableWithProgress checkEvents(final int ctr, final String event, final int um, final int[] eventValid) {
        return new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) {
                OpxmlRunner runner = new OpxmlRunner();
                String[] args = new String[] { OpxmlConstants.CHECKEVENTS_TAG,
                        Integer.toString(ctr), event, Integer.toString(um) };

                runner.run(args, eventValid);
            }
        };
    }

    /**
     *  return list of session collected on this system as well as events under each of them.
     *  @since 3.0
     */
    @Override
    public IRunnableWithProgress sessions(final ArrayList<OpModelSession> sessionList) {
        return new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) {
                OpxmlRunner runner = new OpxmlRunner();
                String[] args = new String[] { OpxmlConstants.OPXML_SESSIONS, };

                SessionsProcessor.SessionInfo sinfo = new SessionsProcessor.SessionInfo(
                        sessionList);
                runner.run(args, sinfo);
            }
        };
    }
}
