/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.cdt.autotools.core.AutotoolsPlugin;

// This class would normally extend IErrorParser and use the CDT error parser
// extension.  However, we want an extended IMarker that contains library info and
// possibly other data in the future.  The standard CDT ErrorParserManager doesn't allow
// us to pass an extended ProblemMarkerInfo, so we are forced to have our own mechanism
// which is similar to the CDT one.
public class ErrorParser extends MarkerGenerator {

	public static final String ID = AutotoolsPlugin.PLUGIN_ID + ".errorParser"; //$NON-NLS-1$
	private Pattern pkgconfigError = 
		Pattern.compile(".*?(configure:\\s+error:\\s+Package requirements\\s+\\((.*?)\\)\\s+were not met).*"); //$NON-NLS-1$
	private Pattern genconfigError = 
		Pattern.compile(".*?configure:\\s+error:\\s+.*"); //$NON-NLS-1$
	private ErrorParserManager epm;
	
	public ErrorParser() {
	}

	public boolean processLine(String line, ErrorParserManager eoParser) {
		
		if (epm == null)
			epm = eoParser;
		Matcher m = pkgconfigError.matcher(line);
		if (m.matches()) {
			eoParser.generateExternalMarker(epm.getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null, null, m.group(2));
//			eoParser.generateMarker(getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null); 
			return true;
		} else {
			Matcher m2 = genconfigError.matcher(line);
			if (m2.matches()) {
				eoParser.generateMarker(epm.getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null);
				return true; 
			}
		}
		return false;
	}

	@Override
	public IProject getProject() {
		if (epm != null)
			return epm.getProject();
		return null;
	}

}
