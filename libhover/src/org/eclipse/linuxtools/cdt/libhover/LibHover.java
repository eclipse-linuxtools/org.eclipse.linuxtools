/*******************************************************************************
 * Copyright (c) 2004, 2006, 2007, 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
/*
 * Initially created on Jul 8, 2004
 */

/**
 * @author Chris Moller, Red Hat, Inc.
 * @author Jeff Johnston, Red Hat, Inc.  (rewrite to use ICHelpProvider)
 * Modified to be org.eclipse.linuxtools.cdt.libhover package.
 */

package org.eclipse.linuxtools.cdt.libhover;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.help.IHelpResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class LibHover implements ICHelpProvider {
	
	public static String LIBHOVER_DOC_EXTENSION = LibhoverPlugin.PLUGIN_ID + ".library"; //$NON-NLS-1$

    // see comment in initialize()
    // private static String defaultSearchPath = null;
    
	private static HashMap<ICHelpBook, LibHoverLibrary> libraries = new HashMap<ICHelpBook, LibHoverLibrary>();
	
    static final String  constructTypes[] ={
    	"dtype", // $NON-NLS-1$
    	"enum",  // $NON-NLS-1$
    	"function", // $NON-NLS-1$
    	"groupsynopsis", // $NON-NLS-1$
    	"struct", // $NON-NLS-1$
    	"type",  // $NON-NLS-1$
    	"union"  // $NON-NLS-1$
    };
    
    static final int dtypeIndex         = 0;
    static final int enumIndex          = 1;
    static final int functionIndex      = 2;
    static final int groupsynopsisIndex = 3;
    static final int structIndex        = 4;
    static final int typeIndex          = 5;
    static final int unionIndex         = 6;
    
    private class LibHoverLibrary {
    	private String name;
    	private String location;
    	private String docs;
    	private Document document;
    	public LibHoverLibrary(String name, String location, String docs, Document document) {
    		this.name = name;
    		this.location = location;
    		this.document = document;
    		this.docs = docs;
    	}
    	public String getName() { 
    		return name; 
    	}
    	public String getLocation() {
    		return location;
    	}
    	public String getDocs() {
    		return docs;
    	}
    	public Document getDocument() {
    		return document;
    	}
    }

    private class HelpBook implements ICHelpBook {
    	private String title;
    	private int type;
    	
    	public HelpBook (String title, String typeName) {
    		this.title = title;
    		if (typeName.equals("C"))
    		    type = HELP_TYPE_C;
    		else if (typeName.equals("C++"))
    			type = HELP_TYPE_CPP;
    		else
    			type = HELP_TYPE_ASM;
    	}
		public String getTitle () {
			return title;
		}
		
		public int getCHelpType () {
			return type;
		}
	}

    private ArrayList<ICHelpBook> helpBooks = new ArrayList<ICHelpBook>();
    
	public void getLibHoverDocs() {
		libraries.clear();
		helpBooks.clear();
		IExtensionRegistry x = RegistryFactory.getRegistry();
		IConfigurationElement[] ces = x.getConfigurationElementsFor(LIBHOVER_DOC_EXTENSION);
		for (int i = 0; i < ces.length; ++i) {
			IConfigurationElement ce = ces[i];
			if (ce.getName().equals("library")) { //$NON-NLS-1$
				Document doc = null;
				try {
					// see comment in initialize()
					try {
						// Use the FileLocator class to open the magic hover doc file
						// in the plugin's jar.
						// Either open the html file or file system file depending
						// on what has been specified.
						String location = ce.getAttribute("location"); //$NON-NLS-1$
						String name = ce.getAttribute("name"); //$NON-NLS-1$
						String helpdocs = ce.getAttribute("docs"); //$NON-NLS-1$
						String type = ce.getAttribute("type"); //$NON-NLS-1$
						URI acDoc = new URI(location);
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
						try {
							DocumentBuilder builder = factory.newDocumentBuilder();
							doc = builder.parse(docStream);
						}
						catch (SAXParseException saxException) {
							doc = null;
						}
						catch (SAXException saxEx) {
							doc = null;
						}
						catch (ParserConfigurationException pce) {
							doc = null;
						}
						catch (IOException ioe) {
							doc = null;
						}
						// If we found the document, add it to our library list.
						if (doc != null) {
							HelpBook h = new HelpBook(name, type);
							helpBooks.add(h);
							libraries.put(h, new LibHoverLibrary(name, location, helpdocs, doc));
						}
					} catch (MalformedURLException e) {
						CUIPlugin.log(e);
					} catch (FileNotFoundException e) {
						CUIPlugin.log(e);
					} catch (URISyntaxException e) {
						CUIPlugin.log(e);
					}
				}
				catch (IOException ioe) {
				}
			}
		}
	}
	
	public void initialize() {
		getLibHoverDocs();
	}
		
	public ICHelpBook[] getCHelpBooks () {
		ICHelpBook[] chelpbooks = new ICHelpBook[helpBooks.size()];
		return helpBooks.toArray(chelpbooks);
	}
	
	private class FunctionSummary implements IFunctionSummary, Comparable<FunctionSummary> {

        private String Name;
        private String ReturnType;
        private String Prototype;
        private String Summary;
        
//        private String Synopsis;
        private class RequiredInclude implements IRequiredInclude {
        	private String include;
        	
        	public RequiredInclude (String file) {
        		include = file;
        	}
        	
        	public String getIncludeName() {
        		return include;
        	}
        	
        	public boolean isStandard() {
        		return true;
        	}
        }
        
		public int compareTo (FunctionSummary x) {
			FunctionSummary y = (FunctionSummary)x;
			return getName().compareTo(y.getName());
		}

//        private RequiredInclude Includes[];
        private ArrayList<RequiredInclude> Includes = new ArrayList<RequiredInclude>();

        private void setIncludeName (String iname) {
        	RequiredInclude nri = new RequiredInclude(iname);
        	Includes.add(nri);
        }

        public class FunctionPrototypeSummary implements IFunctionPrototypeSummary {
            public String getName()             { return Name; }
            public String getReturnType()       { return ReturnType; }
            public String getArguments()        { return Prototype; }
            public String getPrototypeString(boolean namefirst) {
                if (true == namefirst) {
                    return Name + " (" + Prototype + ") " + ReturnType; // $NON-NLS-1$ // $NON-NLS-2$
                }
                else {
                    return ReturnType + " " + Name + " (" + Prototype + ")"; // $NON-NLS-1$ // $NON-NLS-2$ // $NON-NLS-3$
                }
            }
        }

        public String getName()                         { return Name; }
        public String getNamespace()                    { return null; }
        public String getDescription()                  { return Summary; }
        public IFunctionPrototypeSummary getPrototype() { return new FunctionPrototypeSummary(); }
        
        public IRequiredInclude[] getIncludes() {
        	IRequiredInclude[] includes = new IRequiredInclude[Includes.size()];
        	for (int i = 0; i < Includes.size(); ++i) {
        		includes[i] = (IRequiredInclude)Includes.get(i);
        	}
        	return includes;
        }
        
    }
	
	protected FunctionSummary getFunctionSummaryFromNode(String name, Node function_node, Document document) {
        FunctionSummary f = new FunctionSummary();
        f.Name = name;
        NamedNodeMap function_node_map = function_node.getAttributes();
        Node function_node_returntype_node = function_node_map.item(0);
        String function_node_rt_name = function_node_returntype_node.getNodeName();

        if (function_node_rt_name.equals("returntype")) { // $NON-NLS-1$

            // return type

            String function_node_rt_value = function_node_returntype_node.getNodeValue();
            f.ReturnType = function_node_rt_value;
        }		// returntype
        
        NodeList function_node_kids = function_node.getChildNodes();
        for (int fnk = 0; fnk < function_node_kids.getLength(); fnk++) {
        	Node function_node_kid = function_node_kids.item(fnk);
            String function_node_kid_name = function_node_kid.getNodeName();
            if (function_node_kid_name.equals("prototype")) { // $NON-NLS-1$

                // prototype

                String prototype = null;

                NodeList function_node_parms = function_node_kid.getChildNodes();
                for (int fnp = 0; fnp < function_node_parms.getLength(); fnp++) {
                    Node function_node_parm = function_node_parms.item(fnp);
                    String function_node_parm_name =  function_node_parm.getNodeName();
                    if (function_node_parm_name.equals("parameter")) { // $NON-NLS-1$
                        NamedNodeMap function_node_parm_map = function_node_parm.getAttributes();
                        Node function_node_parm_node = function_node_parm_map.item(0);
                        String parameter = function_node_parm_node.getNodeValue();
                        prototype = (null == prototype)
                            ? parameter
                            : prototype + ", " + parameter;
                    }
                }
                f.Prototype = prototype;
            }	// prototype
            
            else if (function_node_kid_name.equals("headers")) { // $NON-NLS-1$

                // headers

                NodeList function_node_headers = function_node_kid.getChildNodes();
                for (int fnh = 0; fnh < function_node_headers.getLength(); fnh++) {
                    Node function_node_header = function_node_headers.item(fnh);
                    String function_node_header_name =  function_node_header.getNodeName();
                    if (function_node_header_name.equals("header")) { // $NON-NLS-1$
                        NamedNodeMap function_node_header_map = function_node_header.getAttributes();
                        Node function_node_header_node = function_node_header_map.item(0);
                        f.setIncludeName(function_node_header_node.getNodeValue());
                    }
                }
            }	// headers
            

            else if (function_node_kid_name.equals("groupsynopsis")) { // $NON-NLS-1$
            	
            	// group synopsis
            	
            	NamedNodeMap attr = function_node_kid.getAttributes();
            	Node idnode = attr.getNamedItem("id"); // $NON-NLS-1$
            	String id = idnode.getNodeValue();
				if (id != null) {
        			Element elem2 = document.getElementById(id);
        			if (null != elem2) {
        				NodeList synopsisNode = elem2.getElementsByTagName("synopsis"); // $NON-NLS-1$
        				if (null != synopsisNode && synopsisNode.getLength() > 0) {
        					Node synopsis = synopsisNode.item(0);
        					Node textNode = synopsis.getLastChild();
        					f.Summary = textNode.getNodeValue();
        				}
        			}
				}
            }
            
            else if (function_node_kid_name.equals("synopsis")) { // $NON-NLS-1$

                // synopsis

                Node textNode = function_node_kid.getLastChild();
                f.Summary =  textNode.getNodeValue();
            }
        }
        return f;
	}
	
	
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
        FunctionSummary f;

        f = null;
        
        // Loop through all the documents we have and report first match.
        for (int i = 0; i < helpBooks.length; ++i) {
        	LibHoverLibrary l = libraries.get(helpBooks[i]);
        	Document document = l != null ? l.getDocument() : null;
        	if ((null != document) && (null != name)) {
        		String sss;

        		for (int ci = 0; ci < constructTypes.length; ci++) {
        			sss = constructTypes[ci] + "-" + name; // $NON-NLS-1$
        			Element elem = document.getElementById(sss);
        			if (null != elem) {
        				switch(ci) {
        				case dtypeIndex:
        					break;
        				case enumIndex:
        					break;
        				case functionIndex:
        					NodeList functionNode = elem.getElementsByTagName("function"); // $NON-NLS-1$
        					if (null != functionNode) {
        						for (int fni = 0; fni < functionNode.getLength(); fni++) {
        							Node function_node = functionNode.item(fni);
        							String function_node_name = function_node.getNodeName();
        							if (function_node_name.equals("function")) { // $NON-NLS-1$
        								f = getFunctionSummaryFromNode(name, function_node, document);
        								return f;
        							}			// function node
        						}				// fni loop
        					}					// null != functionNode
        					break;
        				case structIndex:
        					break;
        				case typeIndex:
        					break;
        				case unionIndex:
        					break;
        				}
        			}
        		}
        	}
        }
        return null;
	}
                
 	
	public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String prefix) {
		ArrayList<IFunctionSummary> fList = new ArrayList<IFunctionSummary>();

		for (int di = 0; di < helpBooks.length; ++di) {
			LibHoverLibrary l = libraries.get(helpBooks[di]);
			Document document = l != null ? l.getDocument() : null;
			if ((null != document) && (null != prefix)) {
				NodeList elems = document.getElementsByTagName("construct"); // $NON-NLS-1$
				for (int i = 0; i < elems.getLength(); ++i) {
					Element elem = (Element)elems.item(i);
					NamedNodeMap attrs = elem.getAttributes();
					Node id_node = attrs.item(0);
					String elemName = id_node.getNodeValue();
					if (elemName != null && elemName.startsWith("function-")) { // $NON-NLS-1$
						String funcName = elemName.substring(9);
						if (funcName != null && funcName.startsWith(prefix)) {
							NodeList functionNodes = elem.getElementsByTagName("function"); // $NON-NLS-1$
							for (int j = 0; j < functionNodes.getLength(); ++j) {
								Node function_node = functionNodes.item(j);
								FunctionSummary f = getFunctionSummaryFromNode(funcName, function_node, document);
								fList.add(f);
							}
						}
					}
				}
			}
		}
		IFunctionSummary[] summaries = new IFunctionSummary[fList.size()];
		for (int k = 0; k < summaries.length; k++) {
			summaries[k] = (IFunctionSummary)fList.get(k);
		}
		Arrays.sort(summaries);
		return summaries;
	}
	
	private class HelpResource implements IHelpResource {
		private String href;
		private String label;
		public HelpResource(String href, String label) {
			this.href = href;
			this.label = label;
		}
		public String getHref() {
			return href;
		}
		public String getLabel() {
			return label;
		}
	}
	
	private class HelpResourceDescriptor implements ICHelpResourceDescriptor {
		private ICHelpBook helpbook;
		
		public HelpResourceDescriptor(ICHelpBook helpbook) {
			this.helpbook = helpbook;
		}
		
		public ICHelpBook getCHelpBook() {
			return helpbook;
		}
		
		public IHelpResource[] getHelpResources() {
			LibHoverLibrary l = libraries.get(helpbook);
			if (l != null) {
				IHelpResource[] hr = new IHelpResource[1];
				hr[0] = new HelpResource(l.getLocation(), l.getName());
				return hr;
			}
			return null;
		}
	}
	
	public ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
		for (int i = 0; i < helpBooks.length; ++i) {
			IFunctionSummary fs = getFunctionInfo(context, new ICHelpBook[]{helpBooks[i]}, name);
			if (fs != null) {
				return new HelpResourceDescriptor[]{new HelpResourceDescriptor(helpBooks[i])};
			}
		}
		return null;
	}
}
