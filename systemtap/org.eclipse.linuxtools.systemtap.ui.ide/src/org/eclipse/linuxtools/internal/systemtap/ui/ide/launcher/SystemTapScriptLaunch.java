/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferrazzutti <aferrazz@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole.ScriptConsoleObserver;

public class SystemTapScriptLaunch extends Launch
    implements ScriptConsoleObserver {

    private ScriptConsole console = null;
    private boolean runStarted = false;
    private boolean runStopped = false;

    public SystemTapScriptLaunch(ILaunchConfiguration launchConfiguration, String mode) {
        super(launchConfiguration, mode, null);
    }

    public void setConsole(ScriptConsole console) {
        if (this.console == console) {
            return;
        }
        // If another launch is using the same console, remove that launch since
        // ScriptConsole prevents two identical stap scripts from being be run at once.
        this.console = console;
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        for (ILaunch launch : manager.getLaunches()) {
            if (launch.equals(this)) {
                continue;
            }
            if (launch instanceof SystemTapScriptLaunch) {
                SystemTapScriptLaunch olaunch = (SystemTapScriptLaunch) launch;
                if (olaunch.console == null || olaunch.console.equals(console)) {
                    olaunch.forceRemove();
                }
            }
        }
        console.addScriptConsoleObserver(this);
    }

    public ScriptConsole getConsole() {
        return console;
    }

    @Override
    public void runningStateChanged(boolean started, boolean stopped) {
        if (!runStarted && started) {
            runStarted = started;
            if (console.getCommand().getProcess() != null) {
                DebugPlugin.newProcess(this, console.getCommand().getProcess(), console.getName());
            }
            if (stopped) {
                runStopped = true;
            }
            console.removeScriptConsoleObserver(this);
        }
    }

    @Override
    public boolean canTerminate() {
        return !isTerminated();
    }

    @Override
    public boolean isTerminated() {
        if (!runStopped) {
            if (super.isTerminated() || (console != null && !console.isRunning())) {
                runStopped = true;
            }
        }
        return runStopped;
    }

    @Override
    public void terminate() {
        if (console != null) {
            console.stop();
        }
    }

    @Override
    public void launchRemoved(ILaunch launch) {
        super.launchRemoved(launch);
        if (launch.equals(this)) {
            removeConsole();
        }
    }

    private void removeConsole() {
        if (console != null) {
            console.removeScriptConsoleObserver(this);
            console = null;
        }
    }

    public void forceRemove() {
        removeConsole();
        runStopped = true;
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        manager.removeLaunch(this);
    }
}
