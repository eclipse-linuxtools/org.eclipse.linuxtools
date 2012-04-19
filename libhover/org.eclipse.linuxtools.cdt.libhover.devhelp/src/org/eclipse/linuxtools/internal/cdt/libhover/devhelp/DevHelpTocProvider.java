/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;

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
               // this is a primary, top-level contribution (a book)
               return true;
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
            	return "";
            }
            @Override
            public String getContributorId() {
            	return "org.eclipse.linuxtools.cdt.libhover.devhelp"; //$NON-NLS-1$
            }
        };
        return new ITocContribution[] { contribution };
	}

}
