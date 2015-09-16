/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * Copyright (c) 2015 Ezchip Semiconductor
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EZchip Semiconductor - adding support for Doxygen XML files as input
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.utils;

import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CXmlLibhoverGen extends LibhoverInfoGenerator{
	
	public CXmlLibhoverGen(Document document){
		this.document = document;
	}

	private FunctionInfo getFunctionInfoFromNode(String name, Node functionNode, Document document) {
        FunctionInfo f = new FunctionInfo(name);
        NamedNodeMap functionNodeMap = functionNode.getAttributes();
        Node functionNodeReturntypeNode = functionNodeMap.item(0);
        String functionNodeRtName = functionNodeReturntypeNode.getNodeName();

        if (functionNodeRtName.equals("returntype")) { //$NON-NLS-1$

            // return type

            String functionNodeRtValue = functionNodeReturntypeNode.getNodeValue();
            f.setReturnType(functionNodeRtValue);
        }        // returntype

        NodeList kids = functionNode.getChildNodes();
        for (int fnk = 0; fnk < kids.getLength(); fnk++) {
            Node kid = kids.item(fnk);
            String kidName = kid.getNodeName();
            if (kidName.equals("prototype")) { //$NON-NLS-1$

                // prototype

                String prototype = null;

                NodeList parms = kid.getChildNodes();
                for (int fnp = 0; fnp < parms.getLength(); fnp++) {
                    Node parm = parms.item(fnp);
                    String parmName =  parm.getNodeName();
                    if (parmName.equals("parameter")) { //$NON-NLS-1$
                        NamedNodeMap parmMap = parm.getAttributes();
                        Node parmNode = parmMap.item(0);
                        String parameter = parmNode.getNodeValue();
                        prototype = (null == prototype) ? parameter : prototype
                                + ", " + parameter; //$NON-NLS-1$
                    }
                }
                f.setPrototype(prototype);
            }    // prototype

            else if (kidName.equals("headers")) { //$NON-NLS-1$

                // headers

                NodeList headers = kid.getChildNodes();
                for (int fnh = 0; fnh < headers.getLength(); fnh++) {
                    Node header = headers.item(fnh);
                    String headerName =  header.getNodeName();
                    if (headerName.equals("header")) { //$NON-NLS-1$
                        NamedNodeMap headerMap = header.getAttributes();
                        Node headerNode = headerMap.item(0);
                        f.addHeader(headerNode.getNodeValue());
                    }
                }
            }    // headers


            else if (kidName.equals("groupsynopsis")) { //$NON-NLS-1$

                // group synopsis

                NamedNodeMap attr = kid.getAttributes();
                Node idnode = attr.getNamedItem("id"); //$NON-NLS-1$
                String id = idnode.getNodeValue();
                if (id != null) {
                    Element elem2 = document.getElementById(id);
                    if (null != elem2) {
                        NodeList synopsisNode = elem2.getElementsByTagName("synopsis"); //$NON-NLS-1$
                        if (null != synopsisNode && synopsisNode.getLength() > 0) {
                            Node synopsis = synopsisNode.item(0);
                            Node textNode = synopsis.getLastChild();
                            f.setDescription(textNode.getNodeValue());
                        }
                    }
                }
            } else if (kidName.equals("synopsis")) { //$NON-NLS-1$
                // synopsis
                Node textNode = kid.getLastChild();
                f.setDescription(textNode.getNodeValue());
            }
        }
        return f;
    }

	@Override
	protected LibHoverInfo doGenerate() {
		LibHoverInfo hoverInfo = new LibHoverInfo();
		
        NodeList nl = document.getElementsByTagName("construct"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            NamedNodeMap m = n.getAttributes();
            Node id = m.getNamedItem("id"); //$NON-NLS-1$
            if (id != null && id.getNodeValue().startsWith("function-")) { //$NON-NLS-1$
                String name = id.getNodeValue().substring(9);
                NodeList nl2 = n.getChildNodes();
                for (int j = 0; j < nl2.getLength(); ++j) {
                    Node n2 = nl2.item(j);
                    if (n2.getNodeName().equals("function")) { //$NON-NLS-1$
                        FunctionInfo f = getFunctionInfoFromNode(name, n2, document);
                        hoverInfo.functions.put(name, f);
                    }
                }
            }
        }
        
        return hoverInfo;
	}

}
