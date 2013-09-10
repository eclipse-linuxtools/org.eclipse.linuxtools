/*******************************************************************************
 * Copyright (c) 2004, 2007, 2011 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.formatters;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.linuxtools.changelog.core.IFormatterChangeLogContrib;
import org.eclipse.linuxtools.internal.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.internal.changelog.core.editors.ChangeLogEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * @author pmuldoon (Phil Muldoon)
 */
public class GNUFormat implements IFormatterChangeLogContrib {

	final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$

	final static String TAB = "\t"; // $NON-NLS-1$


	@Override
	public String formatDateLine(String authorName, String authorEmail) {
		String detail = returnDate() + "  " + //$NON-NLS-1$
		authorName + "  " + "<" + authorEmail + ">" + line_sep + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		line_sep;
		return detail;
	}


	@Override
	public String mergeChangelog(String dateLine, String functionGuess,String defaultContent,
			IEditorPart changelog, String changeLogLocation, String fileLocation) {

		String fileDetail = formatFileDetail(changeLogLocation, fileLocation);
		IDocument changelog_doc = getDocument(changelog);
		String function = formatFunction(functionGuess);
		boolean multipleEntrySuccess = false;
		boolean forceNewEntry = false;
		String functionSpacer = " "; // $NON-NLS-1$
		if (function.equals(": ")) // $NON-NLS-1$
			functionSpacer = ""; // $NON-NLS-1$

		/* Fix Bz #366854.  Make sure that forceNewEntry is used only
		 * once and then cleared even when the ChangeLog is empty to start with.
		 */
		if(changelog instanceof ChangeLogEditor) {
			ChangeLogEditor editor = (ChangeLogEditor)changelog;
			forceNewEntry = editor.isForceNewLogEntry();
			editor.setForceNewLogEntry(false);
		}

		if (changelog_doc.getLength() > 0) {

			int offset_start = findChangeLogEntry(changelog_doc, dateLine);
			int offset_end = dateLine.length();
			boolean foundFunction = false;
			//if the prepare change action determines it requires a new entry, we force
			//a new entry by changing the offset_start and change the corresponding field
			//of the editor back to false to prevent subsequent function change log being
			//written to a new entry again.
			if (forceNewEntry)
				offset_start = -1;

			if (offset_start != -1) {
				int nextChangeEntry = findChangeLogPattern(changelog_doc,
						offset_start + dateLine.length());
				int functLogEntry = offset_start + dateLine.length();

				while (functLogEntry < nextChangeEntry) {
					int line_length = 0;
					String entry = ""; // $NON-NLS-1$
					try {
						line_length = changelog_doc
						.getLineOfOffset(functLogEntry);
						entry = changelog_doc.get(functLogEntry, changelog_doc
								.getLineLength(line_length));
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// Look to see if entry already exists for file (will be preceded by "*")
					if (entry.lastIndexOf("* " + fileDetail) > 0) {
						foundFunction = true;
						// Check for the case where the default content (e.g. new or removed file)
						// is being caught again because user has prepared the ChangeLog more than once.
						// In such a case, just return.  We don't need to repeat ourselves.
						if (defaultContent.length() > 0 && entry.lastIndexOf(defaultContent) > 0) {
								return ""; // $NON-NLS-1$
						}
						int nextFunctLoc = functLogEntry + fileDetail.length() + 2;
						String nextFunc = ""; // $NON-NLS-1$
						try {
							nextFunc = changelog_doc.get(nextFunctLoc,
									nextChangeEntry - nextFunctLoc);
						} catch (BadLocationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						int foundFunc = nextFunc.indexOf("* "); // $NON-NLS-1$
						if (foundFunc > 0) {
							foundFunc--;
							try {
								while (changelog_doc.get(
										nextFunctLoc + foundFunc, 1).equals(
										"\t") // $NON-NLS-1$
										|| changelog_doc.get(
												nextFunctLoc + foundFunc, 1)
												.equals("\n")) { // $NON-NLS-1$
									foundFunc--;
								}
							} catch (BadLocationException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							functLogEntry = nextFunctLoc + foundFunc + 1;
							break;
						}

					}
					try {
						functLogEntry += changelog_doc
						.getLineLength(line_length);
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if (functLogEntry >= nextChangeEntry) {
					functLogEntry = nextChangeEntry - 1;
					try {
						// Get rid of some potential lines containing whitespace only.
						functLogEntry = removeWhitespaceOnlyLines(changelog_doc, functLogEntry);
						while (changelog_doc.get(functLogEntry, 1).equals("\n")) // $NON-NLS-1$
							functLogEntry--;
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					functLogEntry++;
				}

				if (offset_start != -1) {
					if (foundFunction) {
						try {
							if (!function.equals(": ")) // $NON-NLS-1$
								changelog_doc.replace(functLogEntry, 0, "\n" + TAB // $NON-NLS-1$
										+ function + " "); // $NON-NLS-1$
							else
								changelog_doc.replace(functLogEntry, 0, "\n" + TAB // $NON-NLS-1$
										);
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ITextEditor edit = (ITextEditor) changelog;
						if (!function.equals(": ")) // $NON-NLS-1$
							edit.selectAndReveal(functLogEntry + function.length()
									+ 3, 0);
						else
							edit.selectAndReveal(functLogEntry + function.length()
									, 0);
						multipleEntrySuccess = true;
					} else {
						try {
							changelog_doc.replace(offset_end, 0, TAB
									+ "* " + fileDetail + functionSpacer // $NON-NLS-1$
									+ function + functionSpacer + defaultContent + "\n"); //$NON-NLS-1$
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						ITextEditor edit = (ITextEditor) changelog;
						edit.selectAndReveal(offset_end + fileDetail.length()
								+ function.length() +functionSpacer.length()*2 + 3 + defaultContent.length(), 0);
						multipleEntrySuccess = true;
					}

				}
			}
		}

		if (!multipleEntrySuccess) {
			try {
				if (changelog_doc.getLength() > 0)
					changelog_doc.replace(0, 0, "\n\n"); //$NON-NLS-1$
				changelog_doc.replace(0, 0, dateLine + TAB + "* " + fileDetail // $NON-NLS-1$
						+ functionSpacer+function+functionSpacer+defaultContent);

				ITextEditor edit = (ITextEditor) changelog;
				edit.selectAndReveal(dateLine.length() + fileDetail.length()
						+ function.length() + functionSpacer.length()*2 + 3 + defaultContent.length(), 0);
			} catch (BadLocationException e) {
				e.printStackTrace();

			}

		}

		return ""; // $NON-NLS-1$

	}

	/**
	 * Remove any empty lines (i.e. lines only containing whitespace) between
	 * <code>offset</code> and index backed-up until a '\n' preceded by some non-whitespace
	 * character is reached. Whitespace will be merged to '\n\n'. For example
	 * consider the following string "(main): Removed.\n\t\ \n\n\t\n" and
	 * <code>offset</code> pointing to the last '\n'. This string would be
	 * changed to: "(main): Removed.\n\n".
	 *
	 * @param changelog_doc
	 * @param offset
	 * @return The new offset.
	 */
	private int removeWhitespaceOnlyLines(IDocument changelog_doc, int offset) {
		int initialOffset = offset;
		int backedUpOffset = offset;
		char charAtOffset;
		try {
			charAtOffset = changelog_doc.get(offset, 1).charAt(0);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return offset;
		}
		while( backedUpOffset > 0 && (charAtOffset == '\n' || charAtOffset == '\t' || charAtOffset == ' ') ) {
			backedUpOffset--;
			try {
			charAtOffset = changelog_doc.get(backedUpOffset, 1).charAt(0);
			} catch (BadLocationException e) {
				e.printStackTrace();
				break;
			}
		}
		if ( (initialOffset - backedUpOffset) > 2 ) {
			try {
				int replaceLength = (initialOffset - backedUpOffset - 2);
				changelog_doc.replace(backedUpOffset + 2, replaceLength, "");
				// change offset accordingly
				offset -= replaceLength;
			} catch (BadLocationException e) {
				// exception should have been thrown earlier if that's
				// really a bad location...
			}
		}
		return offset;
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private String formatFileDetail(String changeLogLocation,
			String editorFileLocation) {
		// Format Path. Is a full path specified, or just file name?
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		String WorkspaceRoot = myWorkspaceRoot.getLocation().toOSString();
		String changeLogLocNoRoot = ""; // $NON-NLS-1$
		String editorFileLocNoRoot = ""; // $NON-NLS-1$
		if (changeLogLocation.lastIndexOf(WorkspaceRoot) >= 0) {
			changeLogLocNoRoot = changeLogLocation.substring(changeLogLocation
					.lastIndexOf(WorkspaceRoot)
					+ WorkspaceRoot.length(), changeLogLocation.length());
		} else
			changeLogLocNoRoot = changeLogLocation;

		if (editorFileLocation.lastIndexOf(WorkspaceRoot) >= 0) {
			editorFileLocNoRoot = editorFileLocation.substring(
					editorFileLocation.lastIndexOf(WorkspaceRoot),
					editorFileLocation.lastIndexOf(WorkspaceRoot)
					+ WorkspaceRoot.length());
		} else
			editorFileLocNoRoot = editorFileLocation;

		File changelogLocation = new File(changeLogLocNoRoot);
		File fileLocation = new File(editorFileLocNoRoot);
		File reversePath = fileLocation.getParentFile();
		String reversePathb = ""; // $NON-NLS-1$

		while (reversePath.getParentFile() != null) {
			if (reversePath.compareTo(changelogLocation.getParentFile()) == 0)
				break;
			reversePath = reversePath.getParentFile();
		}
		if (reversePath != null)
			reversePathb = fileLocation.toString().substring(
					reversePath.toString().length() + 1,
					fileLocation.toString().length());
		return reversePathb;
	}

	private int findChangeLogPattern(IDocument changelog_doc, int startOffset) {
		// find the "pattern" of a changelog entry. Not a specific one,
		// but one that "looks" like an entry
		int nextEntry = startOffset;
		int line_length = 0;
		String entry = ""; // $NON-NLS-1$
		while (nextEntry < changelog_doc.getLength()) {
			try {
				// Get the line of interest in the changelog document
				line_length = changelog_doc.getLineOfOffset(nextEntry);
				entry = changelog_doc.get(nextEntry, changelog_doc
						.getLineLength(line_length));
				// Attempt to find date pattern on line
				if (matchDatePattern(entry)) {
					//nextDate -= entry.length()+1;
					break;
				}
				// If no date matches, move to the next line
				nextEntry += changelog_doc.getLineLength(line_length);
			} catch (BadLocationException e) {
				ChangelogPlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR, e
								.getMessage(), e

						));
			}

		}
		return nextEntry;
	}

	private boolean matchDatePattern(String text) {

		// Set up patterns for looking for the next date in the changelog
		SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd"); // $NON-NLS-1$

		// Try to find next Date bounded changelog entry by parsing date patterns
		// First start with an ISO date
		try {
			Date ad = isoDate.parse(text);
			if (ad != null)
				return true;
		} catch (ParseException e) {
			// We don't really care on exception; it just means it could not parse a date on that line
		}

		return false;
	}

	private int findChangeLogEntry(IDocument changelog_doc, String entry) {
		FindReplaceDocumentAdapter findDocumentAptd = new FindReplaceDocumentAdapter(
				changelog_doc);
		IRegion region = null;
		try {
			region = findDocumentAptd.find(0, entry, true, false,/*whole world */ false, true);
		} catch (BadLocationException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR, e
							.getMessage(), e

					));
			return -1;
		}
		if (region != null) {
			// If the user's entry is not at the beginning of the file,
			// make a new entry.
			return region.getOffset() > 0 ? -1 : 0;
		}
		else
			return -1;
	}

	private String formatFunction(String function) {

		// If Function Guess is true, and Function Guess has found something
		if (function.length() > 0)
			return "(" + function + "):"; // $NON-NLS-1$ // $NON-NLS-2$
		else
			return ": "; //$NON-NLS-1$

	}

	public IDocument getDocument(IEditorPart currentEditor) {
		AbstractTextEditor castEditor = (AbstractTextEditor) currentEditor;
		IDocumentProvider provider = castEditor.getDocumentProvider();

		return provider.getDocument(castEditor.getEditorInput());
	}

	private String returnDate() {
		SimpleDateFormat date_Format;
		date_Format = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		return date_Format.format(new Date());
	}

}