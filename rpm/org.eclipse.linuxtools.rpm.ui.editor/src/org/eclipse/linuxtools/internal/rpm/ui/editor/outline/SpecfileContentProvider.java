/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackageContainer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpecfileContentProvider implements ITreeContentProvider {

	private IDocumentProvider documentProvider;
	private Specfile specfile;
	private SpecfileEditor specEditor;
	protected static final String SECTION_POSITIONS = "section_positions"; //$NON-NLS-1$
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(
			SECTION_POSITIONS);

	public SpecfileContentProvider(ITextEditor editor) {
		if (editor instanceof SpecfileEditor) {
			specEditor = (SpecfileEditor) editor;
			specfile = specEditor.getSpecfile();
		}
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
				if (specEditor != null) {
					specfile = specEditor.getSpecfile();
				}
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
		} else if (parentElement instanceof SpecfilePackageContainer) {
			return ((SpecfilePackageContainer) parentElement).getPackages();
		} else if (parentElement instanceof SpecfilePackage) {
			return ((SpecfilePackage) parentElement).getSections();
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
		} else if (element instanceof SpecfilePackageContainer) {
			return ((SpecfilePackageContainer) element).hasChildren();
		} else if (element instanceof SpecfilePackage) {
			return ((SpecfilePackage) element).hasChildren();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return this.getChildren(specfile);
	}

}
