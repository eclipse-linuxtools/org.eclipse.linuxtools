package org.eclipse.linuxtools.systemtap.localgui.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

public class ShellOpener extends UIJob{
	private Shell shell;

	public ShellOpener(String name, Shell sh) {
		super(name);
		shell = sh;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		shell.open();
		return Status.OK_STATUS;
	}
	
	public boolean isDisposed() {
		if (shell.isDisposed())
			return true;
		return false;
	}
	
}
