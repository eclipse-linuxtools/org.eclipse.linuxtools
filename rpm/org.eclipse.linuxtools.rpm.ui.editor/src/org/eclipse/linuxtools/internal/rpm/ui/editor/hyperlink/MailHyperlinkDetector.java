/*******************************************************************************
 * Copyright (c) 2008 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;


/**
 * Mail hyperlink detector. Largely inspired of {@link org.eclipse.jface.text.hyperlink.URLHyperlinkDetector}
 */
public class MailHyperlinkDetector extends AbstractHyperlinkDetector {

	private SpecfileEditor editor;

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		if (editor == null) {
			editor = ((SpecfileEditor) this.getAdapter(SpecfileEditor.class));
			if (editor == null) {
				return null;
			}
		}

		IDocument document= textViewer.getDocument();

		int offset= region.getOffset();

		String urlString= null;
		if (document == null) {
			return null;
		}

		IRegion lineInfo;
		String line;
		String mail;
		int mailLength = 0;
		int mailOffsetInLine;
		try {
			lineInfo= document.getLineInformationOfOffset(offset);
			line= document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace();
			return null;
		}

		int startSeparator= line.indexOf('<');
		mailOffsetInLine = startSeparator + 1;

		if (startSeparator != -1) {
			int endSeparator= line.indexOf('>');

			if (endSeparator < 5) {
				return null;
			}

			mail= line.substring(startSeparator + 1, endSeparator).trim();
			mailLength= mail.length();

			// Some cleanups, maybe we can add more.
			mail= mail.replaceAll("(?i) at ", "@"); //$NON-NLS-1$ //$NON-NLS-2$
			mail= mail.replaceAll("(?i) dot ", "."); //$NON-NLS-1$ //$NON-NLS-2$
			mail= mail.replaceAll("(?i)_at_", "@"); //$NON-NLS-1$ //$NON-NLS-2$
			mail= mail.replaceAll("(?i)_dot_", "."); //$NON-NLS-1$ //$NON-NLS-2$

			mail= mail.replaceAll(" +", " "); //$NON-NLS-1$ //$NON-NLS-2$
			if (mail.split(" ").length == 3) { //$NON-NLS-1$
				if (mail.indexOf('@') == -1) {
					mail = mail.replaceFirst(" ", "@").replaceFirst(" ", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
			}
			mail= mail.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$

		} else {

			int offsetInLine= offset - lineInfo.getOffset();

			boolean startDoubleQuote= false;
			mailOffsetInLine= 0;

			int mailSeparatorOffset= line.indexOf('@');
			while (mailSeparatorOffset >= 0) {

				// (left to "@")
				mailOffsetInLine= mailSeparatorOffset;
				char ch;
				do {
					mailOffsetInLine--;
					ch= ' ';
					if (mailOffsetInLine > -1) {
						ch= line.charAt(mailOffsetInLine);
					}
					startDoubleQuote= ch == '"';
				} while (Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-');
				mailOffsetInLine++;

				// a valid mail contain a left part.
				if (mailOffsetInLine == mailSeparatorOffset) {
					return null;
				}

				// Right to "@"
				StringTokenizer tokenizer= new StringTokenizer(line.substring(mailSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
				if (!tokenizer.hasMoreTokens()) {
					return null;
				}

				mailLength= tokenizer.nextToken().length() + 3 + mailSeparatorOffset - mailOffsetInLine;
				if (offsetInLine >= mailOffsetInLine && offsetInLine <= mailOffsetInLine + mailLength) {
					break;
				}

				mailSeparatorOffset= line.indexOf('@', mailSeparatorOffset + 1);
			}

			if (mailSeparatorOffset < 0) {
				return null;
			}

			if (startDoubleQuote) {
				int endOffset= -1;
				int nextDoubleQuote= line.indexOf('"', mailOffsetInLine);
				int nextWhitespace= line.indexOf(' ', mailOffsetInLine);
				if (nextDoubleQuote != -1 && nextWhitespace != -1) {
					endOffset= Math.min(nextDoubleQuote, nextWhitespace);
				} else if (nextDoubleQuote != -1) {
					endOffset= nextDoubleQuote;
				} else if (nextWhitespace != -1) {
					endOffset= nextWhitespace;
				}
				if (endOffset != -1) {
					mailLength= endOffset - mailOffsetInLine;
				}
			}
			if (mailLength == 0) {
				return null;
			}

			mail= line.substring(mailOffsetInLine, mailOffsetInLine + mailLength);
		}

		try {
			// mail address contain at less one '@' and one '.' character.
			if (!mail.contains("@") || !mail.contains(".")) { //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}

			urlString= "mailto:" + mail; //$NON-NLS-1$
			char separator= '?';
			String subject= getSubject();
			if (subject != null) {
				urlString+= separator + "subject=" + subject; //$NON-NLS-1$
				separator= '&';
			}
			String body= getBody();
			if (body != null) {
				urlString+= separator + "body=" + body; //$NON-NLS-1$
			}

			// url don't like %
			urlString= urlString.replaceAll("\\%", "\\%25"); //$NON-NLS-1$ //$NON-NLS-2$
			new URL(urlString);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			urlString= null;
			return null;
		}

		IRegion urlRegion= new Region(lineInfo.getOffset() + mailOffsetInLine, mailLength);
		return new IHyperlink[] {new MailHyperlink(urlRegion, urlString)};
	}

	private String getSubject() {
		Specfile specfile= editor.getSpecfile();
		return MessageFormat.format("[{0}.spec - {1}-{2}]", specfile.getName(), specfile.getVersion(), //$NON-NLS-1$
				specfile.getRelease());
	}

	private String getBody() {
		String body = null;
		// Get current selection
		IDocument document= (IDocument) editor.getAdapter(IDocument.class);
		ISelection currentSelection= editor.getSpecfileSourceViewer().getSelection();
		if (currentSelection instanceof ITextSelection) {
			ITextSelection selection= (ITextSelection) currentSelection;
			try {
				String txt= selection.getText();
				if (txt.trim().length() > 0) {
					int begin= document.getLineOffset(selection.getStartLine());
					body= document.get().substring(begin,
							selection.getOffset() + selection.getLength());
					// replace left spaces or tabs and add a space at the begin of each line.
					body= body.replaceAll("(?m)^[ \\t]+|[ \\t]+$|^", " "); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch (BadLocationException e) {
			}

		}
		return body;
	}

	public void setEditor(SpecfileEditor editor) {
		this.editor = editor;
	}
}
