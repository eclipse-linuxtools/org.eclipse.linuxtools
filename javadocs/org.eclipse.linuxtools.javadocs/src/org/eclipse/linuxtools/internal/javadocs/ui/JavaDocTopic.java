/*******************************************************************************
 * Copyright (c) 2012-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 * Eric Williams <ericwill@redhat.com> - modification for Javadocss
 *******************************************************************************/
package org.eclipse.linuxtools.internal.javadocs.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

/**
 * The Topic class is for the subtopics within the root topic. In this case,
 * topics generated will correspond to directories within /usr/share/javadoc
 * (or JAVADOCS_DIRECTORY is set to).
 */
public class JavaDocTopic implements ITopic {

    private String name;
    private String label;
    private String link;
    private List<ITopic> subTopics;

    JavaDocTopic(String name) {

    	this.name = name;
        this.subTopics = new ArrayList<>();
        this.label = name;
        // For Javadocs, every directory is only one level deep, so we can hard
        // code the link of this directory to index.html.
        link = "index.html"; //$NON-NLS-1$
    }


    @Override
    public boolean isEnabled(IEvaluationContext context) {

    	// Must always be true, otherwise it will not be displayed
    	return true;
    }

    @Override
    public IUAElement[] getChildren() {
        return getSubtopics();
    }


    @Override
    public String getHref() {

    	// WARNING: the documentation for this method allows for a "string
    	// representation of URI to an external document", such as:
    	// 		jar:file:/c:/my%20sources/src.zip!/mypackage/MyClass.html
    	// However providing an absolute path to the file system will cause
    	// errors due to a bug!
    	// Follow the format of pluginID/path_to_file.html for now.
    	return JavaDocPlugin.PLUGIN_ID + "/" + name + "/" + link; //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ITopic[] getSubtopics() {
    	return subTopics.toArray(new ITopic[0]);
    }
}