/*******************************************************************************
 * Copyright (c) 2011-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 * Eric Williams <ericwill@redhat.com - modification for Javadocs
 *******************************************************************************/
package org.eclipse.linuxtools.internal.javadocs.ui;

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;

/**
 * The Toc provider class provides information about the root help document.
 */
public class JavaDocTocProvider extends AbstractTocProvider {

    @Override
    public ITocContribution[] getTocContributions(String locale) {
        ITocContribution contribution = new ITocContribution() {
            @Override
            public String getId() {
               // a way to identify our book
               return "org.eclipse.linuxtools.javadocs.toc"; //$NON-NLS-1$
            }
            @Override
            public String getCategoryId() {
               // our book does not belong to any category of books
               return null;
            }
            @Override
            public boolean isPrimary() {
               // this is a primary, top-level contribution (a book)
               return true;
            }
            @Override
            public IToc getToc() {
                return new JavaDocToc();
            }
            @Override
            public String getLocale() {
                // this provider only provides content for the en_US locale
                return "en_US"; //$NON-NLS-1$
            }
            @Override
            public String[] getExtraDocuments() {
                // there are no extra documents associated with this book
                return new String[0];
            }
            @Override
            public String getLinkTo() {
            	return JavaDocPlugin.PLUGIN_ID + "/"; //$NON-NLS-1$

            }
            @Override
            public String getContributorId() {
                return "org.eclipse.linuxtools.javadocs"; //$NON-NLS-1$
            }
        };
        return new ITocContribution[] { contribution };
    }

}
