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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;

public class SpecfileFoldingStructureProvider {


	private static class ElementByLineNbrComparator implements Comparator<SpecfileElement> {
		public int compare(SpecfileElement element1, SpecfileElement element2) {
			Integer lineNbr1 = element1.getLineNumber();
			Integer lineNbr2 = element2.getLineNumber();
			return lineNbr1.compareTo(lineNbr2);
		}
	}

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

	public void updateFoldingRegions() {
		ProjectionAnnotationModel model = (ProjectionAnnotationModel) sEditor
				.getAdapter(ProjectionAnnotationModel.class);
		if (model != null) {
			updateFoldingRegions(model);
		}
	}

	void updateFoldingRegions(ProjectionAnnotationModel model) {
		Set<Position> structure = createFoldingStructure(sEditor.getSpecfile());
		Annotation[] deletions = computeDifferences(model, structure);
		Map<Annotation,Position> additions = computeAdditions(structure);
		if ((deletions.length != 0 || !additions.isEmpty())
				&& (sProgressMonitor == null || !sProgressMonitor.isCanceled())) {
			model.modifyAnnotations(deletions, additions, EMPTY);
		}
	}

	private Map<Annotation,Position> computeAdditions(Set<Position> currentRegions) {
		Map<Annotation,Position> additionsMap = new HashMap<Annotation,Position>();
		for (Position position: currentRegions) {
			additionsMap.put(new ProjectionAnnotation(), position);
		}
		return additionsMap;
	}

	private Annotation[] computeDifferences(ProjectionAnnotationModel model,
			Set<Position> current) {
		List<Annotation> deletions = new ArrayList<Annotation>();
		for (Iterator<Annotation> iter = model.getAnnotationIterator(); iter.hasNext();) {
			Annotation annotation = iter.next();
			if (annotation instanceof ProjectionAnnotation) {
				Position position = model.getPosition(annotation);
				if (current.contains(position)) {
					current.remove(position);
				} else {
					deletions.add(annotation);
				}
			}
		}
		return deletions.toArray(new Annotation[deletions.size()]);
	}

	private Set<Position> createFoldingStructure(Specfile specfile) {
		List<SpecfileElement> elements = new ArrayList<SpecfileElement>();
		elements.addAll(specfile.getSections());
		elements.addAll(specfile.getComplexSections());
		Collections.sort(elements, new ElementByLineNbrComparator());
		return addFoldingRegions(elements);
	}

	private Set<Position> addFoldingRegions(List<SpecfileElement> elements) {
		Set<Position> regions = new HashSet<Position>();
		// add folding on the preamble section
		Position position;
		if (elements.size() > 0) {
			SpecfileElement element = elements.get(0);
			position = new Position(0, element.getLineStartPosition() - 1);
			regions.add(position);
		}

		for (int i = 0; i < elements.size(); i++) {
			SpecfileElement startElement = elements.get(i);
			int offsetPos = startElement.getLineStartPosition();
			int lenghtPos;
			if (i < elements.size() -1) {
				SpecfileElement endElement = elements.get(i+1);
				lenghtPos = endElement.getLineStartPosition() - startElement.getLineStartPosition() - 1;
			} else {
				lenghtPos = sDocument.getLength() - startElement.getLineStartPosition();
			}
			position = new Position(offsetPos, lenghtPos);
			regions.add(position);
		}
		return regions;
	}
}
