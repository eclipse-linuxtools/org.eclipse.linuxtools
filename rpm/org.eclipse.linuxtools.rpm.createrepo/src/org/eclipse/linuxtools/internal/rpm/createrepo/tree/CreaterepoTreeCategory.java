/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
    private List<String> tags = new ArrayList<>();

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
