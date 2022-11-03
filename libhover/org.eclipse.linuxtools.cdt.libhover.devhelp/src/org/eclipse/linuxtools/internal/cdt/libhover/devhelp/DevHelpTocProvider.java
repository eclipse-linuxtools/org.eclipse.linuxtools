/*******************************************************************************
 * Copyright (c) 2011, 2022 Red Hat Inc. and others.
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

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.osgi.framework.FrameworkUtil;

public class DevHelpTocProvider extends AbstractTocProvider {

    @Override
    public ITocContribution[] getTocContributions(String locale) {
        ITocContribution contribution = new ITocContribution() {
            @Override
            public String getId() {
               // a way to identify our book
               return "org.eclipse.linuxtools.cdt.libhover.devhelp.toc"; //$NON-NLS-1$
            }
            @Override
            public String getCategoryId() {
               // our book does not belong to any category of books
               return null;
            }
            @Override
            public boolean isPrimary() {
               // table of contents will be embedded in a custom index page
               return false;
            }
            @Override
            public IToc getToc() {
                return new DevHelpToc();
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
                return "toc.xml#devhelp_toc"; //$NON-NLS-1$
            }
            @Override
            public String getContributorId() {
                return FrameworkUtil.getBundle(DevHelpTocProvider.class).getSymbolicName();
            }
        };
        return new ITocContribution[] { contribution };
    }
}
