/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.AnnotationBag;

import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;

/**
 * An extensible implementation of both <code>IAnnotationHover</code> and
 * <code>ITextHover</code> that displays the messages of {@link Annotation}s
 * that are displayed on any ruler or inline in a text viewer.
 */
public class AnnotationHover implements IAnnotationHover, ITextHover {
	/** An empty <code>null</code> object. */
	private static final IRegion NO_REGION= new Region(-1, -1);
	/**
	 * A tuple of an {@link Annotation} and its associated {@link Position}.
	 */
	protected final static class Tuple {

		/** The annotation, never <code>null</code>. */
		public final Annotation annotation;
		/** The position, never <code>null</code>. */
		public final Position position;

		/**
		 * Creates a new tuple
		 * @param annotation the annotation
		 * @param position the position
		 */
		Tuple(Annotation annotation, Position position) {
			Assert.isNotNull(annotation);
			Assert.isNotNull(position);
			this.annotation= annotation;
			this.position= position;
		}
	}
	
	/** The preference store used to get the visibility preferences. */
	private final IPreferenceStore fPreferences= EditorsUI.getPreferenceStore();
	
	/**
	 * Creates a new hover.
	 */
	public AnnotationHover() {
	}
	
	/**
	 * Returns the annotations whose positions intersect with the given region.
	 * 
	 * @param model the annotation model or <code>null</code>
	 * @param region the document region of interest
	 * @return the list of tuples whose annotations intersect with
	 *         <code>region</code>
	 */
	private List getAnnotations(IAnnotationModel model, final IRegion region) {
		if (model == null || region == NO_REGION)
			return Collections.EMPTY_LIST;

		List annotations= new ArrayList();
		Iterator iterator= model.getAnnotationIterator();
		
		while (iterator.hasNext()) {
			Annotation annotation= (Annotation) iterator.next();
			Position position= model.getPosition(annotation);
			
			if (position == null)
				continue;
			
			Tuple tuple= new Tuple(annotation, position);
			if (internalFilter(tuple, region))
				continue;
			
			if (annotation instanceof AnnotationBag) {
				AnnotationBag bag= (AnnotationBag) annotation;
				Iterator e= bag.iterator();
				while (e.hasNext()) {
					annotation= (Annotation) e.next();
					position= model.getPosition(annotation);
					if (position != null)
						annotations.add(new Tuple(annotation, position));
				}
				continue;
			} 
			
			annotations.add(tuple);
		}
		
		return annotations;
	}
	
	/**
	 * Returns <code>true</code> if the annotation should be filtered,
	 * <code>false</code> if not. Returns <code>true</code> if the position
	 * does not overlap with the hover region, the annotation is not visible, or
	 * <code>filter</code> returns true.
	 * 
	 * @param tuple a tuple of <code>&lt;Annotation, Position&gt;</code>
	 * @param region the document region of interest
	 * @return <code>true</code> if the annotation is filtered out,
	 *         <code>false</code> if it passes
	 */
	private boolean internalFilter(Tuple tuple, IRegion region) {
		return !isRegionMatch(region, tuple.position) || !isVisible(tuple.annotation) || filter(tuple, region);
	}

	/**
	 * Returns <code>true</code> if <code>annotation</code> is visible for
	 * this hover, <code>false</code> if it is not.
	 * <p>
	 * Subclasses may extend or replace. The default implementation is to return
	 * <code>true</code> if any of the <code>isVisible...</code>methods
	 * returns <code>true</code>.
	 * </p>
	 * 
	 * @param annotation the annotation to return the visibility for
	 * @return <code>true</code> if the annotation is visible, false if not
	 */
	protected boolean isVisible(Annotation annotation) {
		return isVisibleOnOverviewRuler(annotation) || isVisibleOnAnnotationRuler(annotation) || isVisibleInText(annotation);
	}
	
	/**
	 * Returns the annotation preference for the given annotation.
	 * 
	 * @param annotation the annotation
	 * @return the annotation preference or <code>null</code> if none
	 */	
	private AnnotationPreference getAnnotationPreference(Annotation annotation) {
		return EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
	}
	
	/**
	 * Returns <code>true</code> if <code>annotation</code> is visible on
	 * the overview ruler.
	 * 
	 * @param annotation the annotation to check the visibility of
	 * @return <code>true</code> if <code>annotation</code> is visible on
	 *         the overview ruler, <code>false</code> otherwise
	 */
	protected final boolean isVisibleOnOverviewRuler(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;
		
		String key= preference.getOverviewRulerPreferenceKey();
		return key != null && fPreferences.getBoolean(key);
	}
	
	/**
	 * Returns <code>true</code> if <code>annotation</code> is visible on
	 * the annotation ruler.
	 * 
	 * @param annotation the annotation to check the visibility of
	 * @return <code>true</code> if <code>annotation</code> is visible on
	 *         the annotation ruler, <code>false</code> otherwise
	 */
	protected final boolean isVisibleOnAnnotationRuler(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;
		
		String key= preference.getVerticalRulerPreferenceKey();
		return key == null || fPreferences.getBoolean(key);
	}
	
	/**
	 * Returns <code>true</code> if <code>annotation</code> is visible in
	 * the text viewer (highlighted or decorated).
	 * 
	 * @param annotation the annotation to check the visibility of
	 * @return <code>true</code> if <code>annotation</code> is visible in
	 *         the text viewer, <code>false</code> otherwise
	 */
	protected final boolean isVisibleInText(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;
		
		String key= preference.getTextPreferenceKey();
		if (key != null)
			return fPreferences.getBoolean(key);
		key= preference.getHighlightPreferenceKey();
		return key == null || fPreferences.getBoolean(key);
	}
	
	/**
	 * Returns <code>true</code> if the annotation should be filtered,
	 * <code>false</code> if not. The default returns <code>false</code>.
	 * 
	 * @param tuple a tuple of <code>&lt;Annotation, Position&gt;</code>
	 * @param region the document region of interest
	 * @return <code>true</code> if the annotation is filtered out,
	 *         <code>false</code> if it passes
	 */
	protected boolean filter(Tuple tuple, IRegion region) {
		return false;
	}
		
	/**
	 * Checks whether <code>hoverRegion</code> intersects with
	 * <code>position</code>.
	 * 
	 * @param region the region of interest, or <code>null</code>
	 * @param position the position to check for intersection, or
	 *        <code>null</code>
	 * @return <code>true</code> if <code>hoverRegion</code> and
	 *         <code>position</code> intersect, <code>false</code> if not,
	 *         or if either argument is <code>null</code>
	 */
	protected boolean isRegionMatch(IRegion region, Position position) {
		if (position == null || region == null)
			return false;
		return position.overlapsWith(region.getOffset(), region.getLength());
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public final String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IRegion region= getLineCoverage(sourceViewer, lineNumber);
		List tuples= getAnnotations(sourceViewer.getAnnotationModel(), region);
		return computeHoverMessage(tuples);
	}

	/**
	 * Returns a region describing the line <code>lineNumber</code> in the
	 * <code>IDocument</code> of <code>viewer</code>.
	 * 
	 * @param viewer the viewer to get the document from
	 * @param lineNumber the line number
	 * @return the document region describing the line <code>lineNumber</code>,
	 *         or <code>NO_REGION</code>
	 */
	private IRegion getLineCoverage(ITextViewer viewer, int lineNumber) {
		IDocument document= viewer.getDocument();
		if (document != null) {
			try {
				return document.getLineInformation(lineNumber);
			} catch (BadLocationException e) {
				return NO_REGION;
			}
		}
		return NO_REGION;
	}

	/**
	 * Caches the list of tuples computed in <code>getHoverRegion</code> for
	 * later use in <code>getHoverInfo</code>.
	 */
	private transient List fAnnotationCache;

	/*
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public final String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (fAnnotationCache == null)
			return null;
		return computeHoverMessage(fAnnotationCache);
	}

	/*
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public final IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		fAnnotationCache= null;
		if (!(textViewer instanceof ISourceViewer))
			return null;
		ISourceViewer viewer= (ISourceViewer) textViewer;
		IAnnotationModel model= viewer.getAnnotationModel();
		
		// hover region should not have length 0 to get meaningful relevances
		IRegion hoverRegion= new Region(offset, 1);
		List tuples= getAnnotations(model, hoverRegion);
		List selected= select(tuples, hoverRegion);
		IRegion coverage= computeCoverage(selected);
		if (coverage == NO_REGION)
			return null;
		fAnnotationCache= selected;
		return coverage;
	}

	/**
	 * Selects the tuples to display from the given list. Subclasses may
	 * replace.
	 * <p>
	 * The default behavior is to return the tuples whose positions most
	 * precisely match the <code>hoverRegion</code>.
	 * </p>
	 * 
	 * @param tuples the list of <code>Tuple</code>
	 * @param hoverRegion the region of interest
	 * @return the list of <code>Tuple</code>s to display
	 */
	protected List select(List tuples, IRegion hoverRegion) {
		if (tuples.isEmpty())
			return tuples;
		
		float max_relevance= Float.MIN_VALUE;
		List selected= new ArrayList();
		for (Iterator it= tuples.iterator(); it.hasNext();) {
			Tuple tuple= (Tuple) it.next();
			float relevance= computeRelevance(tuple.position, hoverRegion);
			if (relevance > max_relevance) {
				max_relevance= relevance;
				selected.clear();
				selected.add(tuple);
			} else if (relevance == max_relevance) {
				selected.add(tuple);
			}
		}
		
		return selected;
	}
	
	/**
	 * Computes the relevance of <code>position</code> with respect to the
	 * <code>hoverRegion</code>. The relevance is computed from two
	 * properties dependent on the <em>intersection</em> of
	 * <code>position</code> and <code>hoverRegion</code>:
	 * <ul>
	 * <li><strong>precision</strong> - the quotient of the
	 * <em>intersection</em> length and the <code>position</code> length</li>
	 * <li><strong>recall</strong> - the quotient of the <em>intersection</em>
	 * length and the <code>hoverRegion</code> length</li>
	 * </ul>
	 * <em>Precision</em> is considered more important than <em>recall</em>.
	 * 
	 * @param position the position
	 * @param hoverRegion the hoverRegion
	 * @return the relevance of <code>position</code> with respect to
	 *         <code>hoverRegion</code>
	 */
	private float computeRelevance(Position position, IRegion hoverRegion) {
		float intersectionLength= intersectionLength(position, hoverRegion);
		float positionLength= position.getLength();
		float sign= intersectionLength < 0 ? -1f : 1f;
		float precision= positionLength == 0 ? 1f * sign : intersectionLength / positionLength;
		float hoverRegionLength= hoverRegion.getLength();
		float recall= hoverRegionLength == 0 ? 1f * sign : intersectionLength / hoverRegionLength;
		
		float relevance= precision * 1000f + recall;
		return relevance;
	}

	/**
	 * Returns the intersection length of a position and a region. The returned
	 * value may be negative if there is no intersection.
	 * 
	 * @param position the position
	 * @param region the region
	 * @return the intersection length of <code>position</code> and
	 *         <code>region</code>, negative if they don't intersect
	 */
	private int intersectionLength(Position position, IRegion region) {
		int offset= Math.max(position.getOffset(), region.getOffset());
		int endOffset= Math.min(endOffset(position), endOffset(region));
		
		return endOffset - offset;
	}

	/**
	 * Computes the region covering all positions in the given list of tuples.
	 * 
	 * @param selected a list of <code>Tuple</code>
	 * @return the coverage of all positions
	 */
	private IRegion computeCoverage(List selected) {
		int offset= Integer.MAX_VALUE, endOffset= Integer.MIN_VALUE;
		for (Iterator it= selected.iterator(); it.hasNext();) {
			Tuple tuple= (Tuple) it.next();
			if (!tuple.position.isDeleted()) {
				offset= Math.min(tuple.position.getOffset(), offset);
				endOffset= Math.max(endOffset(tuple.position), endOffset);
			}
		}
		if (offset < Integer.MAX_VALUE && endOffset > Integer.MIN_VALUE)
			return new Region(offset, endOffset - offset);
		return NO_REGION;
	}

	/**
	 * Returns the end offset of a <code>position</code>.
	 * 
	 * @param position the position to get the end offset of
	 * @return the end offset of <code>position</code>
	 */
	private int endOffset(Position position) {
		return position.getOffset() + position.getLength();
	}

	/**
	 * Returns the end offset of a <code>region</code>.
	 * 
	 * @param region the region to get the end offset of
	 * @return the end offset of <code>region</code>
	 */
	private int endOffset(IRegion region) {
		return region.getOffset() + region.getLength();
	}
	
	/**
	 * Assembles the hover message from a list of
	 * <code>&lt;Annotation,Position&gt;</code> tuples.
	 * 
	 * @param tuples the list of tuples
	 * @return the hover message, may be <code>null</code> if there are no
	 *         messages
	 */
	private String computeHoverMessage(List tuples) {
		if (tuples.isEmpty())
			return null;
		List messages= new ArrayList();
		Iterator e= tuples.iterator();
		while (e.hasNext()) {
			Tuple tuple= (Tuple) e.next();
			String message= getMessage(tuple.annotation);
			if (message != null)
				messages.add(message.trim());
		}
		switch (messages.size()) {
			case 0:
				return null;
			case 1:
				return (String) messages.get(0);
			default:
				return formatMultipleMessages(messages);
		}
	}

	/**
	 * Returns the message text for a given annotation, or <code>null</code>
	 * if no message should be included for <code>annotation</code>.
	 * <p>
	 * Subclasses may replace. The default implementation returns the result of
	 * <code>annotation.getText()</code> if it is non-empty, <code>null</code>
	 * otherwise.
	 * </p>
	 * 
	 * @param annotation the annotation to get the message for
	 * @return the annotation's message, or <code>null</code> for no message
	 */
	protected String getMessage(Annotation annotation) {
		final String text= annotation.getText();
		return text == null || text.trim().length() == 0 ? null : text;
	}

	/**
	 * Appends a list of <code>String</code>, each on its own line
	 * 
	 * @param messages the list of messages
	 * @return a <code>String</code> with all messages appended on their own
	 *         line
	 */
	private String formatMultipleMessages(List messages) {
		StringBuffer buf= new StringBuffer();
		for (Iterator it= messages.iterator(); it.hasNext();) {
			String msg= (String) it.next();
			buf.append(msg);
			if (it.hasNext())
				buf.append('\n');
		}
		return buf.toString();
	}
}
