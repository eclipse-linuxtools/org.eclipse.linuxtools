/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.actions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfilePartitionScanner;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class SpecfileChangelogAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public SpecfileChangelogAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor instanceof SpecfileEditor) {
			SpecfileEditor specEditor = (SpecfileEditor) editor;
			IDocument doc = specEditor.getDocumentProvider().getDocument(
					specEditor.getEditorInput());
			String[] positionCategories = doc.getPositionCategories();
			String contentTypesPositionCategory = null;

			// there is some random number suffix to the category positions,
			// we need to find the one we want
			for (int i = 0; i < positionCategories.length; i++) {
				if (positionCategories[i]
						.startsWith("__content_types_category"))
					contentTypesPositionCategory = positionCategories[i];
			}

			if (contentTypesPositionCategory != null) {
				try {
					Position[] sectionPositions = doc
							.getPositions(contentTypesPositionCategory);
					ITypedRegion changelogPartition = null;
					for (int i = 0; i < sectionPositions.length; i++) {
						Position position = sectionPositions[i];
						int offset = position.getOffset();

						ITypedRegion partition = doc.getPartition(offset);
						if (partition.getType().equals(
								SpecfilePartitionScanner.SPEC_CHANGELOG))
							changelogPartition = partition;

					}

					// there was no changelog partition - add it.
					if (changelogPartition == null) {
						System.err
								.println("Did not get changelog partition. Please make me one!");
					}

					// now add the entry stub
					System.err.println("Got specfile partition: "
							+ changelogPartition.getOffset() + ","
							+ changelogPartition.getLength());
					String changelogText = doc.get(changelogPartition
							.getOffset(), changelogPartition.getLength());
					String[] changelogLines = changelogText.split("\n");
					StringBuffer buf = new StringBuffer();
					buf.append(changelogLines[0] + "\n");
					buf.append(createChangelogEntry(specEditor.getSpecfile(), doc) + "\n");
					buf.append(" - \n");
					int newCursorOffset = changelogPartition.getOffset() + buf.length() -1;
					
					for (int i = 1; i < changelogLines.length; i++) {
						buf.append(changelogLines[i] + "\n");
					}

					doc.replace(changelogPartition.getOffset(),
							changelogPartition.getLength(), buf.toString());
					specEditor.selectAndReveal(newCursorOffset, 0);
					specEditor.setFocus();
				} catch (BadPositionCategoryException e) {
					SpecfileLog.logError(e);
				} catch (BadLocationException e) {
					SpecfileLog.logError(e);
				}
			} else {
				// log error, we didn't find content type category positions,
				// WTF?
			}
		} else {
			// TODO: Log error.
			System.err.println("Got " + editor.getClass().toString()
					+ " editor");
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	protected String createChangelogEntry(Specfile specfile, IDocument doc) {
		if (specfile == null)
			return "";
		// FIXME:  this is hack-tastic
//		ChangelogPlugin changelogPlugin = new ChangelogPlugin();
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(),
				"com.redhat.eclipse.changelog.core");
//		IPreferenceStore store = changelogPlugin.getPreferenceStore();

		String name = store.getString("IChangeLogConstants.AUTHOR_NAME"); //$NON-NLS-1$
		String email = store.getString("IChangeLogConstants.AUTHOR_EMAIL"); //$NON-NLS-1$

		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		System.err.println(cal.get(Calendar.DAY_OF_WEEK));
		String date = (new SimpleDateFormat("EEE MMM d yyyy")).format(new Date());
		
		SpecfileElement resolveElement = new SpecfileElement();
		resolveElement.setSpecfile(specfile);
		String epoch = specfile.getEpoch() == -1 ? "": (specfile.getEpoch() + ":");
		String version = specfile.getVersion() == null ? "": resolveElement.resolve(specfile.getVersion());
		String release = specfile.getRelease() == null ? "": resolveElement.resolve(specfile.getRelease());
		
		
		return "* " + date + " " + name + "  <" + email + "> " + epoch + version + "-" + release;
	}
}