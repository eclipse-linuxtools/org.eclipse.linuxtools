/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.javadocs.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Test;

class CheckJavadocTest {

	/**
	 * Create a valid Toc (topic contribution) using JavaDocTocProvider. A valid Toc
	 * will be created, with all necessary attributes created properly: locale, ID,
	 * and linkTo.
	 */
    @Test
    void testTocProvider() {
        JavaDocTocProvider provider = new JavaDocTocProvider();
        ITocContribution[] contributions = provider
                .getTocContributions("en_US"); //$NON-NLS-1$

        assertEquals(1, contributions.length);
        ITocContribution con = contributions[0];

        assertEquals(con.getId(),
                "org.eclipse.linuxtools.javadocs.toc"); //$NON-NLS-1$

        assertTrue(con.isPrimary());
        assertNull(con.getCategoryId());
        assertEquals(con.getLocale(), "en_US"); //$NON-NLS-1$

        String[] docs = con.getExtraDocuments();
        assertEquals(docs.length, 0);
        assertEquals(con.getLinkTo(), "org.eclipse.linuxtools.javadocs/");
        assertEquals(con.getContributorId(),
                "org.eclipse.linuxtools.javadocs"); //$NON-NLS-1$
    }

	/**
	 * Create a valid topic file given the sample Javadoc files provided in the
	 * /javadoc_root directory. A valid topic will be created with the proper href
	 * and label. Additionally this topic will be enabled correctly and will contain
	 * no child topics (subtopics).
	 * 
	 * @throws IOException
	 *
	 */
    @Test
    void testHelpTopic() throws IOException {

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
        assertEquals(topics.length, 1);

        // Set topics and check labels
        ITopic topicJnr = topics[0];

        assertTrue(topicJnr.getLabel().startsWith("jnr-unixsocket")); //$NON-NLS-1$

        // Get subtopics for each topic, should be 0 since
        // Javadocs directories are only one level deep
        ITopic[] subtopicsJnr = topicJnr.getSubtopics();

        assertEquals(subtopicsJnr.length, 0);
        IUAElement[] elementsJnr = topicJnr.getChildren();
        assertEquals(elementsJnr.length, 0);


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
