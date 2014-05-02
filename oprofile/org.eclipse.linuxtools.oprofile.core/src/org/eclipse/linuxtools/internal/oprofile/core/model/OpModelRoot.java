/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.model;

import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;

/**
 * A root node for the data model. Only one instance exists at any time,
 * although the contents will change. On instantiation the events and
 * sessions are gathered.
 *
 * Note that this data model does not map 1:1 to the oprofile data model.
 * This model is for use in profiling one application compiled with debug
 * info, from within eclipse.
 */

public class OpModelRoot {
    //single instance
    private static OpModelRoot modelRoot = new OpModelRoot();

    private OpModelSession[] session;

    protected OpModelRoot() {
        session = null;
    }

    public static OpModelRoot getDefault() {
        return modelRoot;
    }

    public void refreshModel() {
        //TODO-performance/interactivity: some persistence for events/sessions
        // that dont change from run to run (non default sessions)

        session = getNewSessions();
        if (session != null) {
            for (int i = 0; i < session.length; i++) {
                if (session[i] != null)
                    session[i].refreshModel();
            }
        }
    }

    /**
     * return list of session collected on this system as well as events under each of them.
     * @return collected sessions list
     * @since 3.0
     */
    protected OpModelSession[] getNewSessions() {
        //launch `opxml sessions`, gather up events & the sessions under them
        return Oprofile.getSessions();
    }


    public OpModelSession[] getSessions() {
        return session;
    }

    @Override
    public String toString() {
        String s = ""; //$NON-NLS-1$
        if (session != null) {
            for (int i = 0; i < session.length; i++) {
                if (session[i] != null) {
                    s += "Session: "; //$NON-NLS-1$
                    s += session[i].toString("\t"); //$NON-NLS-1$
                }
            }
        }
        return s;
    }
}
