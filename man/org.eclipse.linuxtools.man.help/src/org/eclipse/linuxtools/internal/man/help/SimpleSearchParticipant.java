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

import java.net.URL;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.search.IHelpSearchIndex;
import org.eclipse.help.search.ISearchDocument;
import org.eclipse.help.search.SearchParticipant;

/**
 * This {@link SearchParticipant} implementation adds only the title of the man
 * page to the index.
 * <p>
 * It takes way too long to index fully the tens of thousands man pages that a
 * user may have installed on their system, so for now we simply do not index
 * whole man pages.
 */
public class SimpleSearchParticipant extends SearchParticipant {

    @Override
    public IStatus addDocument(IHelpSearchIndex index, String pluginId,
            String path, URL url, String id, ISearchDocument doc) {
        String file = Paths.get(path).getFileName().toString();
        String manpage = file.substring(0, file.length() - 5);
        doc.setTitle(manpage);
        doc.setSummary(manpage);
        doc.addContents(manpage);
        return Status.OK_STATUS;
    }
}
