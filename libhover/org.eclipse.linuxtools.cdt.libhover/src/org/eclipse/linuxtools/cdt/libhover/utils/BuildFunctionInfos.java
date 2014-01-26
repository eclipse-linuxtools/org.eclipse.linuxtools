/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BuildFunctionInfos {

	private Document document;
	private LibHoverInfo hoverInfo = new LibHoverInfo();

	public BuildFunctionInfos(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	protected FunctionInfo getFunctionInfoFromNode(String name, Node function_node, Document document) {
        FunctionInfo f = new FunctionInfo(name);
        NamedNodeMap function_node_map = function_node.getAttributes();
        Node function_node_returntype_node = function_node_map.item(0);
        String function_node_rt_name = function_node_returntype_node.getNodeName();

        if (function_node_rt_name.equals("returntype")) { //$NON-NLS-1$

            // return type

            String function_node_rt_value = function_node_returntype_node.getNodeValue();
            f.setReturnType(function_node_rt_value);
        }		// returntype

        NodeList function_node_kids = function_node.getChildNodes();
        for (int fnk = 0; fnk < function_node_kids.getLength(); fnk++) {
        	Node function_node_kid = function_node_kids.item(fnk);
            String function_node_kid_name = function_node_kid.getNodeName();
            if (function_node_kid_name.equals("prototype")) { //$NON-NLS-1$

                // prototype

                String prototype = null;

                NodeList function_node_parms = function_node_kid.getChildNodes();
                for (int fnp = 0; fnp < function_node_parms.getLength(); fnp++) {
                    Node function_node_parm = function_node_parms.item(fnp);
                    String function_node_parm_name =  function_node_parm.getNodeName();
                    if (function_node_parm_name.equals("parameter")) { //$NON-NLS-1$
                        NamedNodeMap function_node_parm_map = function_node_parm.getAttributes();
                        Node function_node_parm_node = function_node_parm_map.item(0);
                        String parameter = function_node_parm_node.getNodeValue();
                        prototype = (null == prototype)
                            ? parameter
                            : prototype + ", " + parameter; //$NON-NLS-1$
                    }
                }
                f.setPrototype(prototype);
            }	// prototype

            else if (function_node_kid_name.equals("headers")) { //$NON-NLS-1$

                // headers

                NodeList function_node_headers = function_node_kid.getChildNodes();
                for (int fnh = 0; fnh < function_node_headers.getLength(); fnh++) {
                    Node function_node_header = function_node_headers.item(fnh);
                    String function_node_header_name =  function_node_header.getNodeName();
                    if (function_node_header_name.equals("header")) { //$NON-NLS-1$
                        NamedNodeMap function_node_header_map = function_node_header.getAttributes();
                        Node function_node_header_node = function_node_header_map.item(0);
                        f.addHeader(function_node_header_node.getNodeValue());
                    }
                }
            }	// headers


            else if (function_node_kid_name.equals("groupsynopsis")) { //$NON-NLS-1$

            	// group synopsis

            	NamedNodeMap attr = function_node_kid.getAttributes();
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
            }

            else if (function_node_kid_name.equals("synopsis")) { //$NON-NLS-1$

                // synopsis

                Node textNode = function_node_kid.getLastChild();
                f.setDescription(textNode.getNodeValue());
            }
        }
        return f;
	}

	public void buildCPPInfo(String fileName) {
		Document document = getDocument();
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
		try (FileOutputStream f = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(f)){
			// Now, output the LibHoverInfo for caching later
			out.writeObject(hoverInfo);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args args[0] - URL or file name of xml document to parse
	 *        args[1] - file name to place resultant serialized LibHoverInfo
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
			if (doc != null) {
				BuildFunctionInfos d = new BuildFunctionInfos(doc);
				d.buildCPPInfo(args[1]);
			}
			System.out.println("Built " + args[1] + " from " + args[0]); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

	}

}
