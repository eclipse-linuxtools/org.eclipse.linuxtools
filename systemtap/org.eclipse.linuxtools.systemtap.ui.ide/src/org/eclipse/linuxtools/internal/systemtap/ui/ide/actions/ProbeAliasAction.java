/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.ProbevarNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListDialog;



/**
 * This <code>Action</code> is fired when the user selects an item in the <code>ProbeAliasBrowserView</code>.
 * The action taken is to insert a template probe in the current <code>STPEditor</code>, if available, or to
 * insert the probe into a new <code>STPEditor</code> if one does not exist.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor#insertText(String)
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView
 * @see org.eclipse.jface.action.Action
 */
public class ProbeAliasAction extends BrowserViewAction {
	private static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.ProbeAliasAction"; //$NON-NLS-1$

	public ProbeAliasAction(IWorkbenchWindow window, ProbeAliasBrowserView view) {
		super(window, view);
		setId(ID);
		setActionDefinitionId(ID);
		setText(Localization.getString("ProbeAliasAction.Insert")); //$NON-NLS-1$
		setToolTipText(Localization
				.getString("ProbeAliasAction.InsertSelectedProbe")); //$NON-NLS-1$
	}

	/**
	 * The main body of the action. This method checks for the current editor, creating one
	 * if there is no active <code>STPEditor</code>, and then inserts a template probe for the
	 * item that the user clicked on.
	 */
	@Override
	public void run() {
		IWorkbenchPage page = getWindow().getActivePage();
		Object o = getSelectedElement();
		if (o instanceof TreeNode) {
			TreeNode t = (TreeNode) o;
			if (t.isClickable()) {
				IEditorPart editor = page.getActiveEditor();
				if (!(editor instanceof STPEditor)) {
					editor = findEditor();
					if (null == editor) {
						return;
					}
				}
				page.activate(editor);
				buildString((STPEditor) editor, (TreeNode) o);
			} else {
				runExpandAction();
			}
		}
	}

	private void buildString(STPEditor stpeditor, TreeNode t) {
		//build the string
		StringBuilder s = new StringBuilder("\nprobe " + t.toString()); //$NON-NLS-1$
		if (t.getChildCount() > 0 && t.getChildAt(0).getData() instanceof ProbeNodeData) {
			s.append(".*"); //$NON-NLS-1$
		}
		s.append("\n{\n"); //$NON-NLS-1$
		if (t.getChildCount() > 0 && t.getChildAt(0).getData() instanceof ProbevarNodeData) {
			s.append("\t/*\n\t * " + //$NON-NLS-1$
					Localization
					.getString("ProbeAliasAction.AvailableVariables") + //$NON-NLS-1$
					"\n\t * "); //$NON-NLS-1$
			boolean first = true;
			for(int i = 0; i < t.getChildCount(); i++) {
				if(first) {
					first = false;
				} else {
					s.append(", "); //$NON-NLS-1$
				}
				s.append(t.getChildAt(i).toString());
			}
			s.append("\n\t */\n"); //$NON-NLS-1$
		}
		s.append("\n}\n"); //$NON-NLS-1$
		stpeditor.insertText(s.toString());
	}

	private IEditorPart getRestoredEditor(final IEditorReference ref) {
		if (ref.getEditor(false) == null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					ref.getEditor(true);
				}
			});
		}
		return ref.getEditor(false);
	}

	private STPEditor findEditor() {
		final List<STPEditor> allEditors = new LinkedList<>();
		for (IEditorReference ref : getWindow().getActivePage().getEditorReferences()) {
			IEditorPart editor = getRestoredEditor(ref);
			if (editor instanceof STPEditor) {
				allEditors.add((STPEditor) editor);
			}
		}

		switch (allEditors.size()) {
		// If only one file is found, open it. Give user the option to open another file.
		case 1:
			MessageDialog messageDialog = new MessageDialog(getWindow().getShell(),
					Localization.getString("ProbeAliasAction.DialogTitle"), null, //$NON-NLS-1$
					MessageFormat.format(Localization.getString("ProbeAliasAction.AskBeforeAddMessage"), //$NON-NLS-1$
							allEditors.get(0).getEditorInput().getName() ),
							MessageDialog.QUESTION,
							new String[]{Localization.getString("ProbeAliasAction.AskBeforeAddCancel"), //$NON-NLS-1$
				Localization.getString("ProbeAliasAction.AskBeforeAddAnother"), //$NON-NLS-1$
				Localization.getString("ProbeAliasAction.AskBeforeAddYes")}, 2); //$NON-NLS-1$

			switch (messageDialog.open()) {
			case 2:
				return allEditors.get(0);

			case 1:
				return openNewFile();

			default:
				return null;
			}

		// If no files found, prompt user to open a new file
		case 0:
			return openNewFile();

		// If multiple files found, prompt user to select one of them
		default:
			return openNewFileFromMultiple(allEditors);
		}
	}

	private STPEditor openNewFileFromMultiple(final List<STPEditor> allEditors) {
		ListDialog listDialog = new ListDialog(getWindow().getShell());
		listDialog.setTitle(Localization.getString("ProbeAliasAction.DialogTitle")); //$NON-NLS-1$
		listDialog.setContentProvider(new ArrayContentProvider());

		listDialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				int i = (Integer) element;
				return i != -1 ? allEditors.get(i).getEditorInput().getName()
						: Localization.getString("ProbeAliasAction.OtherFile"); //$NON-NLS-1$
			}
		});

		Integer[] editorIndexes = new Integer[allEditors.size() + 1];
		for (int i = 0; i < editorIndexes.length - 1; i++) {
			editorIndexes[i] = i;
		}
		editorIndexes[editorIndexes.length - 1] = -1;
		listDialog.setInput(editorIndexes);
		listDialog.setMessage(Localization.getString("ProbeAliasAction.SelectEditor")); //$NON-NLS-1$
		if (listDialog.open() == Window.OK) {
			int result = (Integer) listDialog.getResult()[0];
			return result != -1 ? allEditors.get(result) : openNewFile();
		}
		// Abort if user cancels
		return null;
	}

	private STPEditor openNewFile() {
		NewFileAction action = new NewFileAction();
		action.run();
		if (action.isSuccessful()) {
			return (STPEditor) getWindow().getActivePage().getActiveEditor();
		}
		return null;
	}

}
