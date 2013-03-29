/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;

public class RpmMacroOccurrencesUpdater implements ISelectionChangedListener {

	private static final String ANNOTATION_TYPE = Activator.PLUGIN_ID + ".highlightannotation"; //$NON-NLS-1$

	private final SpecfileEditor fEditor;

	private final List<Annotation> fOldAnnotations = new LinkedList<Annotation>();

	/**
	 * Creates a new instance on editor <code>specfileEditor</code>.
	 *
	 * @param specfileEditor The editor to mark occurrences on.
	 */
	public RpmMacroOccurrencesUpdater(SpecfileEditor specfileEditor) {
		((IPostSelectionProvider) specfileEditor.getSelectionProvider())
				.addPostSelectionChangedListener(this);
		fEditor = specfileEditor;
	}

	/*
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		update((ISourceViewer) event.getSource());
	}

	/**
	 * Updates the drawn annotations.
	 *
	 * @param viewer The viewer to get the document and annotation model from
	 */
	public void update(ISourceViewer viewer) {
		try {
			IDocument document = viewer.getDocument();
			IAnnotationModel model = viewer.getAnnotationModel();
			if (document == null || model == null) {
				return;
			}
			removeOldAnnotations(model);
			String currentSelectedWord = getWordAtSelection(fEditor.getSelectionProvider()
					.getSelection(), document);
			if (isMacro(currentSelectedWord)) {
				Specfile spec = fEditor.getSpecfile();
				SpecfileDefine define = spec.getDefine(currentSelectedWord);
				String word = currentSelectedWord + ": "; //$NON-NLS-1$
                if (define != null) {
                	word += define.getStringValue();
                } else {
              		// If there's no such define we try to see if it corresponds to
            		// a Source or Patch declaration
            		String retrivedValue = SpecfileHover.getSourceOrPatchValue(spec, currentSelectedWord.toLowerCase());
            		if (retrivedValue != null) {
            			word += retrivedValue;
            		} else {
            			// If it does not correspond to a Patch or Source macro, try to find it
            			// in the macro proposals list.
            			retrivedValue = SpecfileHover.getMacroValueFromMacroList(currentSelectedWord);
            			if (retrivedValue != null) {
            				word += retrivedValue;
            			}
            		}
                }
                createNewAnnotations(currentSelectedWord, word, document, model);
			}
		} catch (BadLocationException e) {
			SpecfileLog.logError(e);
		}
	}

	/**
	 * Removes the previous set of annotations from the annotation model.
	 *
	 * @param model
	 *            the annotation model
	 */
	private void removeOldAnnotations(IAnnotationModel model) {
		for (Annotation annotation: fOldAnnotations) {
			model.removeAnnotation(annotation);
		}
		fOldAnnotations.clear();
	}

	/**
	 * Checks if <code>word</code> is an macro.
	 *
	 * @param word
	 *            the word to check
	 *
	 * @return <code>true</code> if <code>word</code> is an macro,
	 *         <code>false</code> otherwise
	 */
	private boolean isMacro(String word) {
		List<SpecfileDefine> defines = getMacros();
		if (word.length() > 0) {
			for (SpecfileDefine define: defines) {
				if (containsWord(define, word)) {
					return true;
				}
			}
			if (Activator.getDefault().getRpmMacroList().getProposals(
					"%" + word).size() > 0) {//$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the macros from the editor's specfile.
	 *
	 * @return the macros from the editor's specfile
	 */
	private List<SpecfileDefine> getMacros() {
		Specfile specfile = fEditor.getSpecfile();
		if (specfile != null) {
			List<SpecfileDefine> macros = specfile.getDefines();
			if (macros != null) {
				return macros;
			}
		}
		return new ArrayList<SpecfileDefine>();
	}

	/**
	 * Returns <code>true</code> if <code>macro</code> equals the word
	 * <code>current</code>.
	 *
	 * @param macro
	 *            the <code>macro</code> to check
	 * @param current
	 *            the word to look for
	 *
	 * @return <code>true</code> if <code>macro</code> contains the word
	 *         <code>current</code>,<code>false</code> if not
	 */
	private boolean containsWord(SpecfileDefine macro, String current) {
		return macro.getName().equalsIgnoreCase(current);
	}

	/**
	 * Returns the word at the current selection / caret position.
	 *
	 * @param selection
	 *            the selection
	 * @param document
	 *            the document
	 * @return the currently selected text, or the word at the caret if the
	 *         selection has length 0
	 * @throws BadLocationException
	 *             if accessing the document fails
	 */
	private String getWordAtSelection(ISelection selection, IDocument document)
			throws BadLocationException {
		String word;
		if (selection instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection) selection;
			int offset = ts.getOffset();
			int end = offset + ts.getLength();

			// non-empty selections
			if (end != offset) {
				word = ts.getText();
			} else {
				while (offset > 0 && isDefineChar(document.getChar(offset - 1))) {
					offset--;
				}
				while (end < document.getLength()
						&& isDefineChar(document.getChar(end))) {
					end++;
				}

				word = document.get(offset, end - offset);
			}
		} else {
			word = ""; //$NON-NLS-1$
		}
		return word.toLowerCase();
	}

	private boolean isDefineChar(char c) {
		return c != '{' && c != '}' && c != '?' && !Character.isWhitespace(c);
	}

	/**
	 * Adds an annotation for every occurrence of <code>macro</code> in the
	 * document. Also stores the created annotations in
	 * <code>fOldAnnotations</code>.
	 *
	 * @param macro
	 *            the word to look for
	 * @param document
	 *            the document
	 * @param model
	 *            the annotation model
	 */
	private void createNewAnnotations(String macro, String hoverContent, IDocument document,
			IAnnotationModel model) {
		String content = document.get().toLowerCase();
		int idx = content.indexOf(macro.toLowerCase());
		while (idx != -1) {
			Annotation annotation = new Annotation(ANNOTATION_TYPE, false,
					hoverContent);
			Position position = new Position(idx, macro.length());
			model.addAnnotation(annotation, position);
			fOldAnnotations.add(annotation);
			idx = content.indexOf(macro, idx + 1);
		}
	}

	public void dispose() {
		((IPostSelectionProvider) fEditor.getSelectionProvider())
				.removePostSelectionChangedListener(this);
	}
}
