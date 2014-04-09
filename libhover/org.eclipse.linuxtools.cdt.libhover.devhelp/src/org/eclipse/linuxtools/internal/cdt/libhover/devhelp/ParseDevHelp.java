/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.cyberneko.html.HTMLConfiguration;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.LibHoverMessages;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ParseDevHelp {
	
	private final static String PARSING_MSG = "Libhover.Devhelp.Parsing.msg"; //$NON-NLS-1$
	private final static String PARSING_FMT_MSG = "Libhover.Devhelp.Parsing.fmt.msg"; //$NON-NLS-1$
	private static class HTMLSaxParser extends AbstractSAXParser {
		
		private String func;
		private boolean begin;
		private boolean returnType;
		private boolean protoStart;
		private boolean parmStart;
		private boolean descStart;
		private boolean rowIgnore;
		private boolean valid = true;
		private String returnValue;
		private String funcName;
		private String rowTag;
		private StringBuilder prototype = new StringBuilder();
		private StringBuilder description = new StringBuilder();
		private int divCounter;
		private int rowItemCount;
		
		public HTMLSaxParser(String func,  String funcName) {
			super(new HTMLConfiguration());
			this.func = func;
			this.funcName = funcName.trim();
			if (this.funcName.endsWith("()")) { //$NON-NLS-1$
				this.funcName = this.funcName.replaceAll("\\(\\)", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		@Override
		public void startElement(QName name, XMLAttributes a, Augmentations aug) {
			if ("A".equals(name.rawname)) { //$NON-NLS-1$
				String fname = a.getValue("name"); //$NON-NLS-1$
				if (func.equals(fname)) {
					begin = true;
				}
			}
			if (begin) {
				if ("DIV".equals(name.rawname)) { //$NON-NLS-1$
					++divCounter;
				}
				if (!descStart) {
					if ("SPAN".equals(name.rawname)) { //$NON-NLS-1$
						String type = a.getValue("class"); //$NON-NLS-1$
						if (returnValue == null && type != null && 
								type.equals("returnvalue")) { //$NON-NLS-1$
							returnType = true;
						}
					} else if ("PRE".equals(name.rawname)) { //$NON-NLS-1$
						String type = a.getValue("class"); //$NON-NLS-1$
						if (type != null && type.equals("programlisting")) { //$NON-NLS-1$
							returnType = true;
						}
					}
				}
				if (protoStart) {
					if ("P".equals(name.rawname)) { //$NON-NLS-1$
						protoStart = false;
						descStart = true;;
 						description.append("<p>"); //$NON-NLS-1$
					}
				} else	if (descStart) {
					if ("P".equals(name.rawname)) { //$NON-NLS-1$
						description.append("<p>"); //$NON-NLS-1$
					} else if ("TABLE".equals(name.rawname)) { //$NON-NLS-1$
						description.append("<dl>"); //$NON-NLS-1$
					} else if ("TR".equals(name.rawname)) { //$NON-NLS-1$
						rowItemCount = 0;
					} else if ("TD".equals(name.rawname)) { //$NON-NLS-1$
						String type = a.getValue("class"); //$NON-NLS-1$
						if (type != null && type.equals("listing_lines")) { //$NON-NLS-1$
							rowIgnore = true;
						} else {
							rowIgnore = false;
							if (rowItemCount++ == 0) {
								rowTag = "<dt>"; //$NON-NLS-1$
							} else {
								rowTag = "<dd>"; //$NON-NLS-1$
							}
							description.append(rowTag);
						}
					}
				}
			}
		}
		
	    @Override
	    public void endElement(QName name, Augmentations aug) {
	    	if (begin) {
	    		if ("DIV".equals(name.rawname)) { //$NON-NLS-1$
	    			--divCounter;
//	    			System.out.println("divCounter is " + divCounter);
	    			if (divCounter <= 0) {
	    				begin = false;
	    			}
	    		}
	    		if (descStart) {
					if ("P".equals(name.rawname)) {//$NON-NLS-1$
						description.append("</p>"); //$NON-NLS-1$
					} else if ("TABLE".equals(name.rawname)) { //$NON-NLS-1$
						description.append("</dl>"); //$NON-NLS-1$
					} else if ("TR".equals(name.rawname)) { //$NON-NLS-1$
						rowItemCount = 0;
					} else if ("TD".equals(name.rawname)) { //$NON-NLS-1$
						if (!rowIgnore) {
							if (rowTag != null && rowTag.equals("<dt>")) {//$NON-NLS-1$
								description.append("</dt>"); //$NON-NLS-1$
							} else {
								description.append("</dd>"); //$NON-NLS-1$
							}
						}
						rowIgnore = false;
					}
	    		}
	    	}
	    }

	    @Override
	    public void characters(XMLString data, Augmentations aug) {
	    	if (begin) {
	    		if (returnType) {
	    			returnValue = ""; //$NON-NLS-1$
	    			String tmp = data.toString().trim();
	    			boolean completed = false;
	    			if (tmp.endsWith(");")) { //$NON-NLS-1$
	    				completed = true;
	    				tmp = tmp.substring(0, tmp.length() - 2);
	    			}
	    			String tokens[] = tmp.split("\\s+"); //$NON-NLS-1$
	    			String separator = ""; //$NON-NLS-1$
	    			protoStart = true;
	    			for (int i = 0; i < tokens.length; ++i) {
	    				String token = tokens[i];
	    				if (!token.equals(funcName)) {
	    					returnValue += separator + token;
	    					separator = " "; //$NON-NLS-1$
	    				} else {
	    					separator = ""; //$NON-NLS-1$
	    					for (int j = i + 1; j < tokens.length; ++j) {
	    						String jtoken = tokens[j];
	    						if (j == i + 1 && jtoken.charAt(0) == '(') {
	    							jtoken = jtoken.substring(1);
	    							parmStart = true;
	    							protoStart = false;
	    						}
	    						prototype.append(separator).append(jtoken);
	    						separator = " "; //$NON-NLS-1$
	    					}
	    					if (parmStart && completed) {
	    						parmStart = false;
	    						descStart = true;
	    					}
	    					break;
	    				}
	    			}
	    			returnType = false;
	    		} else if (protoStart) {
	    			String temp = data.toString().trim();
	    			boolean completed = false;
	    			if (temp.endsWith(");")) { //$NON-NLS-1$
	    				completed = true;
	    				temp = temp.substring(0, temp.length() - 2);
	    			}
	    			String separator = " "; //$NON-NLS-1$
	    			while (temp.startsWith("*") || temp.startsWith("const")) { //$NON-NLS-1$ //$NON-NLS-2$
	    				if (temp.charAt(0) == '*') {
	    					returnValue += separator + "*"; //$NON-NLS-1$
	    					temp = temp.substring(1).trim();
	    					separator = ""; //$NON-NLS-1$
	    				} else {
	    					returnValue += "const"; //$NON-NLS-1$
	    					temp = temp.substring(5).trim();
	    					separator = " "; //$NON-NLS-1$
	    				}
	    			}
	    			int index = temp.lastIndexOf('(');
	    			int index2 = temp.lastIndexOf(')');
	    			if (index2 < index) {
	    				if (index + 1 < temp.length()) {
	    					temp = temp.substring(index + 1).trim();
	    					prototype.append(temp);
	    				}
	    				parmStart = true;
	    				protoStart = false;
	    			}
	    			if (parmStart && completed) {
	    				parmStart = false;
	    				descStart = true;
	    			}
	    		} else if (parmStart) {
	    			String parmData = data.toString().trim();
	    			int index = parmData.indexOf(')');
	    			if (index >= 0) {
	    				parmStart = false;
	    				descStart = true;
	    				parmData = parmData.substring(0, index);
	    			}
	    			if (prototype.length() == 0) {
	    			   if (!parmData.equals(",") && !parmData.isEmpty()) { //$NON-NLS-1$
	    				   parmData = " " + parmData; //$NON-NLS-1$
	    			   }
	    			}
	    			prototype.append(parmData);
	    		} else if (descStart) {
	    			if (!rowIgnore) {
	    				description.append(String.valueOf(data));
	    			}
	    		}
	    	}
	    }
	    
	    private FunctionInfo getFunctionInfo() {
	    	if (!valid || returnValue == null ||
	    			returnValue.startsWith("#") || //$NON-NLS-1$
	    			returnValue.startsWith("typedef ")) { //$NON-NLS-1$
	    		return null;
	    	}
	    	FunctionInfo info = new FunctionInfo(funcName);
	    	info.setReturnType(returnValue);
	    	info.setPrototype(prototype.toString());
	    	info.setDescription(description.toString());
	    	return info;
	    }
	    
	    private String getFuncName() {
	    	return funcName;
	    }
	    
	    @Override
		public String toString() {
	    	return "funcName: <" + funcName + "> returnType: <" + returnValue + //$NON-NLS-1$ //$NON-NLS-2$
	    	"> prototype: <" + prototype + "> description: " + description; //$NON-NLS-1$ //$NON-NLS-2$
	    }
	}
	
	public static class DevHelpParser {
		
		private static final class NullEntityResolver implements EntityResolver {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) {
				return new InputSource(new StringReader("")); //$NON-NLS-1$
			}
		}

		private static final class FilenameComparator implements
				Comparator<IFileStore> {
			@Override
			public int compare(IFileStore arg0, IFileStore arg1) {
				return (arg0.getName().compareToIgnoreCase(arg1.getName()));
			}
		}

		private String dirName;
		private LibHoverInfo libhover;
		private boolean debug;
		private FilenameComparator filenameComparator = new FilenameComparator();
		private NullEntityResolver entityResolver = new NullEntityResolver();
		private DocumentBuilderFactory factory;
		
		public DevHelpParser(String dirName) {
			this(dirName, false);
		}
		
		public DevHelpParser(String dirName, boolean debug) {
			this.dirName = dirName;
			this.libhover = new LibHoverInfo();
			this.debug = debug;
			factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
		}

		public LibHoverInfo getLibHoverInfo() {
			return libhover;
		}
		
		public LibHoverInfo parse(IProgressMonitor monitor) {
			try {
				IFileSystem fs = EFS.getLocalFileSystem();
				IPath dirPath = new Path(dirName);
				IFileStore htmlDir = fs.getStore(dirPath);
				IFileStore[] files = htmlDir.childStores(EFS.NONE, null);
				monitor.beginTask(LibHoverMessages.getString(PARSING_MSG), files.length);
				Arrays.sort(files, filenameComparator);
   				for (int i = 0; i < files.length; ++i) {
					IFileStore file = files[i];
					String name = file.fetchInfo().getName();
					if (monitor.isCanceled()) {
						return null;
					}
					monitor.setTaskName(LibHoverMessages.getFormattedString(PARSING_FMT_MSG, 
							new String[]{name}));
					File f = new File(dirPath.append(name).append(name + ".devhelp2").toOSString()); //$NON-NLS-1$
					if (f.exists()) {
						parse(f.getAbsolutePath(),
								monitor);
					} else {
						parse(dirPath.append(name)
								.append(name + ".devhelp").toOSString(), //$NON-NLS-1$ 
								monitor);
					}
					monitor.worked(1);
   				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return libhover;
		}

		private void parseLink(Node link, Node name, IPath path, LibHoverInfo libhover) {
			String linkValue = link.getNodeValue();
			String[] linkParts = linkValue.split("#"); //$NON-NLS-1$
			if (linkParts.length == 2) {
				try {
					String nameString = name.getNodeValue();
					nameString = nameString.replaceAll("\\(.*\\);+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
					if (nameString.contains("::") || nameString.startsWith("enum ") //$NON-NLS-1$ //$NON-NLS-2$
							|| nameString.contains("\"")) { //$NON-NLS-1$
						return;
					}
					InputStream reader = new FileInputStream(path.removeLastSegments(1).toOSString()
							+ "/" + linkParts[0]); //$NON-NLS-1$
					HTMLSaxParser parser = new HTMLSaxParser(linkParts[1], nameString);
					parser.parse(new InputSource(reader));
					FunctionInfo finfo = parser.getFunctionInfo();
					if (finfo != null) {
						if (debug) {
							System.out.println(parser.toString());
						}
						libhover.functions.put(parser.getFuncName(), parser.getFunctionInfo());
					}
				} catch (IOException e) {
					// ignore
				} catch (SAXException e) {
					e.printStackTrace();
				}
			}
		}

		private void parse(String fileName, IProgressMonitor monitor) {
			try {
				Path path = new Path(fileName);
				File f = new File(fileName);
				FileInputStream stream = new FileInputStream(f);
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setEntityResolver(entityResolver);
				Document doc = builder.parse(stream);
				NodeList bookNodes = doc.getElementsByTagName("book"); //$NON-NLS-1$
				for (int x = 0; x < bookNodes.getLength(); ++x) {
					Node n = bookNodes.item(x);
					NamedNodeMap m = n.getAttributes();
					Node language = m.getNamedItem("language"); //$NON-NLS-1$
					if (language != null && !language.getNodeValue().equals("c")) { //$NON-NLS-1$
						return;
					}
				}
				if (path.getFileExtension().equals("devhelp")) { //$NON-NLS-1$
					NodeList nl = doc.getElementsByTagName("function"); // $NON-NLS-1$ //$NON-NLS-1$
					for (int i = 0; i < nl.getLength(); ++i) {
						if (monitor.isCanceled()) {
							return;
						}
						Node n = nl.item(i);
						NamedNodeMap m = n.getAttributes();
						Node name = m.getNamedItem("name"); // $NON-NLS-1$ //$NON-NLS-1$
						Node link = m.getNamedItem("link"); // $NON-NLS-1$ //$NON-NLS-1$
						if (link != null) {
							parseLink(link, name, path, libhover);
						}
					}
				} else if (path.getFileExtension().equals("devhelp2")) { //$NON-NLS-1$
					NodeList nl = doc.getElementsByTagName("keyword"); // $NON-NLS-1$ //$NON-NLS-1$
					for (int i = 0; i < nl.getLength(); ++i) {
						if (monitor.isCanceled())
							return;
						Node n = nl.item(i);
						NamedNodeMap m = n.getAttributes();
						Node type = m.getNamedItem("type"); // $NON-NLS-1$ //$NON-NLS-1$
						if (type != null) {
							String typeName = type.getNodeValue();
							if (typeName.equals("function")) { //$NON-NLS-1$
								Node name = m.getNamedItem("name"); // $NON-NLS-1$ //$NON-NLS-1$
								Node link = m.getNamedItem("link"); // $NON-NLS-1$ //$NON-NLS-1$
								if (link != null) {
									parseLink(link, name, path, libhover);
								}
							}
						}
					}
				}
			} catch (FileNotFoundException e1) {
				// ignore
			} catch (ParserConfigurationException|SAXException|IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		long startParse = System.currentTimeMillis();
		String devhelpHtmlDirectory = "/usr/share/gtk-doc/html"; //$NON-NLS-1$
		DevHelpParser p = new DevHelpParser(devhelpHtmlDirectory, false);
		File dir = new File(devhelpHtmlDirectory);
		for (File f : dir.listFiles()) {
			String name = f.getName();
			p.parse(f.getAbsolutePath() + "/" + name + ".devhelp2",  //$NON-NLS-1$ //$NON-NLS-2$
					new NullProgressMonitor());
		}
		long endParse = System.currentTimeMillis();
		System.out.println("Parse Complete:"+(endParse-startParse)); //$NON-NLS-1$
		long startSerialize = System.currentTimeMillis();
		LibHoverInfo hover = p.getLibHoverInfo();
		try {
			// Now, output the LibHoverInfo for caching later
			IPath workspaceDir = new Path(args[1]);
			IPath location = workspaceDir.append("org.eclipse.linuxtools.cdt.libhover/C"); //$NON-NLS-1$
			File ldir = new File(location.toOSString());
			ldir.mkdir();
			location = location.append("devhelp.libhover"); //$NON-NLS-1$
			try (FileOutputStream f = new FileOutputStream(
					location.toOSString());
					ObjectOutputStream out = new ObjectOutputStream(f)) {
				out.writeObject(hover);
			}
		} catch(Exception e) {
		}
		long endSerialize = System.currentTimeMillis();
		
		System.out.println("Parse Complete:"+(endSerialize-startSerialize)); //$NON-NLS-1$

	}

}
