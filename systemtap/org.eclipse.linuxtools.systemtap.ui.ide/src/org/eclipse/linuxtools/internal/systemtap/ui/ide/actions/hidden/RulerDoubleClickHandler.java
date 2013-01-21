/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat Inc. - extract it as separate class and decouple from the editor internals
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.linuxtools.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * The <code>RulerDoubleClickHandler</code> handles double click events on the
 * ruler for this text editor. It first checks to see if the user clicked on a
 * comment line, then if they clicked on a line that SystemTap can use as a probe
 * point, and if the line of code passes both checks, it dispatches an event to the
 * active STPEditor to insert a code block describing the line of code that the user
 * clicked on.
 *
 * The block of code is sent to the STPEditor only under the following circumstances:
 * <ul>
 * 	<li>The line of code is not blank</li>
 * 	<li>If the line of code contains a single-line comment, it must not be the only text on that line</li>
 * 	<li>The line of code must not fall within a multiline comment</li>
 * 	<li>The line of code must be a line that can be used by SystemTap,
 * 	determined by running the following:<br/>
 * 		<code>stap -p2 -e 'probe kernel.statement("*@filename:linenumber")'</code><br/>
 * 		If <code>stap</code> does not generate errors while running the test command, the
 * 		line is assumed valid.</li>
 * </ul>
 *
 * If all of the above are met, the active STPEditor listed in <code>IDESessionSettings</code>
 * is told to insert a template probe for this line of code using the <code>SimpleEditor.insertText</code> method.
 * If no the returned STPEditor reference is null, the code opens a new editor.
 *
 * @see org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor#insertText
 * @see org.eclipse.linuxtools.systemtap.ui.structures.runnable.Command
 * @see org.eclipse.swt.events.MouseListener
 */
public class RulerDoubleClickHandler extends MouseAdapter {

	private AbstractDecoratedTextEditor editor;
	public RulerDoubleClickHandler(AbstractDecoratedTextEditor editor) {
		this.editor = editor;
	}
	/**
	 * The doubleclick event handler method.
	 * @param	e	The <code>MouseEvent</code> that represents this doubleclick event.
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e)
	{
		editor.getSite().getShell().setCursor(editor.getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		int lineno = ((IVerticalRulerInfo)editor.getAdapter(IVerticalRulerInfo.class)).getLineOfLastMouseButtonActivity();
		 IDocument document = editor.getDocumentProvider().getDocument(
		            editor.getEditorInput());

		String s = document.get();
		String[] lines = s.split("\n"); //$NON-NLS-1$
		String line = lines[lineno].trim();
		boolean die = false;
		if("".equals(line))		//eat blank lines //$NON-NLS-1$
		   die = true;
		if(line.startsWith("#"))	//eat preprocessor directives //$NON-NLS-1$
			die = true;
		if(line.startsWith("//"))	//eat C99 comments //$NON-NLS-1$
			die = true;
		if(line.startsWith("/*") && !line.contains("*/") && !line.endsWith("*/"))	//try to eat single-line C comments //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			die = true;

		//gogo find comment segments
		try
		{
			ArrayList<Integer> commentChunks = new ArrayList<Integer>();
			char[] chars = s.toCharArray();
			int needle = 1;
			int offset = document.getLineOffset(lineno);
			while (needle < chars.length)
			{
				if(chars[needle-1] == '/' && chars[needle] == '*')
				{
					commentChunks.add(needle);
					while(needle < chars.length)
					{
						if(chars[needle-1] == '*' && chars[needle] == '/')
						{
							commentChunks.add(needle);
							needle++;
							break;
						}
						needle++;
					}
				}
				needle++;
			}
			for(int i=0, pair, start, end; i < commentChunks.size(); i++)
			{
				if(!(commentChunks.get(i).intValue() < offset))
				{
					pair = i - i%2;
					start = commentChunks.get(pair).intValue();
					end = commentChunks.get(pair+1).intValue();
					if(offset >= start && offset <= end)
						die=true;
				}
			}
		} catch (BadLocationException excp) {
			LogManager.logCritical("Exception mouseDoubleClick: " + excp.getMessage(), this); //$NON-NLS-1$
		}
		if(die) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					Localization.getString("CEditor.ProbeInsertFailed"),Localization.getString("CEditor.CanNotProbeLine")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			IEditorInput in = editor.getEditorInput();
			if(in instanceof FileStoreEditorInput) {
				FileStoreEditorInput input = (FileStoreEditorInput)in;

				IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
				String kernroot = p.getString(IDEPreferenceConstants.P_KERNEL_SOURCE);

				String filepath = input.getURI().getPath();
				String kernrelative = filepath.substring(kernroot.length()+1, filepath.length());
				StringBuffer sb = new StringBuffer();

				sb.append("probe kernel.statement(\"*@"+ kernrelative + ":" + (lineno+1) + "\")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					sb.append("\n{\n\t\n}\n"); //$NON-NLS-1$
					STPEditor activeSTPEditor = IDESessionSettings.getActiveSTPEditor();
					if(null == activeSTPEditor) {
						NewFileAction action = new NewFileAction();
						action.run();
						IEditorPart ed = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
						if(ed instanceof STPEditor)
							IDESessionSettings.setActiveSTPEditor((STPEditor)ed);
					}

					if(null != activeSTPEditor)
						activeSTPEditor.insertText(sb.toString());
			}
		}
		editor.getSite().getShell().setCursor(null);	//Return the cursor to normal
	}
}
