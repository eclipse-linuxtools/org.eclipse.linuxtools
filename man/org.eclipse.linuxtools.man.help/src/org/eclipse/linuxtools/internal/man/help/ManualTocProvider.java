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

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.linuxtools.internal.man.Activator;

/**
 * Provider for help system table of contents.
 * 
 * @see ManualToc
 */
public class ManualTocProvider extends AbstractTocProvider {

    @Override
    public ITocContribution[] getTocContributions(String locale) {
        ITocContribution contribution = new ITocContribution() {
            @Override
            public String getId() {
                return getContributorId() + ".toc"; //$NON-NLS-1$
            }

            @Override
            public String getCategoryId() {
                return null;
            }

            @Override
            public boolean isPrimary() {
                return true;
            }

            @Override
            public IToc getToc() {
                return new ManualToc();
            }

            @Override
            public String getLocale() {
                return "en_US"; //$NON-NLS-1$
            }

            @Override
            public String[] getExtraDocuments() {
                return new String[0];
            }

            @Override
            public String getLinkTo() {
                return ""; //$NON-NLS-1$
            }

            @Override
            public String getContributorId() {
                return Activator.getDefault().getPluginId();
            }
        };
        return new ITocContribution[] { contribution };
    }
}
