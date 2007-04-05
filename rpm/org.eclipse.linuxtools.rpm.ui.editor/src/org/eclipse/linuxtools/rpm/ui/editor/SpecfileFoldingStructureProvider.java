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

package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;

public class SpecfileFoldingStructureProvider {

	private static final Annotation[] EMPTY = new Annotation[] {};
	private SpecfileEditor sEditor;
	private IDocument sDocument;
	private IProgressMonitor sProgressMonitor;

	public SpecfileFoldingStructureProvider(SpecfileEditor editor) {
		sEditor = editor;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		sProgressMonitor = progressMonitor;
	}

	public void setDocument(IDocument document) {
		sDocument = document;
	}

	public void updateFoldingRegions(Specfile specfile) {
		try {
			ProjectionAnnotationModel model = (ProjectionAnnotationModel) sEditor
					.getAdapter(ProjectionAnnotationModel.class);
			if (model != null)
				updateFoldingRegions(specfile, model);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void updateFoldingRegions(Specfile specfile, ProjectionAnnotationModel model)
			throws BadLocationException {
		Set/* <Position> */structure = createFoldingStructure(specfile);
		Annotation[] deletions = computeDifferences(model, structure);
		Map/* <Annotation,Position> */additions = computeAdditions(structure);
		if ((deletions.length != 0 || additions.size() != 0)
				&& (sProgressMonitor == null || !sProgressMonitor.isCanceled()))
			model.modifyAnnotations(deletions, additions, EMPTY);
	}

	private Map computeAdditions(Set currentRegions) {
		Map additionsMap = new HashMap();
		for (Iterator iter = currentRegions.iterator(); iter.hasNext();)
			additionsMap.put(new ProjectionAnnotation(), iter.next());
		return additionsMap;
	}

	private Annotation[] computeDifferences(ProjectionAnnotationModel model,
			Set current) {
		List deletions = new ArrayList();
		for (Iterator iter = model.getAnnotationIterator(); iter.hasNext();) {
			Object annotation = iter.next();
			if (annotation instanceof ProjectionAnnotation) {
				Position position = model.getPosition((Annotation) annotation);
				if (current.contains(position))
					current.remove(position);
				else
					deletions.add(annotation);
			}
		}
		return (Annotation[]) deletions
				.toArray(new Annotation[deletions.size()]);
	}

	private Set createFoldingStructure(Specfile specfile)
			throws BadLocationException {
		Set set = new HashSet();
		addFoldingRegions(set, ((Object[]) specfile.getSections()));
		return set;
	}

	private void addFoldingRegions(Set regions, Object[] elements)
			throws BadLocationException {
		Position position;
		// add folding on the preamble section
		try {
			SpecfileElement element = (SpecfileElement) elements[0];
			position = new Position(0, element.getLineStartPosition() - 1);
			regions.add(position);
		} catch (Exception exception){
			//pass
		}
		// add folding on all "simple" sections
		for (int i = 0; i < elements.length; i++) {
			SpecfileElement startElement = (SpecfileElement) elements[i];
			int offsetPos = startElement.getLineStartPosition();
			int lenghtPos;
			if (i < elements.length -1) {
				SpecfileElement endElement = (SpecfileElement) elements[i+1];
				lenghtPos = endElement.getLineStartPosition() - startElement.getLineStartPosition() - 1;
			} else {
				lenghtPos = sDocument.getLength() - startElement.getLineStartPosition();
			}
			position = new Position(offsetPos, lenghtPos);
			regions.add(position);
		}
	}
}
