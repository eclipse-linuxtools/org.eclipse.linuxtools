/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

/**
 * A topic for a manual section. This topic will contain one sub-topic for every
 * manual page in the section.
 */
public class SectionTopic implements ITopic, Comparable<SectionTopic> {

	private final String displaySectionId;
	private final String label;

	private final Set<PageTopic> pages = new HashSet<>();

	/**
	 * Create a topic for the given manual section.
	 * 
	 * @param displaySectionId
	 *            a section identifier for the purpose of grouping pages
	 *            together for display to the user, e.g.: "1", "3pm" or "5x"
	 * @param label
	 *            a human readable name for the section identifier
	 */
	public SectionTopic(String displaySectionId, String label) {
		this.displaySectionId = displaySectionId;
		this.label = label;
	}

	/**
	 * Add a manual page to this section.
	 * 
	 * @param sectionId
	 *            a section identifier for the manual section in which the given
	 *            page lives
	 * @param pageId
	 *            the identifier of the manual page
	 */
	public void addPage(String sectionId, String pageId) {
		PageTopic page = new PageTopic(sectionId, pageId);
		pages.add(page);
	}

	@Override
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	@Override
	public IUAElement[] getChildren() {
		return getSubtopics();
	}

	@Override
	public String getHref() {
		return null;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ITopic[] getSubtopics() {
		List<PageTopic> pageList = new ArrayList<>(pages);
		Collections.sort(pageList);
		return pageList.toArray(new PageTopic[pageList.size()]);
	}

	@Override
	public int compareTo(SectionTopic o) {
		return displaySectionId.compareTo(o.displaySectionId);
	}
}
