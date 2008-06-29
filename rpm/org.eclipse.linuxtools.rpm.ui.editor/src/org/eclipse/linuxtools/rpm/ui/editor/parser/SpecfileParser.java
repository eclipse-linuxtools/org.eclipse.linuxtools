/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileLog;
import static org.eclipse.linuxtools.rpm.ui.editor.RpmSections.*;

public class SpecfileParser {
	
	/** These are SRPM-wide sections, and they also cannot have any flags like
	 * -n or -f. Hence they are called simple. This is probably a misleading
	 * name and it should be renamed to reflect that they are SRPM-wide sections.
	 */
	public static String[] simpleSections = { PREP_SECTION, BUILD_SECTION, INSTALL_SECTION,
		CLEAN_SECTION, CHANGELOG_SECTION};

	/**
	 * These are sections that apply to a particular sub-package (i.e. binary
	 * RPM), including the main package. These can also have flags like
	 * -f or -n appended to them, hence they are called complex. This should
	 * probably be renamed to reflect that they are in fact per-RPM sections.
	 */
	private static String[] complexSections = { PRETRANS_SECTION, PRE_SECTION, PREUN_SECTION, POST_SECTION,
		POSTUN_SECTION, POSTTRANS_SECTION, FILES_SECTION, PACKAGE_SECTION, DESCRIPTION_SECTION };
	
	// Fix bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=215771
	//	private static String[] simpleDefinitions = { "Epoch", "Name", "Version",
	//		"Release", "License", "URL" };
	private static String[] simpleDefinitions = { "Epoch", "Name", "Version",
		"Release", "URL" };
	
	private static String[] directValuesDefinitions = { "License" };
	// Note that the ordering here should match that in SpecfileSource#SOURCETYPE
	private static String[] complexDefinitions = { "Source", "Patch" };

	// FIXME:  Handle package-level definitions	
	private static String[] packageLevelDefinitions = { "Summary", "Group",
			"Obsoletes", "Provides", "BuildRequires", "Requires",
			"Requires(pre)", "Requires(post)", "Requires(postun)" };

	private SpecfileErrorHandler errorHandler;

	public Specfile parse(IDocument specfileDocument) {

		// remove all existing markers, if a SpecfileErrorHandler is instantiated.
		if (errorHandler != null)
			errorHandler.removeExistingMarkers();
		LineNumberReader reader = new LineNumberReader(new StringReader(
				specfileDocument.get()));
		String line = "";
		int lineStartPosition = 0;
		Specfile specfile = new Specfile();
		specfile.setDocument(specfileDocument);
		try {
			while ((line = reader.readLine()) != null) {
				// IDocument.getLine(#) is 0-indexed whereas
				// reader.getLineNumber appears to be 1-indexed
				SpecfileElement element = parseLine(line, specfile, reader
						.getLineNumber() - 1);
				if (element != null) {
					element.setLineNumber(reader.getLineNumber() - 1);
					element.setLineStartPosition(lineStartPosition);
					element.setLineEndPosition(lineStartPosition
							+ line.length());
					if ((element.getClass() == SpecfileTag.class)
							&& ((SpecfileTag) element).getName()
									.equals("epoch")) {
						// Epoch
						specfile.setEpoch(((SpecfileTag) element).getIntValue());
                                                specfile.addDefine(new SpecfileDefine("epoch", specfile
                                                        .getEpoch(), specfile));
					} else if ((element.getClass() == SpecfileTag.class)
							&& ((SpecfileTag) element).getName().equals("name")) {
						// Name
						specfile.setName(((SpecfileTag) element)
								.getStringValue());
						specfile.addDefine(new SpecfileDefine("name", specfile
								.getName(), specfile));
					} else if ((element.getClass() == SpecfileTag.class)
							&& ((SpecfileTag) element).getName().equals(
									"version")) {
						// Version
						specfile.setVersion(((SpecfileTag) element)
								.getStringValue());
						specfile.addDefine(new SpecfileDefine("version", specfile
								.getVersion(), specfile));
					} else if ((element.getClass() == SpecfileTag.class)
							&& ((SpecfileTag) element).getName().equals(
									"release")) {
						// Release
						specfile.setRelease(((SpecfileTag) element)
								.getStringValue());
						specfile.addDefine(new SpecfileDefine("release", specfile
								.getRelease(), specfile));
					} else if ((element.getClass() == SpecfileTag.class)
							&& ((SpecfileTag) element).getName().equals(
									"license")) {
						// License
						specfile.setLicense(((SpecfileTag) element)
								.getStringValue());
					} else if ((element.getClass() == SpecfilePatchMacro.class)) {
						SpecfilePatchMacro thisPatchMacro = (SpecfilePatchMacro) element;
						if (thisPatchMacro != null) {
							thisPatchMacro.setSpecfile(specfile);
						}
						SpecfileSource thisPatch = specfile.getPatch(thisPatchMacro.getPatchNumber());
						if (thisPatch != null) {
							thisPatch.addLineUsed(reader.getLineNumber() - 1);
							thisPatch.setSpecfile(specfile);
						}
					} else if ((element.getClass() == SpecfileDefine.class)) {
						specfile.addDefine((SpecfileDefine) element);
					} else if ((element.getClass() == SpecfileSource.class)) {
                                                SpecfileSource source = (SpecfileSource)element;
                                                
						source.setLineNumber(reader.getLineNumber() - 1);
						if (source.getSourceType() == SpecfileSource.SOURCE){
							specfile.addSource(source);
                                                }else{
							specfile.addPatch(source);
                                                }
					}
				}
				// The +1 is for the line delimiter. FIXME: will we end up off
				// by one on the last line?
				lineStartPosition += line.length() + 1;
			}
		} catch (IOException e) {
			// FIXME
			SpecfileLog.logError(e);
		}
		return specfile;
	}
	
	public Specfile parse(String specfileContent) {
		return parse(new Document(specfileContent));
	}

	public SpecfileElement parseLine(String lineText, Specfile specfile,
			int lineNumber) {

		if (lineText.startsWith("%"))
			return parseMacro(lineText, specfile, lineNumber);
		
		for (int i = 0; i < simpleDefinitions.length; i++) {
			if (lineText.startsWith(simpleDefinitions[i] + ":")) {
				if (simpleDefinitions[i].equals("License")) {
					return parseSimpleDefinition(lineText, specfile, lineNumber, true);
				} else
					return parseSimpleDefinition(lineText, specfile, lineNumber, false);
			}
		}
		for (String directValuesDefinition: directValuesDefinitions){
			if (lineText.startsWith(directValuesDefinition + ":")) {
				return parseDirectDefinition(lineText, specfile, lineNumber);
			}
		}
		
		// FIXME:  Handle package-level definitions
		for (int i = 0; i < complexDefinitions.length; i++) {
			if (lineText.startsWith(complexDefinitions[i]))
				return parseComplexDefinition(lineText, specfile, lineNumber, i);
		}

		return null;
	}
	
	private SpecfileElement parseSection(String lineText, Specfile specfile, int lineNumber) {
		List<String> tokens = Arrays.asList(lineText.split("\\s+"));
		SpecfileSection toReturn = null;
		boolean isSimpleSection = false;
		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			String token = iter.next();

			// Sections
			// Simple Section Headers
			for (int i = 0; i < simpleSections.length; i++) {
				if (token.equals(simpleSections[i])) {
					toReturn = new SpecfileSection(token.substring(1), specfile);
					specfile.addSection(toReturn);
					isSimpleSection = true;
				}

			}

			// Complex Section Headers
			for (int i = 0; i < complexSections.length; i++) {
				if (token.equals(complexSections[i])) {
					String name = token.substring(1);
					if (!name.equals("package")) {
						toReturn = new SpecfileSection(name, specfile);
						specfile.addComplexSection(toReturn);
					}	
					while (iter.hasNext()) {
						String nextToken = iter.next();
						if (nextToken.equals("-n")) {
							if (!iter.hasNext()) {
								errorHandler
								.handleError(new SpecfileParseException(
										"No package name after -n in "
										+ name
										+ " section.",
										lineNumber, 0, lineText
										.length(),
										IMarker.SEVERITY_ERROR));
								continue;
							}

							nextToken = iter.next();
							if (nextToken.startsWith("-")) {
								errorHandler
								.handleError(new SpecfileParseException(
										"Package name must not start with '-': "
										+ nextToken + ".",
										lineNumber, 0, lineText
										.length(),
										IMarker.SEVERITY_ERROR));
							}
							
						} else if (nextToken.equals("-p")) {
							// FIXME: rest of line is the actual section
							break;
						} else if (nextToken.equals("-f")) {
							break;
						}
                                                
                                                // this is a package
                                                if (toReturn == null ){
                                                    toReturn = specfile.getPackage(nextToken);
                                                    
                                                    if (toReturn == null){
                                                        toReturn = new SpecfilePackage(nextToken, specfile);
                                                        specfile.addPackage((SpecfilePackage)toReturn);
                                                    }
                                                    return toReturn;
                                                }
                                                
                                                // this is another section
						SpecfilePackage enclosingPackage = specfile.getPackage(nextToken);
						if (enclosingPackage == null){
							enclosingPackage = new SpecfilePackage(nextToken, specfile);
							specfile.addPackage(enclosingPackage);
						}
						toReturn.setPackage(enclosingPackage);
						enclosingPackage.addSection(toReturn);
					}
				}
			}
		}

                // if this package is part of the top level package, add it to it
                if (toReturn != null && toReturn.getPackage() == null){
                    SpecfilePackage topPackage = specfile.getPackage(specfile.getName());
                    if (topPackage == null){
                        topPackage = new SpecfilePackage(specfile.getName(), specfile);
                        specfile.addPackage(topPackage);
                    }
                    if (!isSimpleSection){
                    topPackage.addSection(toReturn);
                    }
                }
                
		return toReturn;
	}

	private SpecfileElement parseMacro(String lineText, Specfile specfile, int lineNumber) {
		// FIXME:  handle other macros
		
		if (lineText.startsWith("%define")) {
			return parseDefine(lineText, specfile, lineNumber);
		} else if (lineText.startsWith("%patch")) {
			return parsePatch(lineText, specfile, lineNumber);
		}
		
		String[] sections = new String[simpleSections.length + complexSections.length];
		System.arraycopy(simpleSections, 0, sections, 0, simpleSections.length);
		System.arraycopy(complexSections, 0, sections, simpleSections.length, complexSections.length);
		for (int i = 0; i < sections.length; i++) {
			if (lineText.startsWith(sections[i]))
				return parseSection(lineText, specfile, lineNumber);
		}
		// FIXME:  add handling of lines containing %{SOURCENNN}
		return null;
	}
	
	private SpecfileElement parsePatch(String lineText, Specfile specfile, int lineNumber) {
		
		SpecfilePatchMacro toReturn = null;
		
		List<String> tokens = Arrays.asList(lineText.split("\\s+"));
		
		for (String token: tokens) {
			// %patchN+
			try {
				if (token.startsWith("%patch")) {
					int patchNumber = 0;
					if (token.length() > 6) {
						patchNumber = Integer.parseInt(token.substring(6));
					}
					toReturn = new SpecfilePatchMacro(patchNumber);
				}
			} catch (NumberFormatException e) {
				errorHandler
				.handleError(new SpecfileParseException(
						"Patch number be an integer.",
						lineNumber, 0, lineText.length(),
						IMarker.SEVERITY_ERROR));
				return null;
			}
		}
		
		return toReturn;
	}

	private SpecfileDefine parseDefine(String lineText, Specfile specfile, int lineNumber) {
		List<String> tokens = Arrays.asList(lineText.split("\\s+"));
		SpecfileDefine toReturn = null;
		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			// Eat the actual "%define" token
			iter.next();
			while (iter.hasNext()) {
				String defineName = iter.next();
				// FIXME: is this true?  investigate in rpmbuild source
				// Definitions must being with a letter
				if (!Character.isLetter(defineName.charAt(0)) && (defineName.charAt(0) != '_')) {
					errorHandler
					.handleError(new SpecfileParseException(
							"Definition lvalue must begin with a letter or an underscore.",
							lineNumber, 0, lineText.length(),
							IMarker.SEVERITY_ERROR));
					return null;
				} else {
					if (!iter.hasNext()) {
						// FIXME: Should this be an error?
						errorHandler
						.handleError(new SpecfileParseException(
								"No value name after define.",
								lineNumber, 0, lineText
								.length(),
								IMarker.SEVERITY_WARNING));
					} else {
						String defineStringValue = iter.next();
						// Defines that are more than one token
						if (iter.hasNext()) {
							defineStringValue = lineText.substring(lineText
									.indexOf(defineStringValue));
							// Eat up the rest of the tokens
							while (iter.hasNext())
								iter.next();
						}
						int defineIntValue = -1;
						try {
							defineIntValue = Integer
							.parseInt(defineStringValue);
						} catch (NumberFormatException e) {
							toReturn = new SpecfileDefine(defineName,
									defineStringValue, specfile);
						}
						if (toReturn == null)
							toReturn = new SpecfileDefine(defineName,
								defineIntValue, specfile);
					}
				}
			}
		}
		return toReturn;
	}

	private SpecfileElement parseComplexDefinition(String lineText, Specfile specfile, int lineNumber, int sourceType) {
		SpecfileSource toReturn = null;
		List<String> tokens = Arrays.asList(lineText.split("\\s+"));
		int number = -1;
		boolean firstToken = true;

		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			String token = iter.next();
			if (token != null && token.length() > 0) {
				if (firstToken) {
					if (token.endsWith(":")) {
						token = token.substring(0, token.length() - 1);
					} else {
						// FIXME:  come up with a better error message here
						// FIXME:  what about descriptions that begin a line with the word "Source" or "Patch"?
						errorHandler
						.handleError(new SpecfileParseException(
								"If this is a Source or Patch directive, it must end with a colon.",
								lineNumber, 0, lineText.length(),
								IMarker.SEVERITY_WARNING));
						return null;
					}
					if (sourceType == SpecfileSource.PATCH) {
						if (token.length() > 5) {
							number = Integer.parseInt(token.substring(5));
							if (!("patch" + number).equalsIgnoreCase(token)) {
								errorHandler
								.handleError(new SpecfileParseException(
										"Invalid patch directive.",
										lineNumber, 0, lineText.length(),
										IMarker.SEVERITY_ERROR));
								return null;
							}
						} else {
							number = 0;
						}
					} else {
						if (token.length() > 6) {
							number = Integer.parseInt(token.substring(6));
							if (!("source" + number).equalsIgnoreCase(token)) {
								errorHandler
								.handleError(new SpecfileParseException(
										"Invalid source directive.",
										lineNumber, 0, lineText.length(),
										IMarker.SEVERITY_ERROR));
								return null;
							}
						} else {
							number = 0;
						}
					}
					toReturn = new SpecfileSource(number, "");
					toReturn.setSourceType(sourceType);
					firstToken = false;
				} else {
					// toReturn should never be null but check just in case
					if (toReturn != null)
						toReturn.setFileName(token);
					if (iter.hasNext()) {
						errorHandler.handleError(new SpecfileParseException(
								"Filename cannot be multiple words.",
								lineNumber, 0, lineText.length(),
								IMarker.SEVERITY_ERROR));
					}
				}
			}
		}
		
		return toReturn;
	}

	private SpecfileElement parseSimpleDefinition(String lineText, Specfile specfile, int lineNumber, boolean warnMultipleValues) {
		List<String> tokens = Arrays.asList(lineText.split("\\s+"));
		SpecfileTag toReturn = null;
		
		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			String token = iter.next();

			if (token.length() <= 0) {
				break;
			}
			
			if (iter.hasNext()) {
				String possValue = iter.next();
				if (possValue.startsWith("%") && iter.hasNext()){
					possValue += ' '+iter.next();
				}
				toReturn = new SpecfileTag(token.substring(0, token.length() - 1).toLowerCase(),
						possValue, specfile);
				if (iter.hasNext() && !warnMultipleValues) {
					errorHandler.handleError(new SpecfileParseException(
							token.substring(0, token.length() - 1) + " cannot have multiple values.",
							lineNumber, 0, lineText.length(),
							IMarker.SEVERITY_ERROR));
					return null;
				}
				// FIXME:  investigate whether we should keep this or not
//				} else {
//					errorHandler.handleError(new SpecfileParseException(
//							token.substring(0, token.length() - 1) + " should be an acronym.",
//							lineNumber, 0, lineText.length(),
//							IMarker.SEVERITY_WARNING));
//				}
			} else {
				errorHandler.handleError(new SpecfileParseException(
						token.substring(0, token.length() - 1) + " declaration without value.", lineNumber,
						0, lineText.length(), IMarker.SEVERITY_ERROR));
				toReturn = null;
			}
		}
		if ((toReturn != null) && (toReturn.getStringValue() != null)) {
			if (toReturn.getStringValue().indexOf("_") > 0) {
				if (toReturn.getName().equalsIgnoreCase("release"))
					errorHandler.handleError(new SpecfileParseException(
							"Release should not contain an underscore.", lineNumber,
							0, lineText.length(), IMarker.SEVERITY_WARNING));
			}
			try {
				int intValue = Integer.parseInt(toReturn.getStringValue());
				toReturn.setIntValue(intValue);
				toReturn.setStringValue(null);
				toReturn.setTagType(SpecfileTag.INT);
			} catch (NumberFormatException e) {
				if (toReturn.getName().equals("epoch")) {
					errorHandler.handleError(new SpecfileParseException(
							"Epoch cannot have non-integer value.", lineNumber,
							0, lineText.length(), IMarker.SEVERITY_ERROR));
					toReturn = null;
				}
			}
		}
		return toReturn;
	}
	
	private SpecfileElement parseDirectDefinition(String lineText,
			Specfile specfile, int lineNumber) {
		String[] parts = lineText.split(":");
		SpecfileTag licenseElement = new SpecfileTag(parts[0].toLowerCase(),parts[1].trim(), specfile);
		licenseElement.setLineNumber(lineNumber);
		return licenseElement;
	}

	public void setErrorHandler(SpecfileErrorHandler specfileErrorHandler) {
		errorHandler = specfileErrorHandler;
	}
}
