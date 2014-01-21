package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole.ScriptConsoleObserver;
import org.eclipse.swt.widgets.Display;

public class SystemTapScriptLaunch extends Launch
	implements ScriptConsoleObserver {

	private ScriptConsole console = null;
	private boolean runStopped = false;

	public SystemTapScriptLaunch(ILaunchConfiguration launchConfiguration, String mode) {
		super(launchConfiguration, mode, null);
	}

	public void setConsole(ScriptConsole console) {
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
	public void runningStateChanged(boolean running) {
		DebugPlugin.newProcess(this, console.getCommand().getProcess(), console.getName());
		console.removeScriptConsoleObserver(this);
	}

	@Override
	public boolean canTerminate() {
		if (runStopped) {
			return false;
		}
		return console != null && !runStopped;
	}

	@Override
	public boolean isTerminated() {
		if (!runStopped) {
			if (super.isTerminated()) {
				runStopped = true;
			}
		}
		return runStopped;
	}

	@Override
	public void terminate() {
		if (console != null)
		{
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					console.stop();
				}
			});
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
		runStopped = true;
		removeConsole();
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		manager.removeLaunch(this);
	}
}
