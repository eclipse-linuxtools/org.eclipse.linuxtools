/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.javadocs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.javadocs.ui.JavaDocPlugin;
import org.eclipse.linuxtools.internal.javadocs.ui.JavaDocToc;
import org.eclipse.linuxtools.internal.javadocs.ui.JavaDocTocProvider;
import org.eclipse.linuxtools.internal.javadocs.ui.preferences.PreferenceConstants;
import org.junit.Test;

public class CheckJavadoc {

    /**
    * 	Create a valid Toc (topic contribution) using JavaDocTocProvider
    * 	@result  A valid Toc will be created, with all necessary attributes
    *            created properly: locale, ID, and linkTo.
    */
    @Test
    public void testTocProvider() {
        JavaDocTocProvider provider = new JavaDocTocProvider();
        ITocContribution[] contributions = provider
                .getTocContributions("en_US"); //$NON-NLS-1$

        assertTrue(contributions.length == 1);
        ITocContribution con = contributions[0];

        assertEquals(con.getId(),
                "org.eclipse.linuxtools.javadocs.toc"); //$NON-NLS-1$

        assertTrue(con.isPrimary());
        assertEquals(con.getCategoryId(), null);
        assertEquals(con.getLocale(), "en_US"); //$NON-NLS-1$

        String[] docs = con.getExtraDocuments();
        assertEquals(docs.length, 0);
        assertTrue(con.getLinkTo().equals("org.eclipse.linuxtools.javadocs/"));
        assertEquals(con.getContributorId(),
                "org.eclipse.linuxtools.javadocs"); //$NON-NLS-1$
    }

    /**
     * 	Create a valid topic file given the sample Javadoc files provided
     *  in the /javadoc_root directory.
     *  @throws IOException
     * 	@result  A valid topic will be created with the proper href and label.
     *           Additionally this topic will be enabled correctly and will
     *           contain no child topics (subtopics).
     *
     */
    @Test
    public void testHelpTopic() throws IOException {

    	// Get current directory and append our test doc directory to it.
    	// Set plugin preferences to read/detect Javadocs from this directory
    	// instead.
    	String testDocLoc = "/javadoc_root";
    	String dir = FileLocator.toFileURL(
                this.getClass().getResource(testDocLoc)).getPath();
        IPath wsRoot = new Path(dir);
        IPreferenceStore ps = JavaDocPlugin.getDefault().getPreferenceStore();
        ps.setValue(PreferenceConstants.JAVADOCS_DIRECTORY, wsRoot.toOSString());
        JavaDocToc toc = new JavaDocToc();

        // Get topics array
        ITopic[] topics = toc.getTopics();

        // Verify we have the test topic, in this case jnr-unixsocket
        assertTrue(topics.length > 0);
        assertTrue(topics.length == 1);

        // Set topics and check labels
        ITopic topicJnr = topics[0];

        assertTrue(topicJnr.getLabel().startsWith("jnr-unixsocket")); //$NON-NLS-1$

        // Get subtopics for each topic, should be 0 since
        // Javadocs directories are only one level deep
        ITopic[] subtopicsJnr = topicJnr.getSubtopics();

        assertTrue(subtopicsJnr.length == 0);
        IUAElement[] elementsJnr = topicJnr.getChildren();
        assertTrue(elementsJnr.length == 0);


        // Get hrefs and check for correctness according to Plugin-ID
        // relative path
        String hrefApache = topicJnr.getHref();
        assertEquals(hrefApache,
                "org.eclipse.linuxtools.javadocs/jnr-unixsocket/index.html"); //$NON-NLS-1$

        // Check to make sure topics are enables (should always be true)
        // regardless of input
        assertTrue(topicJnr.isEnabled(null));

    }
}
