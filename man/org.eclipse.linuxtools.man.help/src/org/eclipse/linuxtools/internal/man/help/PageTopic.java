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

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.linuxtools.internal.man.Activator;

/**
 * A topic for an individual manual page.
 */
public class PageTopic implements ITopic, Comparable<PageTopic> {

    private final String sectionId;
    private final String pageId;

    /**
     * Create a topic for the given manual page.
     * 
     * @param sectionId
     *            a section identifier for the manual section in which the given
     *            page lives
     * @param pageId
     *            the identifier of the manual page
     */
    public PageTopic(String sectionId, String pageId) {
        this.sectionId = sectionId;
        this.pageId = pageId;
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
        // This replacement hack is a workaround for
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=417222
        String pageUrl = pageId;
        pageUrl = pageUrl.replaceAll("\\[", "LBRACKET"); //$NON-NLS-1$ //$NON-NLS-2$
        pageUrl = pageUrl.replaceAll("\\]", "RBRACKET"); //$NON-NLS-1$ //$NON-NLS-2$

        return "/" + Activator.getDefault().getPluginId() + "/" //$NON-NLS-1$ //$NON-NLS-2$
                + sectionId + "/" + pageUrl + ".html"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public String getLabel() {
        return pageId;
    }

    @Override
    public ITopic[] getSubtopics() {
        return new ITopic[0];
    }

    @Override
    public int compareTo(PageTopic o) {
        return pageId.compareTo(o.pageId);
    }
}
