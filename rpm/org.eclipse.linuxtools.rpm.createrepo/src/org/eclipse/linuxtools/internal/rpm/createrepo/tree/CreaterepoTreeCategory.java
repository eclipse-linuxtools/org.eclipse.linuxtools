/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Categories and tags used by the tree in the metadata page.
 */
public class CreaterepoTreeCategory {

	private String name;
	private List<String> tags = new ArrayList<String>();

	/**
	 * Default constructor to initialize name of the
	 * category at instantiation.
	 *
	 * @param name Name of category.
	 */
	public CreaterepoTreeCategory(String name) {
		if (name != null && !name.isEmpty()) {
			this.name = name;
		}
	}

	/**
	 * @return The category name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param tag The tag to remove from the category.
	 */
	public void removeTag(String tag) {
		if (tags.contains(tag)) {
			tags.remove(tag);
		}
	}

	/**
	 * @param tag A unique tag to add to the category.
	 */
	public void addTag(String tag) {
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
	}

	/**
	 * @param tags A list of tags to add to the category.
	 */
	public void addAllTags(List<String> tags) {
		for (String tag : tags) {
			addTag(tag);
		}
	}

	/**
	 * @return The tags in the category.
	 */
	public List<String> getTags() {
		return tags;
	}

}
