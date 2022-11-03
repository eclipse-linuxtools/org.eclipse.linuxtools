/*******************************************************************************
 * Copyright (c) 2012, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;

public class DevHelpToc implements IToc {

    @Override
    public String getLabel() {
        return Messages.DevHelpToc_TocLabel;
    }

    @Override
    public String getHref() {
        return null;
    }

    @Override
    public boolean isEnabled(IEvaluationContext context) {
        return true;
    }

    @Override
    public IUAElement[] getChildren() {
        return getTopics();
    }

    @Override
    public ITopic[] getTopics() {
        List<ITopic> topics = new ArrayList<>();
        // Find all Devhelp books in the set of paths from preferences and create a
        // topic for each one
        IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
        String devhelpDirs = ps.getString(PreferenceConstants.DEVHELP_DIRECTORY);
        List<Path> books = ParseDevHelp.findAllDevhelpBooks(devhelpDirs);
        topics.addAll(books.stream().map(DevHelpTopic::new).toList());
        topics.sort((o1, o2) -> o1.getLabel().compareToIgnoreCase(o2.getLabel()));
        return topics.toArray(new ITopic[topics.size()]);
    }

    @Override
    public ITopic getTopic(String href) {
        return null;
    }
}
