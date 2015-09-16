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
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.cdt.libhover.ClassInfo;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.MemberInfo;
import org.eclipse.linuxtools.cdt.libhover.TypedefInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CDoxygenLibhoverGen extends LibhoverInfoGenerator{

    private static final String PROT3 = "prot";//$NON-NLS-1$
	private static final String RETURN = "return";//$NON-NLS-1$
	private static final String PARAMETERDESCRIPTION = "parameterdescription";//$NON-NLS-1$
	private static final String PARAMETERNAME = "parametername";//$NON-NLS-1$
	private static final String PARAMETERNAMELIST = "parameternamelist";//$NON-NLS-1$
	private static final String PARAMETERITEM = "parameteritem";//$NON-NLS-1$
	private static final String EXCEPTION = "exception";//$NON-NLS-1$
	private static final String DEFINITION = "definition";//$NON-NLS-1$
	private static final String TYPEDEF2 = "typedef";//$NON-NLS-1$
	private static final String REFID2 = "refid";//$NON-NLS-1$
	private static final String REF = "ref";//$NON-NLS-1$
	private static final String PUBLIC_FUNC = "public-func";//$NON-NLS-1$
	private static final String BASECOMPOUNDREF = "basecompoundref";//$NON-NLS-1$
	private static final String INCLUDES = "includes";//$NON-NLS-1$
	private static final String DECLNAME = "declname";//$NON-NLS-1$
	private static final String TEMPLATEPARAMLIST = "templateparamlist";//$NON-NLS-1$
	private static final String CLASS = "class";//$NON-NLS-1$
	private static final String PUBLIC = "public";//$NON-NLS-1$
	private static final String LOCATION = "location";//$NON-NLS-1$
	private static final String SIMPLESECT = "simplesect";//$NON-NLS-1$
	private static final String PARAMETERLIST = "parameterlist";//$NON-NLS-1$
	private static final String DETAILEDDESCRIPTION = "detaileddescription";//$NON-NLS-1$
	private static final String PARA = "para";//$NON-NLS-1$
	private static final String BRIEFDESCRIPTION = "briefdescription";//$NON-NLS-1$
	private static final String TYPE2 = "type";//$NON-NLS-1$
	private static final String PARAM = "param";//$NON-NLS-1$
	private static final String ARGSSTRING = "argsstring";//$NON-NLS-1$
	private static final String NAME3 = "name";//$NON-NLS-1$
	private static final String FUNCTION = "function";//$NON-NLS-1$
	private static final String MEMBERDEF = "memberdef";//$NON-NLS-1$
	private static final String FUNC = "func";//$NON-NLS-1$
	private static final String SECTIONDEF = "sectiondef";//$NON-NLS-1$
	private static final String COMPOUNDNAME = "compoundname";//$NON-NLS-1$
	private static final String FILE = "file";//$NON-NLS-1$
	private static final String COMPOUNDDEF = "compounddef";//$NON-NLS-1$
	private static final String TYPEDEF = "typedef ";//$NON-NLS-1$
	private Document document;
    private Map<String, ClassInfo> classesById = new HashMap<>();

    public CDoxygenLibhoverGen(Document document) {
        this.document = document;
    }

    private String[] getTypedefTypes(String def) {
        String[] result = null;
        if (def.startsWith(TYPEDEF)) { 
            int startIndex = 8;
            int count = 0;
            int i = def.length() - 1;
            // To break up types, we look for first blank outside of a template, working backwards.
            // We need to work backwards because the transformed type may contain actual numeric parameters
            // which could use the shift operators and we won't know whether they are shift operators or
            // template specifiers without some actual parsing.
            while (i >= 0) {
                char ch = def.charAt(i);
                if (ch == '<') {
                    --count;
                } else if (ch == '>') {
                    ++count;
                }
                // We look at last blank not in a template as being the delimeter between
                // type name and definition.
                if (count == 0 && ch == ' ') {
                    startIndex = i + 1;
                    break;
                }
                --i;
            }
            result = new String[2];
            result[1] = def.substring(startIndex);
            // Following is a bit of a hack knowing the docs don't add the namespace when the transformed
            // type is in the same space
            int namespace = result[1].indexOf("::"); //$NON-NLS-1$
            if (namespace < 0) {
                result[0] = def.substring(8, startIndex).trim();
            } else {
                result[0] = result[1].substring(0, namespace) + "::" + def.substring(8, startIndex).trim(); //$NON-NLS-1$
            }
        }
        return result;
    }

    private String getElementText(Node node) {
        StringBuffer d = new StringBuffer();
        NodeList nl = node.getChildNodes();
        for (int x = 0; x < nl.getLength(); ++x) {
            Node text = nl.item(x);
            if (text.getNodeType() == Node.TEXT_NODE) {
                d.append(text.getNodeValue());
            } else {
                d.append(getElementText(text));
            }
        }
        return d.toString();
    }

    private ClassInfo getClassInfo(LibHoverInfo libHoverInfo, String className) {
        String typedefName = className.replaceAll("<.*>", "<>"); //$NON-NLS-1$ //$NON-NLS-2$
        TypedefInfo typedef = libHoverInfo.typedefs.get(typedefName);
        if (typedef != null) {
            className = typedef.getTransformedType(className);  // Reset class name to typedef transformation
        }
        int index = className.indexOf('<');
        // Check if it is a template reference.
        if (index != -1) {
            // It is.  We want to see if there are partial specific templates
            // and we choose the first match.  If nothing matches our particular
            // case, we fall back on the initial generic template.
            ClassInfo info = libHoverInfo.classes.get(className.substring(0, index));
            if (info == null)
                return null;
            ArrayList<ClassInfo> children = info.getChildren();
            if (children != null && children.size() > 0) {
                for (int x = 0; x < children.size(); ++x) {
                    ClassInfo child = children.get(x);
                    if (className.matches(child.getClassName())) {
                        info = child;
                        break;
                    }
                }
            }
            return info;
        }
        // Otherwise no template, just fetch the class info directly.
        return libHoverInfo.classes.get(className);
    }

    @Override
    public LibHoverInfo doGenerate(){
    	LibHoverInfo libHoverInfo = new LibHoverInfo();
        // Create a hash table of all the class nodes mapped by class name.  Trim any template info
		// for the class name key value.
		NodeList nl = document.getElementsByTagName(COMPOUNDDEF);  
		for (int i = 0; i < nl.getLength(); ++i) {
		    Node n = nl.item(i);
		    NamedNodeMap attrs = n.getAttributes();
		    Node kind = attrs.getNamedItem("kind");  //$NON-NLS-1$
		    Node id = attrs.getNamedItem("id");  //$NON-NLS-1$
		    Node prot = attrs.getNamedItem(PROT3); 
		    
		    // C functions
		    if (id != null && kind != null && FILE.equals(kind.getNodeValue())) { 
		        NodeList nl2 = n.getChildNodes();
		        FunctionInfo fi = null;
		        String include = null;
		        for (int j = 0; j < nl2.getLength(); ++j) {
		            Node n2 = nl2.item(j);
		            String name2 = n2.getNodeName();
		           
		            if (COMPOUNDNAME.equals(name2)) { 
		            	// compoundname for a file node is the filename 
		            	// this can be a .c or .h file
		            	String filename = getElementText(n2); 
		            	if(filename.endsWith(".h")) { //$NON-NLS-1$
		            		include = filename; 
		            	}
		            } else if (SECTIONDEF.equals(name2)) {  
		                // We are only interested in functions 
		                NamedNodeMap m = n2.getAttributes();
		                if (m != null) {
		                    Node kind2 = m.getNamedItem("kind");  //$NON-NLS-1$
		                    if (kind2 != null && FUNC.equals(kind2.getNodeValue())) { 
		                        NodeList pubfuncs = n2.getChildNodes();
		                        int pubfuncLength = pubfuncs.getLength();
		                        for (int j1 = 0; j1 < pubfuncLength; ++j1) {
		                            Node n3 = pubfuncs.item(j1);
		                            // Add all public member functions to the list of members
		                            if (MEMBERDEF.equals(n3.getNodeName())) {  
		                                NamedNodeMap m3 = n3.getAttributes();
		                                if (m3 != null) {
		                                    Node m3Kind = m3.getNamedItem("kind");  //$NON-NLS-1$
		                                    if (m3Kind != null && FUNCTION.equals(m3Kind.getNodeValue())) { 
		                                        String name = null;
		                                        String type = null;
		                                        String args = null;
		                                        String desc = null;
		                                        
		                                        boolean briefDescriptionProcessed = false;
		                                        boolean detailedDescriptionProcessed = false;
                                            	boolean parameterListProcessed = false;
                                            	boolean retValProcessed = false;
                                            	boolean locationProcessed = false;
                                            	
		                                        ArrayList<String> parms = new ArrayList<>();
		                                        NodeList nl4 = n3.getChildNodes();
		                                        int memberLength = nl4.getLength();
		                                        for (int k = 0; k < memberLength; ++k) {
		                                            Node n4 = nl4.item(k);
		                                            String n4Name = n4.getNodeName();
		                                            if (TYPE2.equals(n4Name)) {  
		                                                NodeList nl5 = n4.getChildNodes();
		                                                type = "";  //$NON-NLS-1$
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (n5.getNodeType() == Node.TEXT_NODE)
		                                                        type += n5.getNodeValue();                                                                
		                                                }
		                                            } else if (NAME3.equals(n4Name)) {  
		                                                name = n4.getTextContent();
		                                            } else if (ARGSSTRING.equals(n4Name)) {  
		                                                args = getElementText(n4);
		                                            } else if (PARAM.equals(n4Name)) {  
		                                                NodeList nl5 = n4.getChildNodes();
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (TYPE2.equals(n5.getNodeName())) { 
		                                                        parms.add(getElementText(n5));
		                                                    }
		                                                }
		                                            } else if (BRIEFDESCRIPTION.equals(n4Name) && !briefDescriptionProcessed ) {  
		                                                NodeList nl5 = n4.getChildNodes();
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (PARA.equals(n5.getNodeName())) { 
		                                                        if (desc == null) {
		                                                            desc = ""; //$NON-NLS-1$
		                                                        }
		                                                        desc += "<p>" + getElementText(n5) + "</p>";   //$NON-NLS-1$ //$NON-NLS-2$
		                                                        briefDescriptionProcessed = true;
		                                                    }
		                                                }
		                                            } else if (DETAILEDDESCRIPTION.equals(n4Name)) { 		                                          
		                                                NodeList nl5 = n4.getChildNodes();
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (n5.getNodeName().equals(PARA)) {  
		                                                        if (desc == null)
		                                                            desc = new String("");  //$NON-NLS-1$
		                                                        NodeList nl6 = n5.getChildNodes();
		                                                        Node n6 = nl6.item(0);
		                                                        if (n6.getNodeType() == Node.TEXT_NODE && !detailedDescriptionProcessed){
		                                                            desc += "<p>" + getElementText(n5) + "</p>";   //$NON-NLS-1$ //$NON-NLS-2$
		                                                            detailedDescriptionProcessed = true;
		                                                        } else {
		                                                            for (int x2 = 0; x2 < nl6.getLength(); ++x2) {
		                                                                n6 = nl6.item(x2);
		                                                                if (PARAMETERLIST.equals(n6.getNodeName()) && !parameterListProcessed) { 
		                                                                    desc += getParameters(n6, false);
		                                                                    parameterListProcessed = true;
		                                                                } else if (SIMPLESECT.equals(n6.getNodeName()) & !retValProcessed) { 
		                                                                    desc += getReturn(n6);
		                                                                    retValProcessed = true;
		                                                                }
		                                                            }
		                                                        }
		                                                    }
		                                                }
		                                            } else if (LOCATION.equals(n4Name) && !locationProcessed) { 
		                                                // Location is after all descriptions so we can now add the function
		                                                if (name != null) {
		                                                	// Try to update existing function, in case information is split between .c and .h files
		                                                	fi = libHoverInfo.functions.get(name);
		                                                	if (fi == null) {
		                                                		fi = new FunctionInfo(name);
		                                                	}
		                                                	if (type != null) {
		                                                		fi.setReturnType(type);
		                                                	}  
		                                                	if (args != null) {
		                                                        // Strip ()s, as the plugin adds them back in
		                                                        if(args.charAt(0) == '(' && args.charAt(args.length() - 1) == ')')
		                                                        	fi.setPrototype(args.substring(1, args.length() - 1));
		                                                        else
		                                                        	fi.setPrototype(args);
		                                                	}
		                                                	if (desc != null) {
		                                                		fi.setDescription(desc);
		                                                	}
		                                                    if(include != null) {
		                                                    	fi.addHeader(include);
		                                                    }                                                                
		                                                    //System.out.println(name + "|" + type +  "|" + args +  "|" + desc +  "|" + include);                                                                                                                                
		                                                    libHoverInfo.functions.put(name, fi);
		                                                    locationProcessed = true;
		                                                }		                                                
		                                                break;
		                                            }
		                                        }
		                                    }
		                                }
		                            }
		                        }
		                    }
		                }
		            }
		        }
		    }
		    	                
		    // We are only interested in cataloging public classes.
		    if (id != null && prot != null && PUBLIC.equals(prot.getNodeValue()) 
		            && kind != null && CLASS.equals(kind.getNodeValue())) {  
		        NodeList nl2 = n.getChildNodes();
		        ClassInfo d = null;
		        String hashName = null;
		        for (int j = 0; j < nl2.getLength(); ++j) {
		            Node n2 = nl2.item(j);
		            String name2 = n2.getNodeName();
		            if (name2.equals(COMPOUNDNAME)) {  
		                String text = n2.getTextContent();
		                if (text != null && !text.equals("")) { //$NON-NLS-1$
		                    String className = text;
		                    text = text.replaceAll("<\\s*", "<");   //$NON-NLS-1$ //$NON-NLS-2$
		                    text = text.replaceAll("\\s*>", ">");   //$NON-NLS-1$ //$NON-NLS-2$
		                    int index = text.indexOf('<');
		                    hashName = text;
		                    if (index > 0)
		                        hashName = text.substring(0, index);
		                    d = new ClassInfo(className, n);
		                    classesById.put(id.getNodeValue(), d);
		                    ClassInfo e = libHoverInfo.classes.get(hashName);
		                    if (e != null) { /* We are dealing with a partial specific template...add it to list  */
		                        if (!d.areTemplateParmsFilled())
		                            d.setTemplateParms(getTemplateParms(n));
		                        String[] templateParms = d.getTemplateParms();
		                        // For each template parameter, replace with a generic regex so later we can compare
		                        // and identify a match (e.g. A<_a, _b> and A<char, _b> are defined and we have an instance
		                        // of A<char, int>.  We want to to match with A<char, _b> and replace all occurrences of "_b"
		                        // with "int".  For speed, we assume that the template parameter is not a subset of any
		                        // other variable (e.g. if _A is used, there is no __A or _AB).  If this proves untrue in
		                        // any instance, more refinement of the initial value to replace will be required.
		                        for (int k = 0; k < templateParms.length; ++k) {
		                            text = text.replaceAll(templateParms[k], "[a-zA-Z0-9_: *]+");  //$NON-NLS-1$
		                        }
		                        d.setClassName(text);
		                        e.addTemplate(d);
		                    }
		                    else
		                        libHoverInfo.classes.put(hashName, d);
		                }
		            } else if (TEMPLATEPARAMLIST.equals(name2)) { 
		                ArrayList<String> templates = new ArrayList<>();
		                NodeList params = n2.getChildNodes();
		                int paramsLength = params.getLength();
		                for (int j2 = 0; j2 < paramsLength; ++j2) {
		                    Node n3 = params.item(j2);
		                    if (n3.getNodeName().equals(PARAM)) { 
		                        NodeList types = n3.getChildNodes();
		                        int typesLength = types.getLength();
		                        for (int j3 = 0; j3 < typesLength; ++j3) {
		                            Node n4 = types.item(j3);
		                            if (DECLNAME.equals(n4.getNodeName())) {
		                                templates.add(getElementText(n4));
		                            }
		                        }
		                    }
		                }
		                String[] templateNames = new String[templates.size()];
		                d.setTemplateParms(templates.toArray(templateNames));
		            } else if (INCLUDES.equals(name2)) {  
		                String include = getElementText(n2);
		                if (d != null)
		                    d.setInclude(include);
		            } else if (BASECOMPOUNDREF.equals(name2)) { 
		                // We have a base class.  If public, add it to the list of nodes to look at in case we don't find the member
		                // in the current class definition.
		                NamedNodeMap m = n2.getAttributes();
		                if (m != null) {
		                    Node refid = m.getNamedItem(REFID2);  
		                    Node prot2 = m.getNamedItem(PROT3); 
		                    if (prot2 != null && PUBLIC.equals(prot2.getNodeValue())) {  
		                        ClassInfo baseClass = null;
		                        if (refid != null) {
		                            // If we have been given the id of the base class, fetch it directly
		                            baseClass = classesById.get(refid.getNodeValue());
		                        } else {
		                            // We probably have a template that needs resolution
		                            String baseClassName = n2.getTextContent();
//                                        System.out.println("base class name is " + baseClassName);
		                            baseClass = getClassInfo(libHoverInfo, baseClassName);
		                        }
		                        if (d != null && baseClass != null)
		                            d.addBaseClass(baseClass);
		                    }
		                }
		            } else if (SECTIONDEF.equals(name2)) {  
		                // We are only interested in public member functions which are in their own section.
		                NamedNodeMap m = n2.getAttributes();
		                if (m != null) {
		                    Node kind2 = m.getNamedItem("kind");  //$NON-NLS-1$
		                    if (kind2 != null && PUBLIC_FUNC.equals(kind2.getNodeValue())) { 
		                        NodeList pubfuncs = n2.getChildNodes();
		                        int pubfuncLength = pubfuncs.getLength();
		                        for (int j1 = 0; j1 < pubfuncLength; ++j1) {
		                            Node n3 = pubfuncs.item(j1);
		                            // Add all public member functions to the list of members
		                            if (MEMBERDEF.equals(n3.getNodeName())) { 
		                                NamedNodeMap m3 = n3.getAttributes();
		                                if (m3 != null) {
		                                    Node m3Kind = m3.getNamedItem("kind");  //$NON-NLS-1$
		                                    if (m3Kind != null && FUNCTION.equals(m3Kind.getNodeValue())) {
		                                        String name = null;
		                                        String type = null;
		                                        String args = null;
		                                        String desc = null;
		                                        ArrayList<String> parms = new ArrayList<>();
		                                        NodeList nl4 = n3.getChildNodes();
		                                        int memberLength = nl4.getLength();
		                                        for (int k = 0; k < memberLength; ++k) {
		                                            Node n4 = nl4.item(k);
		                                            String n4Name = n4.getNodeName();
		                                            if (TYPE2.equals(n4Name)) {  
		                                                NodeList nl5 = n4.getChildNodes();
		                                                type = "";  //$NON-NLS-1$
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (n5.getNodeType() == Node.TEXT_NODE)
		                                                        type += n5.getNodeValue();
		                                                    else if (REF.equals(n5.getNodeName())) {  
		                                                        NamedNodeMap n5m = n5.getAttributes();
		                                                        Node n5id = n5m.getNamedItem(REFID2);
		                                                        if (n5id != null) {
		                                                            String refid = n5id.getNodeValue();
		                                                            ClassInfo refClass = classesById.get(refid);
		                                                            if (refClass != null)
		                                                                type += refClass.getClassName();
		                                                        }
		                                                    }
		                                                }
		                                            } else if (NAME3.equals(n4Name)) {  
		                                                name = n4.getTextContent();
		                                            } else if (ARGSSTRING.equals(n4Name)) {  
		                                                args = getElementText(n4);
		                                            } else if (PARAM.equals(n4Name)) { 
		                                                NodeList nl5 = n4.getChildNodes();
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (TYPE2.equals(n5.getNodeName())) { 
		                                                        parms.add(getElementText(n5));
		                                                    }
		                                                }
		                                            } else if (BRIEFDESCRIPTION.equals(n4Name)) { 
		                                                NodeList nl5 = n4.getChildNodes();
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (PARA.equals(n5.getNodeName())) {  
		                                                        if (desc == null) {
		                                                            desc = ""; //$NON-NLS-1$
		                                                        }
		                                                        desc += "<p>" + getElementText(n5) + "</p>";   //$NON-NLS-1$ //$NON-NLS-2$
		                                                    }
		                                                }
		                                            } else if (DETAILEDDESCRIPTION.equals(n4Name)) {  
		                                                NodeList nl5 = n4.getChildNodes();
		                                                for (int x = 0; x < nl5.getLength(); ++x) {
		                                                    Node n5 = nl5.item(x);
		                                                    if (PARA.equals(n5.getNodeName())) {  
		                                                        if (desc == null)
		                                                            desc = new String("");  //$NON-NLS-1$
		                                                        NodeList nl6 = n5.getChildNodes();
		                                                        Node n6 = nl6.item(0);
		                                                        if (n6.getNodeType() == Node.TEXT_NODE)
		                                                            desc += "<p>" + getElementText(n5) + "</p>";   //$NON-NLS-1$ //$NON-NLS-2$
		                                                        else {
		                                                            for (int x2 = 0; x2 < nl6.getLength(); ++x2) {
		                                                                n6 = nl6.item(x2);
		                                                                if (PARAMETERLIST.equals(n6.getNodeName())) {
		                                                                    desc += getParameters(n6, true);
		                                                                } else if (SIMPLESECT.equals(n6.getNodeName())) {
		                                                                    desc += getReturn(n6);
		                                                                }
		                                                            }
		                                                        }
		                                                    }
		                                                }
		                                            } else if (LOCATION.equals(n4Name)) { 
		                                                // Location is after all descriptions so we can now add the member
		                                                if (name != null) {
		                                                    MemberInfo member = new MemberInfo(name);
		                                                    member.setReturnType(type);
		                                                    member.setPrototype(args);
		                                                    member.setDescription(desc);
		                                                    String[] argNames = new String[parms.size()];
		                                                    member.setParamTypes(parms.toArray(argNames));
		                                                    d.addMember(member);
		                                                }
		                                                break;
		                                            }
		                                        }
		                                    }
		                                }
		                            }
		                        }
		                    }
		                }
		            }
		        }
		    }
		}
		// Create a hash table of all the typedefs.  Keep any template info.
		nl = document.getElementsByTagName(MEMBERDEF);  
		for (int i = 0; i < nl.getLength(); ++i) {
		    Node n = nl.item(i);
		    NamedNodeMap attrs = n.getAttributes();
		    if (attrs != null) {
		        Node kind = attrs.getNamedItem("kind");  //$NON-NLS-1$
		        Node prot = attrs.getNamedItem(PROT3); 
		        if (kind != null && TYPEDEF2.equals(kind.getNodeValue())  
		                && prot != null && PUBLIC.equals(prot.getNodeValue())) {  
		            NodeList list = n.getChildNodes();
		            for (int x = 0; x < list.getLength(); ++x) {
		                Node n2 = list.item(x);
		                if (DEFINITION.equals(n2.getNodeName())) {  
		                    String def = n2.getTextContent();
		                    if (def != null && !def.equals("")) { //$NON-NLS-1$
		                        def = def.replaceAll("<\\s*", "<");   //$NON-NLS-1$ //$NON-NLS-2$
		                        def = def.replaceAll("\\s*>", ">");   //$NON-NLS-1$ //$NON-NLS-2$
		                        String[] types = getTypedefTypes(def);
		                        if(types == null){
		                        	continue;
		                        }
		                        TypedefInfo d = new TypedefInfo(types[1], types[0]);
		                        String hashName = d.getTypedefName();
		                        int index = hashName.indexOf('<');
		                        if (index > 0) {
		                            String className = hashName.substring(0, index);
		                            hashName = hashName.replaceAll("<.*>", "<>");   //$NON-NLS-1$ //$NON-NLS-2$
		                            ClassInfo e = libHoverInfo.classes.get(className);
		                            if (e == null)
		                                break;
		                            ArrayList<ClassInfo> children = e.getChildren();
		                            if (children != null && children.size() > 0) {
		                                for (int y = 0; y < children.size(); ++y) {
		                                    ClassInfo child = children.get(y);
		                                    String childName = child.getClassName().replaceAll("\\*", "\\\\*"); //$NON-NLS-1$ //$NON-NLS-2$
		                                    childName = childName.replace("[]", "\\[\\]"); //$NON-NLS-1$ //$NON-NLS-2$
		                                    if (types[1].matches(childName.concat("::.*"))) { //$NON-NLS-1$
		                                        e = child;
		                                        break;
		                                    }
		                                }
		                            }
		                            String[] templates = e.getTemplateParms();
		                            d.copyTemplates(templates);

		                            TypedefInfo f = libHoverInfo.typedefs.get(hashName);
		                            if (f != null) {
		                                String typedefName = d.getTypedefName();
		                                for (int z = 0; z < templates.length; ++z) {
		                                    typedefName = typedefName.replaceAll(templates[z], "[a-zA-Z0-9_: ]+"); //$NON-NLS-1$
		                                }
		                                d.setTypedefName(typedefName);
		                                f.addTypedef(d);
		                            }
		                            else
		                                libHoverInfo.typedefs.put(hashName, d);
		                            break;
		                        } else {
		                            // Otherwise we have a non-template typedef name.  Just add it to the list.
		                            libHoverInfo.typedefs.put(hashName, d);
		                            break;
		                        }
		                    }
		                }
		            }
		        }
		    }
		}	
		
		return libHoverInfo;
    }

    private String getParameters(Node n6, boolean addHyphen) {
    	String desc = "<br><br><h3>Parameters:</h3>"; //$NON-NLS-1$
    	NamedNodeMap m = n6.getAttributes();
    	Node kind = m.getNamedItem("kind"); //$NON-NLS-1$
    	if (kind != null && EXCEPTION.equals(kind.getNodeValue())) { 
    		desc = "<br><br><h3>Exceptions:</h3>"; //$NON-NLS-1$
        }       
        NodeList nl = n6.getChildNodes();
        for (int x = 0; x < nl.getLength(); ++x) {
            Node n = nl.item(x);
            if (PARAMETERITEM.equals(n.getNodeName())) {
                NodeList nl2 = n.getChildNodes();
                for (int y = 0; y < nl2.getLength(); ++y) {
                    Node n2 = nl2.item(y);
                    if (PARAMETERNAMELIST.equals(n2.getNodeName())) { 
                        NodeList nl3 = n2.getChildNodes();
                        for (int z = 0; z < nl3.getLength(); ++z) {
                            Node n3 = nl3.item(z);
                            if (PARAMETERNAME.equals(n3.getNodeName())) { 
                                desc += getElementText(n3); 
                                if(addHyphen) {
                                	desc += " - ";//$NON-NLS-1$
                                }
                            }
                        }
                    } else if (PARAMETERDESCRIPTION.equals(n2.getNodeName())) { 
                        desc += getElementText(n2) + "<br>"; //$NON-NLS-1$
                    }

                }
            }
        }
        return desc;

    }

    private String getReturn(Node n6) {
        String desc = ""; //$NON-NLS-1$
        NamedNodeMap m = n6.getAttributes();
        Node kind = m.getNamedItem("kind"); //$NON-NLS-1$
        if (kind != null && RETURN.equals(kind.getNodeValue())) { 
            desc += "<br><h3>Returns:</h3>" + getElementText(n6) + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return desc;
    }

    private String[] getTemplateParms(Node classNode) {
        Node n = null;
        ArrayList<String> templateArray = new ArrayList<>();
        NodeList list = classNode.getChildNodes();
        for (int i = 0; i < list.getLength(); ++i) {
            n = list.item(i);
            if (TEMPLATEPARAMLIST.equals(n.getNodeName())) {  
                break;
            }
        }
        if (n != null) {
            NodeList templateList = n.getChildNodes();
            for (int j = 0; j < templateList.getLength(); ++j) {
                Node p = templateList.item(j);
                if (PARAM.equals(p.getNodeName())) {  
                    NodeList paramList = p.getChildNodes();
                    for (int k = 0; k < paramList.getLength(); ++k) {
                        Node q = paramList.item(k);
                        if (DECLNAME.equals(q.getNodeName())) {  
                            String templateName = q.getTextContent();
                            templateArray.add(templateName);
                        }
                    }
                }
            }
        }
        String[] templates = new String[templateArray.size()];
        return templateArray.toArray(templates);
    }
}
