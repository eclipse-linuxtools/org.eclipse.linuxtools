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
	private String configName;
	private String binaryCommand;

	public DocWriter(String name, TextConsole console, String cName,
			String binaryCommand) {
		super(name);
		this.console = console;
		this.configName = cName;
		this.binaryCommand = binaryCommand;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		
		if (console == null)
			return Status.CANCEL_STATUS;
		
		IDocument doc = console.getDocument();
		
		if (binaryCommand.length() > 0)
			try {
				doc.replace(doc.getLength(), 0, 
					PluginConstants.NEW_LINE 
					+ PluginConstants.NEW_LINE +"-------------" //$NON-NLS-1$
					+ PluginConstants.NEW_LINE 
					+ "Configuration name:   "//$NON-NLS-1$ 
					+ configName + PluginConstants.NEW_LINE +
					"Binary arguments  :   "//$NON-NLS-1$ 
					+ binaryCommand + PluginConstants.NEW_LINE +
					"To change this command, check under the Binary " + //$NON-NLS-1$
					"Arguments tab for this configuration in " + //$NON-NLS-1$
					"Profile As --> Profile Configurations."//$NON-NLS-1$
					);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		else
			try {
				doc.replace(doc.getLength(), 0,
					PluginConstants.NEW_LINE +
					PluginConstants.NEW_LINE + "-------------" + //$NON-NLS-1$
					PluginConstants.NEW_LINE + 
					"Configuration name:   "//$NON-NLS-1$ 
					+ configName + PluginConstants.NEW_LINE +
					"No binary commands specified. To specify commands, check under the Binary Arguments tab for this configuration in Profile As --> Profile Configurations." + //$NON-NLS-1$
					PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		
		return Status.OK_STATUS;
	}
	
}
