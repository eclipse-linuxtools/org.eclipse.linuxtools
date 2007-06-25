/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.internal.resources.mapping.SimpleResourceMapping;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.changelog.core.ChangeLogWriter;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Diff;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.IContributorResourceAdapter2;
import org.eclipse.ui.part.FileEditorInput;


/**
 * Action handler for prepare changelog.
 * 
 * @author klee
 * 
 */
public class PrepareChangeLogAction extends ChangeLogAction {

	/**
	 * Provides IDocument given editor input
	 * 
	 * @author klee
	 * 
	 */
	private class MyDocumentProvider extends FileDocumentProvider {

		public IDocument createDocument(Object element) throws CoreException {

			return super.createDocument(element);

		}
	}

	public PrepareChangeLogAction(String name) {
		super(name);
	}

	private IStructuredSelection selected;

	public PrepareChangeLogAction() {

		super();

	}

	protected void setSelection(IStructuredSelection selection) {
		this.selected = selection;
	}

	private String parseCurrentFunctionAtOffset(String editorName,
			IEditorInput input, int offset) {

		IParserChangeLogContrib parser = extensionManager
				.getParserContributor(editorName);

		try {
			return parser.parseCurrentFunction(input, offset);
		} catch (CoreException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
		}
		return "";
	}

	// if (parserExtensions != null) {
	// IConfigurationElement[] elements = parserExtensions
	// .getConfigurationElements();
	// for (int i = 0; i < elements.length; i++) {
	// if (elements[i].getName().equals("parser") &&
	// (elements[i].getAttribute("editor").equals(editorName))) { //$NON-NLS-1$
	// try {
	// IConfigurationElement bob = elements[i];
	// parserContributor = (IParserChangeLogContrib) bob
	// .createExecutableExtension("class");
	// return parserContributor.parseCurrentFunction(input,
	// offset);
	// } catch (CoreException e) {
	// ChangelogPlugin.getDefault().getLog().log(
	// new Status(IStatus.ERROR, "Changelog",
	// IStatus.ERROR, e.getMessage(), e));
	// }
	//
	// }
	// }
	// }
	// return "";
	// }

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	protected void doRun() {

		IRunnableWithProgress code = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Preparing ChangeLog", 1000);
				preapreChangeLog(monitor);
				monitor.done();
			}
		};

		ProgressMonitorDialog pd = new ProgressMonitorDialog(getWorkbench()
				.getActiveWorkbenchWindow().getShell());

		try {
			pd.run(false /* fork */, false /* cancelable */, code);
		} catch (InvocationTargetException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
			return;
		} catch (InterruptedException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
		}
	}

	private ResourceMapping getResourceMapping(Object o) {
		if (o instanceof ResourceMapping) {
			return (ResourceMapping) o;
		}
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o,
					ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
		}
		return null;
	}

	private ResourceMapping[] getResourceMappings(Object[] objects) {
		List result = new ArrayList();
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			ResourceMapping mapping = getResourceMapping(object);
			if (mapping != null)
				result.add(mapping);
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result
				.size()]);
	}

	private void preapreChangeLog(IProgressMonitor monitor) {

		// getParserContributions();
		String diffResult = null;
		String projectPath = null;

		Object element = selected.getFirstElement();

		ResourceMapping[] mappings = getResourceMappings(selected.toArray());

		IResource resource;
		if (element instanceof SimpleResourceMapping)
			resource = (IResource) ((SimpleResourceMapping) element)
					.getModelObject();
		else
			resource = (IResource) element;

		projectPath = resource.getProject().getFullPath().toOSString();

		try {

			StringDiffOperation sdo = new StringDiffOperation(getWorkbench()
					.getActiveWorkbenchWindow().getPartService()
					.getActivePart(), mappings,
					new LocalOption[] { Diff.INCLUDE_NEWFILES }, false, true,
					ResourcesPlugin.getWorkspace().getRoot().getFullPath());

			sdo.execute(monitor);

			diffResult = sdo.getResult();
		} catch (CVSException e) {

			e.printStackTrace();
			return;
		} catch (InterruptedException e) {

			e.printStackTrace();
			return;
		}

		if (diffResult == null) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR,
							"Could not get diff", new Exception(
									"No diff result from CVS")));
			return;
		}
		if (projectPath == null) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR,
							"Could not get project path", new Exception(
									"Couldn't get project path")));
			return;
		}

		if (diffResult.equals(StringDiffOperation.EMPTY_DIFF)) {
			MessageDialog.openInformation(getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					"Prepare ChangeLog - ChangeLog", "No changes found.");
			return;
		}
		// parse the patch and get only info we need
		// filename, which line has changed.(range)

		monitor.subTask("Parsing diff result");
		PatchFile[] patchFileInfoList = parseStandardPatch(diffResult,
				projectPath, monitor);
		monitor.worked(250);
		// now, find out modified functions/classes.
		// try to use the the extension point. so it can be extended easily

		if (patchFileInfoList == null) {
			// nothing to parse
			return;
		}

		// for all files in patch file info list, get function guesses of each
		// file.
		monitor.subTask("Writing ChangeLog");
		int unitwork = 250 / patchFileInfoList.length;
		for (int pfIndex = 0; pfIndex < patchFileInfoList.length; pfIndex++) {
			// for each file

			PatchFile pf = patchFileInfoList[pfIndex];

			// System.out.println(pf.getPath().toOSString());
			String[] funcGuessList = guessFunctionNames(pf);

			outputMultipleEntryChangeLog(pf.getPath().toOSString(),
					funcGuessList);

			/*
			 * // print info for debug
			 * System.out.println(pf.getPath().toOSString()); for (int i = 0; i <
			 * funcGuessList.length; i++) {
			 * System.out.println(funcGuessList[i]); }
			 * System.out.println("---------------------");
			 */
			monitor.worked(unitwork);
		}
	}

	protected IEditorPart changelog;

	public void outputMultipleEntryChangeLog(String entryFileName,
			String[] functionGuess) {

		ChangeLogWriter clw = new ChangeLogWriter();

		// load settings from extensions + user pref.
		loadPreferences();

		// get file path from target file
		clw.setEntryFilePath(entryFileName);

		// err check. do nothing if no file is being open/edited
		if (clw.getEntryFilePath() == "") {
			return;
		}

		// get formatter
		clw.setFormatter(extensionManager.getFormatterContributor(
				entryFileName, pref_Formatter));

		IEditorPart changelog = null;

		changelog = getChangelog(entryFileName);

		if (changelog == null)
			changelog = askChangeLogLocation(entryFileName);

		if (changelog == null) {
			System.out.println("oops, coudln't get changelog");
			return;
		}

		// select changelog
		clw.setChangelog(changelog);

		// write to changelog

		clw.setDateLine(clw.getFormatter().formatDateLine(pref_AuthorName,
				pref_AuthorEmail));

		clw.setChangelogLocation(getDocumentLocation(clw.getChangelog(), true));

		// print multiple changelog entries with different
		// function guess names.
		for (int i = 0; i < functionGuess.length; i++) {

			clw.setGuessedFName(functionGuess[i]);
			clw.writeChangeLog();
		}

	}

	/**
	 * Parses patch generated by CVS diff into <code>PatchFile</code> array.
	 * 
	 * @param diffResult
	 *            patch file
	 * @param projectPathh
	 *            local parent path for the patch
	 * @return array of PatchFile info
	 */
	protected PatchFile[] parseStandardPatch(String diffResult,
			String projectPath, IProgressMonitor monitor) {
		StringTokenizer st = new StringTokenizer(diffResult, "\n");
		ArrayList fileList = new ArrayList();

		// regex pattern for matching line info in standard patch.
		Pattern lineInfoPattern = Pattern
				.compile("(\\d+|\\d+,\\d+)[adc](\\d+|\\d+,\\d+)");

		boolean inRange = false;
		if (st.countTokens() == 0) 
			return null;
		int unitwork = 250 / st.countTokens();
		while (st.hasMoreTokens()) {
			String ln = st.nextToken();
			// this line contains file path relative to resource
			// and starts new file entry
			if (ln.indexOf("Index: ") == 0) {
				String fullPath = projectPath + "/" + ln.substring(7);

				// ignore all ChangeLogs
				if (fullPath.substring(
						fullPath.length() - "ChangeLog".length(),
						fullPath.length()).equals("ChangeLog")) {
					continue;
				}

				// System.out.println(fullPath + "- full path");
				fileList.add(new PatchFile(fullPath));
				inRange = false;
				continue;
			}

			if (fileList.size() > 0) {

				PatchFile tpe = (PatchFile) fileList.get(fileList.size() - 1);
				if (tpe != null) {

					Matcher linem = lineInfoPattern.matcher(ln);

					if (linem.matches()) {
						inRange = true;
						int from = 1;
						int length = 0;

						int modifierIndex;

						if ((modifierIndex = ln.indexOf("a")) < 0)
							if ((modifierIndex = ln.indexOf("d")) < 0)
								modifierIndex = ln.indexOf("c");

						// String firstHalf = ln.substring(0, modifierIndex);
						String secondHalf = ln.substring(modifierIndex + 1);

						int commaIndex;
						switch (ln.charAt(modifierIndex)) {

						case 'a':
						case 'c':

							if ((commaIndex = secondHalf.indexOf(",")) >= 0) {
								from = Integer.parseInt(secondHalf.substring(0,
										commaIndex));
								length = Integer.parseInt(secondHalf
										.substring(commaIndex + 1))
										- Integer.parseInt(secondHalf
												.substring(0, commaIndex));
							} else {
								from = Integer.parseInt(secondHalf);
							}

							break;

						case 'd':
							from = Integer.parseInt(secondHalf);
							from++;
							break;
						}

						tpe.addLineRange(from, from + length);
						continue;
					}

					// add actual patch just in case if we need it later.
					if (inRange)
						tpe.appendTxtToLastRange(ln);

				}
			}
			monitor.worked(unitwork);
		}

		PatchFile[] parseResult = new PatchFile[fileList.size()];
		for (int i = 0; i < fileList.size(); i++)
			parseResult[i] = (PatchFile) fileList.get(i);

		return (parseResult.length == 0) ? null : parseResult;
	}

	/**
	 * Guesses the function effected/modified by the patch from local file(newer
	 * file).
	 * 
	 * @param patchFileInfo
	 *            patch file
	 * @return array of unique function names
	 */
	private String[] guessFunctionNames(PatchFile patchFileInfo) {

		// NOTE //
		// some of the codes below only works with java files.

		String[] fnames = new String[0];
		String editorName = "";

		try {
			IEditorDescriptor ed = org.eclipse.ui.ide.IDE
					.getEditorDescriptor(patchFileInfo.getPath().toOSString());
			editorName = ed.getId().substring(ed.getId().lastIndexOf(".") + 1);
		} catch (PartInitException e1) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e1
							.getMessage(), e1));
			return new String[0];
		}

		// check if the file type is supported

		// get editor input for target file
		FileEditorInput fei = new FileEditorInput(getWorkspaceRoot()
				.getFileForLocation(
						getWorkspaceRoot().getLocation().append(
								patchFileInfo.getPath())));

		MyDocumentProvider mdp = new MyDocumentProvider();

		try {
			// get document for target file
			IDocument doc = mdp.createDocument(fei);

			PatchRangeElement[] tpre = patchFileInfo.getRanges();
			HashMap functionNamesMap = new HashMap();

			// for all the ranges

			for (int i = 0; i < patchFileInfo.countRanges(); i++) {

				// for all the lines in a range

				for (int j = tpre[i].ffromLine; j <= tpre[i].ftoLine; j++) {

					if ((j <= 0) || (j >= doc.getNumberOfLines()))
						continue; // ignore out of bound lines

					// add func that determines type of file.
					// right now it assumes it's java file.
					String functionGuess = parseCurrentFunctionAtOffset(
							editorName, fei, doc.getLineOffset(j));

					// putting it in hashmap will eliminate duplicate
					// guesses.
					functionNamesMap.put(functionGuess, functionGuess);

				}

			}

			// dump all unique func. guesses
			Iterator fnmIterator = functionNamesMap.values().iterator();

			fnames = new String[functionNamesMap.size()];

			int i = 0;
			while (fnmIterator.hasNext())
				fnames[i++] = (String) fnmIterator.next();

		
		} catch (CoreException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
		} catch (BadLocationException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
		} catch (Exception e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
		}
		return fnames;
	}

}