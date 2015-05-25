/*******************************************************************************
 * Copyright (c) 2012-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 * Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/
package org.eclipse.linuxtools.internal.javadocs.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.javadocs.ui.preferences.PreferenceConstants;

/**
 * The Toc class generates the root topic (i.e. the "Javadoc documents")
 * heading which will be clickable in the Eclipse help system.
 */
public class JavaDocToc implements IToc {

    @Override
    public String getLabel() {

    	return "Javadoc documents";
    }

    @Override
    public String getHref() {
    	return JavaDocPlugin.PLUGIN_ID + "/"; //$NON-NLS-1$

    }

    @Override
    public boolean isEnabled(IEvaluationContext context) {
        // Should always be true, other Eclipse will not find the help resource
    	return true;
    }

    @Override
    public IUAElement[] getChildren() {
        return getTopics();
    }

    @Override
    public ITopic[] getTopics() {
        try {
        	// Finds the path to the javadoc directory from the preference
        	// store, and gets all children directories.

            ArrayList<ITopic> topics = new ArrayList<>();
            IPreferenceStore ps = JavaDocPlugin.getDefault()
                    .getPreferenceStore();
            IPath javadocLocation = new Path(
                    ps.getString(PreferenceConstants.JAVADOCS_DIRECTORY));
            IFileSystem fs = EFS.getLocalFileSystem();
            IFileStore htmlDir = fs.getStore(javadocLocation);
            IFileStore[] files = htmlDir.childStores(EFS.NONE, null);
            Arrays.sort(files, new Comparator<IFileStore>() {

                @Override
                public int compare(IFileStore arg0, IFileStore arg1) {
                    return (arg0.getName().compareToIgnoreCase(arg1.getName()));
                }

            });

            // Loops through all children directories within the javadoc
            // directory and generates topics for them.
            for (IFileStore file: files) {
                String name = file.fetchInfo().getName();
                if (fs.getStore(
                        javadocLocation.append(name)) //$NON-NLS-1$
                        .fetchInfo().exists() && fs.getStore(
                                javadocLocation.append(name + "/index.html")) //$NON-NLS-1$
                                .fetchInfo().exists() ) {
                    ITopic topic = new JavaDocTopic(name);
                    topics.add(topic);


                }
            }

            // Returns an array of the generated topics
            ITopic[] retval = new ITopic[topics.size()];
            return topics.toArray(retval);

        }

        catch (CoreException e) {

        }

        // If no child directories exist, the root "Javadoc documents" help
        // resource will not be displayed in the help system until there are
    	// child directories present.
        return null;
    }

    @Override
    public ITopic getTopic(String href) {

    	if (href == null){
    		return null;

    	}

    	// The Eclipse help system appends and extra "/" to the beginning of
    	// every path so we need to strip it before we can do a search.
    	ITopic[] topicList = this.getTopics();
    	for (ITopic iTopic : topicList) {

    		if (iTopic.getHref().equals(href.substring(1)) ||
    				iTopic.getHref().equals(href)){

    			return iTopic;

    		}

    	}

    	return null;
    }

}
