/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.linuxtools.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class AddStapProbe extends AbstractRulerActionDelegate {

	@Override
	protected IAction createAction(final ITextEditor editor,
			final IVerticalRulerInfo rulerInfo) {
		return new Action() {

			@Override
			public void runWithEvent(Event event) {
				Shell shell = editor.getSite().getShell();
				shell.setCursor(shell.getDisplay().getSystemCursor(
						SWT.CURSOR_WAIT));
				int lineno = rulerInfo.getLineOfLastMouseButtonActivity();
				IDocument document = editor.getDocumentProvider().getDocument(
						editor.getEditorInput());

				String s = document.get();
				String[] lines = s.split("\n"); //$NON-NLS-1$
				String line = lines[lineno].trim();
				boolean die = false;
				if (line.isEmpty()) {//eat blank lines
					die = true;
				}
				if (line.startsWith("#")) {//eat preprocessor directives //$NON-NLS-1$
					die = true;
				}
				if (line.startsWith("//")) {//eat C99 comments //$NON-NLS-1$
					die = true;
				}
				if (line.startsWith("/*") && !line.contains("*/") && !line.endsWith("*/")) {//try to eat single-line C comments //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					die = true;
				}

				// gogo find comment segments
				try {
					ArrayList<Integer> commentChunks = new ArrayList<Integer>();
					char[] chars = s.toCharArray();
					int needle = 1;
					int offset = document.getLineOffset(lineno);
					while (needle < chars.length) {
						if (chars[needle - 1] == '/' && chars[needle] == '*') {
							commentChunks.add(needle);
							while (needle < chars.length) {
								if (chars[needle - 1] == '*'
										&& chars[needle] == '/') {
									commentChunks.add(needle);
									needle++;
									break;
								}
								needle++;
							}
						}
						needle++;
					}
					for (int i = 0, pair, start, end; i < commentChunks.size(); i++) {
						if (!(commentChunks.get(i).intValue() < offset)) {
							pair = i - i % 2;
							start = commentChunks.get(pair).intValue();
							end = commentChunks.get(pair + 1).intValue();
							if (offset >= start && offset <= end) {
								die = true;
							}
						}
					}
				} catch (BadLocationException excp) {
					ExceptionErrorDialog.openError(Localization.getString("AddStapProbe.UnableToInsertProbe"), excp); //$NON-NLS-1$
				}
				if (die) {
					MessageDialog
							.openError(
									PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow()
											.getShell(),
									Localization
											.getString("CEditor.ProbeInsertFailed"), Localization.getString("CEditor.CanNotProbeLine")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					IEditorInput in = editor.getEditorInput();
					if (in instanceof FileStoreEditorInput) {
						FileStoreEditorInput input = (FileStoreEditorInput) in;

						IPreferenceStore p = IDEPlugin.getDefault()
								.getPreferenceStore();
						String kernroot = p
								.getString(IDEPreferenceConstants.P_KERNEL_SOURCE);

						String filepath = input.getURI().getPath();
						String kernrelative = filepath.substring(
								kernroot.length() + 1, filepath.length());
						StringBuffer sb = new StringBuffer();

						sb.append("probe kernel.statement(\"*@" + kernrelative + ":" + (lineno + 1) + "\")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						sb.append("\n{\n\t\n}\n"); //$NON-NLS-1$
						STPEditor activeSTPEditor = IDESessionSettings
								.getActiveSTPEditor();
						if (null == activeSTPEditor) {
							NewFileAction action = new NewFileAction();
							action.run();
							IEditorPart ed = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.getActiveEditor();
							if (ed instanceof STPEditor) {
								IDESessionSettings
										.setActiveSTPEditor((STPEditor) ed);
							}
						}

						if (null != activeSTPEditor) {
							activeSTPEditor.insertText(sb.toString());
						}
					}
				}
				shell.setCursor(null); // Return the cursor to normal

			}

		};
	}

}
