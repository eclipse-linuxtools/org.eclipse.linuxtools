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
package org.eclipse.linuxtools.cdt.libhover.libstdcxx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.ClassInfo;
import org.eclipse.linuxtools.cdt.libhover.MemberInfo;
import org.eclipse.linuxtools.cdt.libhover.TypedefInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class DoxygenCPPInfo {

	private Document document;
	private LibHoverInfo cppInfo = new LibHoverInfo();
	private HashMap<String, ClassInfo> classesById = new HashMap<String, ClassInfo>();

	public DoxygenCPPInfo(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	private String[] getTypedefTypes(String def) {
		String[] result = null;
		if (def.startsWith("typedef ")) { //$NON-NLS-1$
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
				}
				else if (ch == '>') {
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
			if (namespace < 0)
				result[0] = def.substring(8, startIndex).trim();
			else
				result[0] = result[1].substring(0, namespace) + "::" + def.substring(8, startIndex).trim(); //$NON-NLS-1$
		}
		return result;
	}

	private String getElementText(Node node) {
		StringBuffer d = new StringBuffer();
		NodeList nl = node.getChildNodes();
		for (int x = 0; x < nl.getLength(); ++x) {
			Node text = nl.item(x);
			if (text.getNodeType() == Node.TEXT_NODE)
				d.append(text.getNodeValue());
			else
				d.append(getElementText(text));
		}
		return d.toString();
	}

	public ClassInfo getClassInfo(String className) {
		String typedefName = className.replaceAll("<.*>", "<>"); //$NON-NLS-1$ //$NON-NLS-2$
		TypedefInfo typedef = cppInfo.typedefs.get(typedefName);
		if (typedef != null) {
			className = typedef.getTransformedType(className);  // Reset class name to typedef transformation
		}
		int index = className.indexOf('<');
		// Check if it is a template reference.
		if (index != -1) {
			// It is.  We want to see if there are partial specific templates
			// and we choose the first match.  If nothing matches our particular
			// case, we fall back on the initial generic template.
			ClassInfo info = cppInfo.classes.get(className.substring(0, index));
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
		return cppInfo.classes.get(className);
	}

	public void buildDoxygenCPPInfo(String fileName) {
		try {
			// Create a hash table of all the class nodes mapped by class name.  Trim any template info
			// for the class name key value.
			NodeList nl = getDocument().getElementsByTagName("compounddef");  //$NON-NLS-1$
			for (int i = 0; i < nl.getLength(); ++i) {
				Node n = nl.item(i);
				NamedNodeMap attrs = n.getAttributes();
				Node kind = attrs.getNamedItem("kind");  //$NON-NLS-1$
				Node id = attrs.getNamedItem("id");  //$NON-NLS-1$
				Node prot = attrs.getNamedItem("prot");  //$NON-NLS-1$
				// We are only interested in cataloging public classes.
				if (id != null && prot != null && prot.getNodeValue().equals("public")  //$NON-NLS-1$
						&& kind != null && kind.getNodeValue().equals("class")) {  //$NON-NLS-1$
					NodeList nl2 = n.getChildNodes();
					ClassInfo d = null;
					String hashName = null;
					for (int j = 0; j < nl2.getLength(); ++j) {
						Node n2 = nl2.item(j);
						String name2 = n2.getNodeName();
						if (name2.equals("compoundname")) {  //$NON-NLS-1$
							String text = n2.getTextContent();
							if (text != null && !text.equals("")) { //$NON-NLS-1$
								String className = text;
								text = text.replaceAll("<\\s*", "<");   //$NON-NLS-1$ //$NON-NLS-2$
								text = text.replaceAll("\\s*>", ">");   //$NON-NLS-1$ //$NON-NLS-2$
								int index = text.indexOf('<');
								hashName = text;
								if (index > 0)
									hashName = text.substring(0, index);
								d = new ClassInfo(className, id.getNodeValue(), n);
								classesById.put(id.getNodeValue(), d);
								ClassInfo e = cppInfo.classes.get(hashName);
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
									cppInfo.classes.put(hashName, d);
							}
						} else if (name2.equals("templateparamlist")) { //$NON-NLS-1$
							ArrayList<String> templates = new ArrayList<String>();
							NodeList params = n2.getChildNodes();
							int paramsLength = params.getLength();
							for (int j2 = 0; j2 < paramsLength; ++j2) {
								Node n3 = params.item(j2);
								if (n3.getNodeName().equals("param")) { //$NON-NLS-1$
									NodeList types = n3.getChildNodes();
									int typesLength = types.getLength();
									for (int j3 = 0; j3 < typesLength; ++j3) {
										Node n4 = types.item(j3);
										if (n4.getNodeName().equals("declname")) { //$NON-NLS-1$
											templates.add(getElementText(n4));
										}
									}
								}
							}
							String[] templateNames = new String[templates.size()];
							d.setTemplateParms(templates.toArray(templateNames));
						} else if (name2.equals("includes")) {  //$NON-NLS-1$
							String include = getElementText(n2);
							if (d != null)
								d.setInclude(include);
						} else if (name2.equals("basecompoundref")) {  //$NON-NLS-1$
							// We have a base class.  If public, add it to the list of nodes to look at in case we don't find the member
							// in the current class definition.
							NamedNodeMap m = n2.getAttributes();
							if (m != null) {
								Node refid = m.getNamedItem("refid");  //$NON-NLS-1$
								Node prot2 = m.getNamedItem("prot");  //$NON-NLS-1$
								if (prot2 != null && prot2.getNodeValue().equals("public")) {  //$NON-NLS-1$
									ClassInfo baseClass = null;
									if (refid != null) {
										// If we have been given the id of the base class, fetch it directly
										baseClass = classesById.get(refid.getNodeValue());
									} else {
										// We probably have a template that needs resolution
										String baseClassName = n2.getTextContent();
//										System.out.println("base class name is " + baseClassName);
										baseClass = getClassInfo(baseClassName);
									}
									if (d != null && baseClass != null)
										d.addBaseClass(baseClass);
								}
							}
						} else if (name2.equals("sectiondef")) {  //$NON-NLS-1$
							// We are only interested in public member functions which are in their own section.
							NamedNodeMap m = n2.getAttributes();
							if (m != null) {
								Node kind2 = m.getNamedItem("kind");  //$NON-NLS-1$
								if (kind2 != null && kind2.getNodeValue().equals("public-func")) {  //$NON-NLS-1$
									NodeList pubfuncs = n2.getChildNodes();
									int pubfuncLength = pubfuncs.getLength();
									for (int j1 = 0; j1 < pubfuncLength; ++j1) {
										Node n3 = pubfuncs.item(j1);
										// Add all public member functions to the list of members
										if (n3.getNodeName().equals("memberdef")) {  //$NON-NLS-1$
											NamedNodeMap m3 = n3.getAttributes();
											if (m3 != null) {
												Node m3Kind = m3.getNamedItem("kind");  //$NON-NLS-1$
												if (m3Kind != null && m3Kind.getNodeValue().equals("function")) {  //$NON-NLS-1$
													String name = null;
													String type = null;
													String args = null;
													String desc = null;
													ArrayList<String> parms = new ArrayList<String>();
													NodeList nl4 = n3.getChildNodes();
													int memberLength = nl4.getLength();
													for (int k = 0; k < memberLength; ++k) {
														Node n4 = nl4.item(k);
														String n4Name = n4.getNodeName();
														if (n4Name.equals("type")) {  //$NON-NLS-1$
															NodeList nl5 = n4.getChildNodes();
															type = new String("");  //$NON-NLS-1$
															for (int x = 0; x < nl5.getLength(); ++x) {
																Node n5 = nl5.item(x);
																if (n5.getNodeType() == Node.TEXT_NODE)
																	type += n5.getNodeValue();
																else if (n5.getNodeName().equals("ref")) {  //$NON-NLS-1$
																	NamedNodeMap n5m = n5.getAttributes();
																	Node n5id = n5m.getNamedItem("refid"); //$NON-NLS-1$
																	if (n5id != null) {
																		String refid = n5id.getNodeValue();
																		ClassInfo refClass = classesById.get(refid);
																		if (refClass != null)
																			type += refClass.getClassName();
																	}
																}
															}
														} else if (n4Name.equals("name")) {  //$NON-NLS-1$
															name = n4.getTextContent();
														} else if (n4Name.equals("argsstring")) {  //$NON-NLS-1$
															args = getElementText(n4);
														} else if (n4Name.equals("param")) {  //$NON-NLS-1$
															NodeList nl5 = n4.getChildNodes();
															for (int x = 0; x < nl5.getLength(); ++x) {
																Node n5 = nl5.item(x);
																if (n5.getNodeName().equals("type")) {  //$NON-NLS-1$
																	parms.add(getElementText(n5));
																}
															}
														} else if (n4Name.equals("briefdescription")) {  //$NON-NLS-1$
															NodeList nl5 = n4.getChildNodes();
															for (int x = 0; x < nl5.getLength(); ++x) {
																Node n5 = nl5.item(x);
																if (n5.getNodeName().equals("para")) {  //$NON-NLS-1$
																	if (desc == null)
																		desc = new String(""); //$NON-NLS-1$
																	desc += "<p>" + getElementText(n5) + "</p>";   //$NON-NLS-1$ //$NON-NLS-2$
																}
															}
														} else if (n4Name.equals("detaileddescription")) {  //$NON-NLS-1$
															NodeList nl5 = n4.getChildNodes();
															for (int x = 0; x < nl5.getLength(); ++x) {
																Node n5 = nl5.item(x);
																if (n5.getNodeName().equals("para")) {  //$NON-NLS-1$
																	if (desc == null)
																		desc = new String("");  //$NON-NLS-1$
																	NodeList nl6 = n5.getChildNodes();
																	Node n6 = nl6.item(0);
																	if (n6.getNodeType() == Node.TEXT_NODE)
																		desc += "<p>" + getElementText(n5) + "</p>";   //$NON-NLS-1$ //$NON-NLS-2$
																	else {
																		for (int x2 = 0; x2 < nl6.getLength(); ++x2) {
																			n6 = nl6.item(x2);
																			if (n6.getNodeName().equals("parameterlist")) {  //$NON-NLS-1$
																				desc += getParameters(n6);
																			} else if (n6.getNodeName().equals("simplesect")) {  //$NON-NLS-1$
																				desc += getReturn(n6);
																			}
																		}
																	}
																}
															}
														} else if (n4Name.equals("location")) { //$NON-NLS-1$
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
			nl = getDocument().getElementsByTagName("memberdef");  //$NON-NLS-1$
			for (int i = 0; i < nl.getLength(); ++i) {
				Node n = nl.item(i);
				NamedNodeMap attrs = n.getAttributes();
				if (attrs != null) {
					Node kind = attrs.getNamedItem("kind");  //$NON-NLS-1$
					Node prot = attrs.getNamedItem("prot");  //$NON-NLS-1$
					if (kind != null && kind.getNodeValue().equals("typedef")  //$NON-NLS-1$
							&& prot != null && prot.getNodeValue().equals("public")) {  //$NON-NLS-1$
						NodeList list = n.getChildNodes();
						for (int x = 0; x < list.getLength(); ++x) {
							Node n2 = list.item(x);
							if (n2.getNodeName().equals("definition")) {  //$NON-NLS-1$
								String def = n2.getTextContent();
								if (def != null && !def.equals("")) { //$NON-NLS-1$
									def = def.replaceAll("<\\s*", "<");   //$NON-NLS-1$ //$NON-NLS-2$
									def = def.replaceAll("\\s*>", ">");   //$NON-NLS-1$ //$NON-NLS-2$
									String[] types = getTypedefTypes(def);
									TypedefInfo d = new TypedefInfo(types[1], types[0]);
									String hashName = d.getTypedefName();
									int index = hashName.indexOf('<');
									if (index > 0) {
										String className = hashName.substring(0, index);
										hashName = hashName.replaceAll("<.*>", "<>");   //$NON-NLS-1$ //$NON-NLS-2$
										ClassInfo e = cppInfo.classes.get(className);
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

										TypedefInfo f = cppInfo.typedefs.get(hashName);
										if (f != null) {
											String typedefName = d.getTypedefName();
											for (int z = 0; z < templates.length; ++z) {
												typedefName = typedefName.replaceAll(templates[z], "[a-zA-Z0-9_: ]+"); //$NON-NLS-1$
											}
											d.setTypedefName(typedefName);
											f.addTypedef(d);
										}
										else
											cppInfo.typedefs.put(hashName, d);
										break;
									} else {
										// Otherwise we have a non-template typedef name.  Just add it to the list.
										cppInfo.typedefs.put(hashName, d);
										break;
									}
								}
							}
						}
					}
				}
			}
			// Now, output the LibHoverInfo for caching later
			FileOutputStream f = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(f);
			out.writeObject(cppInfo);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getParameters(Node n6) {
		String desc = "<br><br><h3>Parameters:</h3>"; //$NON-NLS-1$
		NodeList nl = n6.getChildNodes();
		for (int x = 0; x < nl.getLength(); ++x) {
			Node n = nl.item(x);
			if (n.getNodeName().equals("parameteritem")) { //$NON-NLS-1$
				NodeList nl2 = n.getChildNodes();
				for (int y = 0; y < nl2.getLength(); ++y) {
					Node n2 = nl2.item(y);
					if (n2.getNodeName().equals("parameternamelist")) { //$NON-NLS-1$
						NodeList nl3 = n2.getChildNodes();
						for (int z = 0; z < nl3.getLength(); ++z) {
							Node n3 = nl3.item(z);
							if (n3.getNodeName().equals("parametername")) { //$NON-NLS-1$
								desc += getElementText(n3) + " - "; //$NON-NLS-1$
							}
						}
					} else if (n2.getNodeName().equals("parameterdescription")) { //$NON-NLS-1$
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
		if (kind != null && kind.getNodeValue().equals("return")) { //$NON-NLS-1$
			desc += "<br><h3>Returns:</h3>" + getElementText(n6) + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return desc;
	}

	public String[] getTemplateParms(Node classNode) {
		Node n = null;
		ArrayList<String> templateArray = new ArrayList<String>();
		NodeList list = classNode.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			n = list.item(i);
			if (n.getNodeName().equals("templateparamlist")) {  //$NON-NLS-1$
				break;
			}
		}
		if (n != null) {
			NodeList templateList = n.getChildNodes();
			for (int j = 0; j < templateList.getLength(); ++j) {
				Node p = templateList.item(j);
				if (p.getNodeName().equals("param")) {  //$NON-NLS-1$
					NodeList paramList = p.getChildNodes();
					for (int k = 0; k < paramList.getLength(); ++k) {
						Node q = paramList.item(k);
						if (q.getNodeName().equals("declname")) {  //$NON-NLS-1$
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

	/**
	 * Create LibHoverInfo serialized output
	 *
	 * @param args (args[0] = location of Doxygen xml document to parse (file or URL),
	 * 				args[1] = name of file to put serialized LibHoverInfo
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
				DoxygenCPPInfo d = new DoxygenCPPInfo(doc);
				d.buildDoxygenCPPInfo(args[1]);
			}
			System.out.println("Built " + args[1] + " from " + args[0]); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
