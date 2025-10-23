/*******************************************************************************
 * Copyright (c) 2025 Aleksandar Kurtakov and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Aleksandar Kurtakov - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.util.Map;

import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.FuncFoundSaxException;
import org.xml.sax.Attributes;

public class DevhelpContentHandler implements IDevhelpContentHandler {
	private boolean begin;
	private boolean returnType;
	private boolean protoStart;
	private boolean parmStart;
	private boolean descStart;
	private boolean rowIgnore;
	private boolean srcLink;
	private boolean valid = true;
	private Map<String, String> funcs;
	private String returnValue;
	private String funcName;
	private String rowTag;
	private StringBuilder prototype = new StringBuilder();
	private StringBuilder description = new StringBuilder();
	private int divCounter;
	private int rowItemCount;
	private DevHelpSAXParser htmlsaxParser;

	public DevhelpContentHandler(Map<String, String> funcs) {
		this.funcs = funcs;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes a) {
		// look for a tag that matches one of the functions we are trying to find
		if ("A".equals(localName)) { //$NON-NLS-1$
			String classValue = a.getValue("class"); //$NON-NLS-1$
			if (classValue != null) {
				if (classValue.equals("anchor")) { //$NON-NLS-1$
					String href = a.getValue("href"); //$NON-NLS-1$
					if (href != null) {
						if (href.equals("#declaration")) { //$NON-NLS-1$
							String mapName = funcs.get("name"); //$NON-NLS-1$
							if (mapName != null) {
								// We have found one of the functions we are looking for.
								// Register the name for later and allow function parsing to begin.
								funcName = mapName.trim();
								if (funcName.endsWith("()")) { //$NON-NLS-1$
									// Remove () at end of function name and remove all space chars which might be
									// non-breaking space chars which unfortunately do not get caught by the trim()
									// method.
									funcName = this.funcName.replaceAll("\\(\\)", "").replaceAll("\\p{javaSpaceChar}", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											""); //$NON-NLS-1$
								}
								begin = true;
							}
						} else if (href.equals("#description")) { //$NON-NLS-1$
							descStart = true;
						} else if (href.equals("#return-value")) { //$NON-NLS-1$
							descStart = true;
						} else if (href.equals("#parameters")) { //$NON-NLS-1$
							description.append("<br><br><h4>Parameters:</h4>"); //$NON-NLS-1$
							descStart = true;
						}
					}
				} else if (classValue.equals("srclink")) { //$NON-NLS-1$
					srcLink = true;
				}
			}
		}
		if (begin) {
			if ("DIV".equals(localName)) { //$NON-NLS-1$
				++divCounter;
			}
			if (!descStart) {
				if ("SPAN".equals(localName)) { //$NON-NLS-1$
					String type = a.getValue("class"); //$NON-NLS-1$
					if (returnValue == null && type != null && type.equals("n")) { //$NON-NLS-1$
						returnType = true;
					}
				}
			}
			if (protoStart) {
				if ("P".equals(localName)) { //$NON-NLS-1$
					protoStart = false;
					descStart = true;
					description.append("<p>"); //$NON-NLS-1$
				}
			} else if (descStart) {
				if ("P".equals(localName)) { //$NON-NLS-1$
					description.append("<p>"); //$NON-NLS-1$
				} else if ("TABLE".equals(localName)) { //$NON-NLS-1$
					description.append("<dl>"); //$NON-NLS-1$
				} else if ("TR".equals(localName)) { //$NON-NLS-1$
					rowItemCount = 0;
				} else if ("TD".equals(localName)) { //$NON-NLS-1$
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
				} else if ("H4".equals(localName)) { //$NON-NLS-1$
//                    description.append("<br>"); //$NON-NLS-1$
				}
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (srcLink) {
			if ("A".equals(localName)) { //$NON-NLS-1$
				srcLink = false;
			}
		} else if (begin) {
			if ("DIV".equals(localName)) { //$NON-NLS-1$
				--divCounter;
				if (divCounter <= 0) {
					// We have finished parsing the current area, reset all flags
					descStart = false;
					parmStart = false;
					protoStart = false;
				}
			} else if ("SECTION".equals(localName)) { //$NON-NLS-1$
				// We have finished parsing the function
				// If valid, create and save the function info
				if (valid && returnValue != null && !returnValue.startsWith("#") && //$NON-NLS-1$
						!returnValue.startsWith("typedef ")) { //$NON-NLS-1$
					FunctionInfo info = new FunctionInfo(funcName);
					info.setReturnType(returnValue);
					info.setPrototype(prototype.toString());
					info.setDescription(description.toString());
					htmlsaxParser.getFunctionInfos().put(funcName, info);
					throw new FuncFoundSaxException(); // indicate we are done and stop parser
				}
			}
			if (descStart) {
				if ("P".equals(localName)) {//$NON-NLS-1$
					description.append("</p>"); //$NON-NLS-1$
				} else if ("TABLE".equals(localName)) { //$NON-NLS-1$
					description.append("</dl>"); //$NON-NLS-1$
				} else if ("TR".equals(localName)) { //$NON-NLS-1$
					rowItemCount = 0;
				} else if ("TD".equals(localName)) { //$NON-NLS-1$
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
	public void characters(char[] ch, int start, int length) {
		if (begin && !srcLink) {
			if (returnType) {
				returnValue = ""; //$NON-NLS-1$
				String tmp = new String(ch).trim();
				boolean completed = false;
				if (tmp.endsWith(");")) { //$NON-NLS-1$
					completed = true;
					tmp = tmp.substring(0, tmp.length() - 2);
				}
				String[] tokens = tmp.split("\\s+"); //$NON-NLS-1$
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
				String temp = new String(ch).trim();
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
				String parmData = new String(ch).trim();
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
					description.append(String.valueOf(ch));
				}
			}
		}

	}

	@Override
	public String toString() {
		return "funcName: <" + funcName + "> returnType: <" + returnValue + //$NON-NLS-1$ //$NON-NLS-2$
				"> prototype: <" + prototype + "> description: " + description; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void setHtmlsaxParser(DevHelpSAXParser htmlsaxParser) {
		this.htmlsaxParser = htmlsaxParser;
	}
	
}
