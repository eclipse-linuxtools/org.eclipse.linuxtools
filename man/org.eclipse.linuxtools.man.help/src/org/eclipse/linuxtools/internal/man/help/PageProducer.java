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

import java.io.InputStream;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.linuxtools.internal.man.Activator;
import org.eclipse.linuxtools.internal.man.parser.ManParser;

/**
 * Content producer that renders manual pages in HTML format.
 */
public class PageProducer implements IHelpContentProducer {

	@Override
	public InputStream getInputStream(String pluginID, String href,
			Locale locale) {

		// Strip off the extra parameters that Eclipse help system adds, that we
		// don't care about
		String parts[];
		if (href.contains("?")) { //$NON-NLS-1$
			parts = href.substring(0, href.indexOf('?')).split("/"); //$NON-NLS-1$
		} else {
			parts = href.split("/"); //$NON-NLS-1$
		}
		if (parts == null || parts.length < 2) {
			Status status = new Status(IStatus.ERROR,
					Messages.ManPageProducer_ParseError,
					Activator.getDefault().getPluginId());
			Activator.getDefault().getLog().log(status);
			return null;
		}

		// This replacement hack is a workaround for
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417222
		String page = parts[1].substring(0, parts[1].length() - 5);
		page = page.replaceAll("LBRACKET", "["); //$NON-NLS-1$ //$NON-NLS-2$
		page = page.replaceAll("RBRACKET", "]"); //$NON-NLS-1$ //$NON-NLS-2$

		return new ManParser().getManPage(page, true, parts[0]);
	}
}
