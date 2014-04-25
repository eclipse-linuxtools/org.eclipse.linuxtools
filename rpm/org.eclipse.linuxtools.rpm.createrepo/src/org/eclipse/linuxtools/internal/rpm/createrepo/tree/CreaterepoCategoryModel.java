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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoConstants;

/**
 * The category and tags (if any) of the metadata tree.
 */
public class CreaterepoCategoryModel {

    private IEclipsePreferences projectPreferences;

    public CreaterepoCategoryModel(CreaterepoProject project) {
        if (project != null) {
            projectPreferences = project.getEclipsePreferences();
        }
    }

    /**
     * The only categories acceptable by createrepo. The categories will be populated with
     * tags found from the project preferences (if any).
     *
     * @return Initial categories and saved tags (if any).
     */
    public List<CreaterepoTreeCategory> getCategories() {
        List<CreaterepoTreeCategory> model = new ArrayList<>();
        CreaterepoTreeCategory distroCat = new CreaterepoTreeCategory(
                CreaterepoPreferenceConstants.PREF_DISTRO_TAG);
        distroCat.addAllTags(getDistroTags());
        CreaterepoTreeCategory contentCat = new CreaterepoTreeCategory(
                CreaterepoPreferenceConstants.PREF_CONTENT_TAG);
        contentCat.addAllTags(getContentTags());
        CreaterepoTreeCategory repoCat = new CreaterepoTreeCategory(
                CreaterepoPreferenceConstants.PREF_REPO_TAG);
        repoCat.addAllTags(getRepoTags());
        model.add(distroCat);
        model.add(contentCat);
        model.add(repoCat);
        return model;
    }

    /**
     * Get the distro tags from the project preferences.
     *
     * @return The prefered distro tags or empty if no preferences stored.
     */
    private List<String> getDistroTags() {
        if (projectPreferences != null) {
            String tagPref = projectPreferences.get(CreaterepoPreferenceConstants.PREF_DISTRO_TAG,
                    ICreaterepoConstants.EMPTY_STRING);
            if (!tagPref.isEmpty()) {
                return Arrays.asList(tagPref.split(ICreaterepoConstants.DELIMITER));
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the content tags from the project preferences.
     *
     * @return The prefered content tags or empty if no preferences stored.
     */
    private List<String> getContentTags() {
        if (projectPreferences != null) {
            String tagPref = projectPreferences.get(CreaterepoPreferenceConstants.PREF_CONTENT_TAG,
                    ICreaterepoConstants.EMPTY_STRING);
            if (!tagPref.isEmpty()) {
                return Arrays.asList(tagPref.split(ICreaterepoConstants.DELIMITER));
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the repo tags from the project preferences.
     *
     * @return The prefered repo tags or empty if no preferences stored.
     */
    private List<String> getRepoTags() {
        if (projectPreferences != null) {
            String tagPref = projectPreferences.get(CreaterepoPreferenceConstants.PREF_REPO_TAG,
                    ICreaterepoConstants.EMPTY_STRING);
            if (!tagPref.isEmpty()) {
                return Arrays.asList(tagPref.split(ICreaterepoConstants.DELIMITER));
            }
        }
        return Collections.EMPTY_LIST;
    }

}
