/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackageContainer;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpecfileContentProvider implements ITreeContentProvider {

	private IDocumentProvider documentProvider;
	private Specfile specfile;
	protected static final String SECTION_POSITIONS = "section_positions"; //$NON-NLS-1$
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(SECTION_POSITIONS);

	public SpecfileContentProvider(ITextEditor editor) {
		specfile = new SpecfileParser().parse(editor.getDocumentProvider().getDocument(editor.getEditorInput()));
		this.documentProvider = editor.getDocumentProvider();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != null) {
			IDocument document = documentProvider.getDocument(oldInput);
			if (document != null) {
				try {
					document.removePositionCategory(SECTION_POSITIONS);
				} catch (BadPositionCategoryException x) {
				}
				document.removePositionUpdater(positionUpdater);
			}
		}

		if (newInput != null) {
			IDocument document = documentProvider.getDocument(newInput);
			if (document != null) {
				document.addPositionCategory(SECTION_POSITIONS);
				document.addPositionUpdater(positionUpdater);
				specfile = new SpecfileParser().parse(document);
			}
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == specfile) {
			int elmsSize = 1 + 1 + specfile.getSections().size();
			Object[] elms = new Object[elmsSize];
			elms[0] = specfile.getPreamble();
			Object[] sections = specfile.getSections().toArray();
			for (int i = 0; i < sections.length; i++) {
				elms[i + 1] = sections[i];
			}
			elms[elmsSize - 1] = specfile.getPackages();
			return elms;
		} else if (parentElement instanceof SpecfilePackageContainer spc) {
			return spc.getPackages();
		} else if (parentElement instanceof SpecfilePackage sp) {
			return sp.getSections();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == specfile) {
			return true;
		} else if (element instanceof SpecfilePackageContainer spc) {
			return spc.hasChildren();
		} else if (element instanceof SpecfilePackage sp) {
			return sp.hasChildren();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return this.getChildren(specfile);
	}

}
