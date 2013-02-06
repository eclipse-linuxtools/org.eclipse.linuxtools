package org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

/**
 * Creates and displays an error message box that runs in the UI thread.
 *
 */
public class ErrorMessage {
	private String title;
	private String error;
	
	public ErrorMessage(String title, String error) {
		this.title = title;
		this.error = error;
	}
	
	public void open() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {  		
				MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setMessage(error);
				messageBox.setText(title);
				messageBox.open();
			} // end run	
		}); // end new Runnable
	}
}
