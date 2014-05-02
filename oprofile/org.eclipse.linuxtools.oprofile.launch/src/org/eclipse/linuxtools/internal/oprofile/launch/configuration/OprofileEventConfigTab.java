/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *
 * CounterSubTab Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation (before subclassing)
 *    Kent Sebastian <ksebasti@redhat.com> - turned into a sub class,
 *          changed layouts, fixed up some interactivity issues, ..
 *
 * UnitMaskViewer Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;

/**
 * Thic class represents the event configuration tab of the launcher dialog.
 * It supplies all functionality dependent on opcontrol and having root access.
 */
public class OprofileEventConfigTab extends AbstractEventConfigTab {

    @Override
    protected boolean getOprofileTimerMode() {
        return Oprofile.getTimerMode();
    }

    @Override
    public OprofileCounter getOprofileCounter(int i) {
        return new OprofileCounter(i);
    }

    @Override
    protected OprofileCounter[] getOprofileCounters(ILaunchConfiguration config) {
        return OprofileCounter.getCounters(config);
    }

    @Override
    protected int getNumberOfOprofileCounters() {
        return Oprofile.getNumberOfCounters();
    }

    @Override
    protected IProject getOprofileProject() {
        return Oprofile.OprofileProject.getProject();
    }

    @Override
    protected void setOprofileProject(IProject project) {
        Oprofile.OprofileProject.setProject(project);
    }

    @Override
    protected void updateOprofileInfo() {
        Oprofile.updateInfo();
    }

    @Override
    protected boolean checkEventSetupValidity(int counter, String name,
            int maskValue) {
        return OprofileLaunchPlugin.getCache().checkEvent(counter, name, maskValue);
    }

}
