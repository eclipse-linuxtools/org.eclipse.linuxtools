/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
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

import java.util.TreeMap;

import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;

class DevHelpSAXParser extends org.codelibs.nekohtml.sax.HTMLSAXParser {
	 private TreeMap<String, FunctionInfo> infos = new TreeMap<>();

     public DevHelpSAXParser(IDevhelpContentHandler provider) {
         super();
         this.setContentHandler(provider);
         provider.setHtmlsaxParser(this);
         
     }
    
	public TreeMap<String, FunctionInfo> getFunctionInfos() {
         return infos;
     }

}