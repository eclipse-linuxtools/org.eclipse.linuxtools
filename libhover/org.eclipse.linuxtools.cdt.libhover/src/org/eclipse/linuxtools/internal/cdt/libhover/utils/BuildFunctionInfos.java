/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     EZchip Semiconductor - adding support for Doxygen XML files as input
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class BuildFunctionInfos {
	
    private static final String IS_DOXYGEN = "--doxygen"; //$NON-NLS-1$

    /**
     * @param args args[0] - URL or file name of xml document to parse
     *        args[1] - file name to place resultant serialized LibHoverInfo
     *        args[2] - optional parameter in form of '--doxygen' whether provided XML file in doxygen format
     */
    public static void main(String[] args) {
        URI acDoc;
        try {
            acDoc = new URI(args[0]);
            IPath p = URIUtil.toPath(acDoc);
            InputStream docStream = null;
            if (p == null) {
                URL url = acDoc.toURL();
                docStream = url.openStream();
            } else {
                docStream = new FileInputStream(p.toFile());
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(docStream);
            LibhoverInfoGenerator libhoverInfoGenerator = (args.length == 3 && IS_DOXYGEN.equals(args[2]))?new CDoxygenLibhoverGen(doc):new CXmlLibhoverGen(doc);
        	libhoverInfoGenerator.generate(args[1]);
            
            System.out.println("Built " + args[1] + " from " + args[0]); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (URISyntaxException|SAXException|ParserConfigurationException|IOException e) {
            e.printStackTrace();
        }

    }
}
