package org.eclipse.linuxtools.systemtap.local.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.progress.UIJob;

public class DocWriter extends UIJob {
	private TextConsole console;
	private String message;

	/**
	 * Initiate DocWriter class. DocWriter will append the given message
	 * to the given console in a separate UI job. 
	 * 
	 * 
	 * @param name
	 * @param console
	 * @param message
	 */
	public DocWriter(String name, TextConsole console, String message) {
		
		super(name);
		this.console = console;
		this.message = message;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		
		if (console == null)
			return Status.CANCEL_STATUS;
		
		IDocument doc = console.getDocument();
		
		
		try {
			doc.replace(doc.getLength(), 0, message);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
//					try {
//				doc.replace(doc.getLength(), 0,
//					PluginConstants.NEW_LINE +
//					PluginConstants.NEW_LINE + "-------------" + //$NON-NLS-1$
//					PluginConstants.NEW_LINE + 
//					"Configuration name:   "//$NON-NLS-1$ 
//					+ configName + PluginConstants.NEW_LINE +
//					"No binary commands specified. To specify commands, check under the Binary Arguments tab for this configuration in Profile As --> Profile Configurations." + //$NON-NLS-1$
//					PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
//			} catch (BadLocationException e) {
//				e.printStackTrace();
//			}
		
		return Status.OK_STATUS;
	}
	
}
	