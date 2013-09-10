/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *    Kyu Lee <klee@redhat.com>          - new execute method
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.linuxtools.internal.changelog.core.ChangeLogWriter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * @author pmuldoon (Phil Muldoon)
 */
public class InsertChangeLogKeyHandler extends ChangeLogAction implements
		IHandler, IWorkbenchWindowActionDelegate {

	private IEditorPart currentEditor;

	private String getEditorName() {
		if (currentEditor != null)
			return returnQualifedEditor(currentEditor.getClass());
		else
			return "";

	}

	private String getEntryFilePath() {
		if (currentEditor != null)
			return getDocumentLocation(currentEditor, false);
		else
			return "";
	}

	private String returnQualifedEditor(Class<?> ClassName) {
		return ClassName.toString().substring(
				ClassName.getPackage().toString().length() - 1,
				ClassName.toString().length());
	}


	IEditorPart getChangelog() {

		IConfigurationElement formatterConfigElement = extensionManager
				.getFormatterConfigElement();
		if (formatterConfigElement.getAttribute("inFile").toLowerCase().equals(
				"true")) {
			return currentEditor;
			// this formatter wants to use an external changelog file
		} else {
			IEditorPart changelog = null;

			IConfigurationElement nameElement = formatterConfigElement
					.getChildren()[0];
			if (nameElement.getAttribute("name") == null) {
				reportErr("Got non-name child with inFile set to False", null);
				return null;
			} else {
				pref_ChangeLogName = nameElement.getAttribute("name");
				changelog = getChangelog(getDocumentLocation(currentEditor,
						false));

				if (changelog == null) {
					changelog = askChangeLogLocation(getDocumentLocation(
							currentEditor, false));
				}

				return changelog;
			}
		}

	}

	String parseFunctionName(IParserChangeLogContrib parser) {

		try {
			return parser.parseCurrentFunction(currentEditor);
		} catch (CoreException e) {
			reportErr("Couldn't parse function name with "
					+ parser.getClass().toString(), null);
			return "";
		}

	}

	@Override
	public Object execute(ExecutionEvent event) {

		currentEditor = HandlerUtil.getActiveEditor(event);

		// make sure an editor is selected.
		if (currentEditor == null) {
			return null;
		}

		ChangeLogWriter clw = new ChangeLogWriter();

		// load settings from extensions + user pref.
		loadPreferences();

		// get file path from target file
		clw.setEntryFilePath(getEntryFilePath());

		// err check. do nothing if no file is being open/edited
		if (clw.getEntryFilePath() == "") {
			return null;
		}

		String editorName = getEditorName();

		// get a parser for this file
		IParserChangeLogContrib parser = extensionManager
				.getParserContributor(editorName);

		// if no parser for this type of document, then don't guess function
		// name
		// and set it as "".
		if (parser == null) {
			clw.setGuessedFName("");
		} else {
			// guess function name
			clw.setGuessedFName(parseFunctionName(parser));
		}

		// get formatter
		clw.setFormatter(extensionManager.getFormatterContributor(clw
				.getEntryFilePath(), pref_Formatter));

		// select changelog
		clw.setChangelog(getChangelog());
		if (clw.getChangelog() == null)
			return null;

		// write to changelog
		clw.setDateLine(clw.getFormatter().formatDateLine(pref_AuthorName,
				pref_AuthorEmail));

		clw.setChangelogLocation(getDocumentLocation(clw.getChangelog(), true));

		clw.writeChangeLog();

		return null;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	@Override
	public boolean isEnabled() {
		IEditorReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < refs.length; ++i) {
			IEditorReference ref = refs[i];
			String id = ref.getId();
			System.out.println(id);
		}
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {

	}

	@Override
	public void run(IAction action) {

			execute(null);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}
}
