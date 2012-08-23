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
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
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
	private static class Parser extends ParserCallback {
		
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
		private String prototype = ""; //$NON-NLS-1$
		private String description = ""; //$NON-NLS-1$
		private int divCounter;
		private int rowItemCount;
		
		public Parser(String func,  String funcName) {
			this.func = func;
			this.funcName = funcName.trim();
			if (this.funcName.endsWith("()")) //$NON-NLS-1$
				this.funcName = this.funcName.replaceAll("\\(\\)", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		@Override
		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (t == HTML.Tag.A) {
				String name = (String)(a.getAttribute(HTML.Attribute.NAME));
				if (func.equals(name)) {
					begin = true;
				}
			}
			if (begin) {
				if (t == HTML.Tag.DIV) {
					++divCounter;
				}
				if (!descStart) {
					if (t == HTML.Tag.SPAN) {
						String type = (String)a.getAttribute(HTML.Attribute.CLASS);
						if (returnValue == null && type != null && 
								type.equals("returnvalue")) { //$NON-NLS-1$
							returnType = true;
						}
					}
					if (t == HTML.Tag.PRE) {
						String type = (String)a.getAttribute(HTML.Attribute.CLASS);
						if (type != null && type.equals("programlisting")) //$NON-NLS-1$
							returnType = true;
					}
				}
				if (protoStart) {
					if (t == HTML.Tag.P) {
						protoStart = false;
						descStart = true;;
 						description += "<p>"; //$NON-NLS-1$
					}
				} else	if (descStart) {
					if (t == HTML.Tag.P) {
						description += "<p>"; //$NON-NLS-1$
					}
					else if (t == HTML.Tag.TABLE) {
						description += "<dl>"; //$NON-NLS-1$
					}
					else if (t == HTML.Tag.TR) {
						rowItemCount = 0;
					}
					else if (t == HTML.Tag.TD) {
						String type = (String)a.getAttribute(HTML.Attribute.CLASS);
						if (type != null && type.equals("listing_lines")) { //$NON-NLS-1$
							rowIgnore = true;
						} else {
							rowIgnore = false;
							if (rowItemCount++ == 0)
								rowTag = "<dt>"; //$NON-NLS-1$
							else
								rowTag = "<dd>"; //$NON-NLS-1$
							description += rowTag;
						}
					}
				}
			}
		}
		
	    @Override
		public void handleEndTag(HTML.Tag t, int pos) {
	    	if (begin) {
	    		if (t == HTML.Tag.DIV) {
	    			--divCounter;
//	    			System.out.println("divCounter is " + divCounter);
	    			if (divCounter <= 0) {
	    				begin = false;
	    				try {
							flush();
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    		}
	    		if (descStart) {
	    			if (t == HTML.Tag.P)
 	    				description += "</p>"; //$NON-NLS-1$
	    			else if (t == HTML.Tag.TABLE)
	    				description += "</dl>"; //$NON-NLS-1$
	    			else if (t == HTML.Tag.TR) {
	    				rowItemCount = 0;
	    			}
	    			else if (t == HTML.Tag.TD) {
	    				if (!rowIgnore) {
	    					if (rowTag != null && rowTag.equals("<dt>")) //$NON-NLS-1$
	    						description += "</dt>"; //$NON-NLS-1$
	    					else
	    						description += "</dd>"; //$NON-NLS-1$
	    				}
	    				rowIgnore = false;
	    			}
	    		}
	    	}
	    }

	    @Override
		public void handleText(char[] data, int pos) {
	    	if (begin) {
	    		if (returnType) {
	    			returnValue = ""; //$NON-NLS-1$
	    			String tmp = String.valueOf(data).trim();
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
	    						prototype += separator + jtoken;
	    						separator = " ";
	    					}
	    					if (parmStart && completed) {
	    						parmStart = false;
	    						descStart = true;
	    					}
	    					break;
	    				}
	    			}
	    			returnType = false;
	    		}
	    		else if (protoStart) {
	    			String temp = String.valueOf(data).trim();
	    			boolean completed = false;
	    			if (temp.endsWith(");")) { //$NON-NLS-1$
	    				completed = true;
	    				temp = temp.substring(0, temp.length() - 2);
	    			}
	    			String separator = " ";
	    			while (temp.startsWith("*") || temp.startsWith("const")) { //$NON-NLS-1$
	    				if (temp.charAt(0) == '*') { //$NON-NLS-1$
	    					returnValue += separator + "*"; //$NON-NLS-1$
	    					temp = temp.substring(1).trim();
	    					separator = ""; //$NON-NLS-1$
	    				} else {
	    					returnValue += "const"; //$NON-NLS-1$
	    					temp = temp.substring(5).trim();
	    					separator = " ";
	    				}
	    			}
	    			int index = temp.lastIndexOf('(');
	    			int index2 = temp.lastIndexOf(')');
	    			if (index2 < index) {
	    				if (index + 1 < temp.length()) {
	    					temp = temp.substring(index + 1).trim();
	    					prototype += temp;
	    				}
	    				parmStart = true;
	    				protoStart = false;
	    			}
	    			if (parmStart && completed) {
	    				parmStart = false;
	    				descStart = true;
	    			}
	    		}
	    		else if (parmStart) {
	    			String parmData = String.valueOf(data).trim();
	    			int index = parmData.indexOf(')');
	    			if (index >= 0) {
	    				parmStart = false;
	    				descStart = true;
	    				parmData = parmData.substring(0, index);
	    			}
	    			if (!prototype.equals("")) {
	    			   if (!parmData.equals(",") && !parmData.equals(""))
	    				   parmData = " " + parmData;
	    			}
	    			prototype += parmData;
	    		}
	    		else if (descStart) {
	    			if (!rowIgnore)
	    				description += String.valueOf(data);
	    		}
	    	}

	    }
	    
	    public FunctionInfo getFunctionInfo() {
	    	if (!valid || returnValue == null ||
	    			returnValue.startsWith("#") ||
	    			returnValue.startsWith("typedef "))
	    		return null;
	    	FunctionInfo info = new FunctionInfo(funcName);
	    	info.setReturnType(returnValue);
	    	info.setPrototype(prototype);
	    	info.setDescription(description);
	    	return info;
	    }
	    
	    public String getFuncName() {
	    	return funcName;
	    }
	    
	    @Override
		public String toString() {
	    	return "funcName: <" + funcName + "> returnType: <" + returnValue +
	    	"> prototype: <" + prototype + "> description: " + description;
	    }

	    @Override
		public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	    	
	    }


	}
	
	public static class DevHelpParser {
		
		private String dirName;
		private LibHoverInfo libhover;
		private boolean debug;
		
		public DevHelpParser(String dirName) {
			this(dirName, false);
		}
		
		public DevHelpParser(String dirName, boolean debug) {
			this.dirName = dirName;
			this.libhover = new LibHoverInfo();
			this.debug = debug;
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
				Arrays.sort(files, new Comparator<IFileStore>() {
					@Override
					public int compare(IFileStore arg0, IFileStore arg1) {
						return (arg0.getName().compareToIgnoreCase(arg1.getName()));
					}
				});
   				for (int i = 0; i < files.length; ++i) {
					IFileStore file = files[i];
					String name = file.fetchInfo().getName();
					if (monitor.isCanceled())
						return null;
					monitor.setTaskName(LibHoverMessages.getFormattedString(PARSING_FMT_MSG, 
							new String[]{name}));
					File f = new File(dirPath.append(name).append(name + ".devhelp2").toOSString());
					if (f.exists())
						parse(dirPath.append(name).append(name + ".devhelp2").toOSString(), //$NON-NLS-1$ 
								monitor);
					else
						parse(dirPath.append(name).append(name + ".devhelp").toOSString(), //$NON-NLS-1$ 
								monitor);
					monitor.worked(1);
   				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
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
							|| nameString.contains("\"")) //$NON-NLS-1$
						return;
					Reader reader = new FileReader(path.removeLastSegments(1).toOSString()
							+ "/" + linkParts[0]); //$NON-NLS-1$
					Parser callback = new Parser(linkParts[1], nameString);
					new ParserDelegator().parse(reader, callback, true);
					FunctionInfo finfo = callback.getFunctionInfo();
					if (finfo != null) {
						if (debug)
							System.out.println(callback.toString());
						libhover.functions.put(callback.getFuncName(), callback.getFunctionInfo());
					}
				} catch (FileNotFoundException e1) {
					// ignore
				} catch (IOException e) {
					// ignore
				}
			}
		}

		public void parse(String fileName, IProgressMonitor monitor) {
			try {
				Path path = new Path(fileName);
				File f = new File(fileName);
				FileInputStream stream = new FileInputStream(f);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setEntityResolver(new EntityResolver()
		        {
		            @Override
					public InputSource resolveEntity(String publicId, String systemId)
		                throws SAXException, IOException
		            {
		                return new InputSource(new StringReader("")); //$NON-NLS-1$
		            }
		        });
				Document doc = builder.parse(stream);
				NodeList bookNodes = doc.getElementsByTagName("book"); //$NON-NLS-1$
				for (int x = 0; x < bookNodes.getLength(); ++x) {
					Node n = bookNodes.item(x);
					NamedNodeMap m = n.getAttributes();
					Node language = m.getNamedItem("language"); //$NON-NLS-1$
					if (language != null && !language.getNodeValue().equals("c"))
						return;
				}
				if (path.lastSegment().endsWith("devhelp")) {
					NodeList nl = doc.getElementsByTagName("function"); // $NON-NLS-1$
					for (int i = 0; i < nl.getLength(); ++i) {
						if (monitor.isCanceled())
							return;
						Node n = nl.item(i);
						NamedNodeMap m = n.getAttributes();
						Node name = m.getNamedItem("name"); // $NON-NLS-1$
						Node link = m.getNamedItem("link"); // $NON-NLS-1$
						if (link != null) {
							parseLink(link, name, path, libhover);
						}
					}
				} else if (path.lastSegment().endsWith("devhelp2")) {
					NodeList nl = doc.getElementsByTagName("keyword"); // $NON-NLS-1$
					for (int i = 0; i < nl.getLength(); ++i) {
						if (monitor.isCanceled())
							return;
						Node n = nl.item(i);
						NamedNodeMap m = n.getAttributes();
						Node type = m.getNamedItem("type"); // $NON-NLS-1$
						if (type != null) {
							String typeName = type.getNodeValue();
							if (typeName.equals("function")) { //$NON-NLS-1$
								Node name = m.getNamedItem("name"); // $NON-NLS-1$
								Node link = m.getNamedItem("link"); // $NON-NLS-1$
								if (link != null) {
									parseLink(link, name, path, libhover);
								}
							}
						}
					}
				}
			} catch (FileNotFoundException e1) {
				// ignore
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class UpdateDevhelp extends Job {

		public UpdateDevhelp(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			return null;
		}
	}
	
	public static void main(String[] args) {
		
		String devhelpHtmlDirectory = args[0];
		DevHelpParser p = new DevHelpParser(devhelpHtmlDirectory, true);
		File dir = new File(devhelpHtmlDirectory);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; ++i) {
			File f = files[i];
			String name = f.getName();
			p.parse(f.getAbsolutePath() + "/" + name + ".devhelp2",  //$NON-NLS-1$ //$NON-NLS-2$
					new NullProgressMonitor());
		}
		LibHoverInfo hover = p.getLibHoverInfo();
		try {
			// Now, output the LibHoverInfo for caching later
			IPath workspaceDir = new Path(args[1]);
			IPath location = workspaceDir.append("org.eclipse.linuxtools.cdt.libhover/C"); //$NON-NLS-1$
			File ldir = new File(location.toOSString());
			ldir.mkdir();
			location = location.append("devhelp.libhover"); //$NON-NLS-1$
			FileOutputStream f = new FileOutputStream(location.toOSString());
			ObjectOutputStream out = new ObjectOutputStream(f);
			out.writeObject(hover);
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Parse Complete"); //$NON-NLS-1$

	}

}
