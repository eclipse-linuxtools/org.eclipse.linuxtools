/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
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

import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.BUILD_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CHANGELOG_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CLEAN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.DESCRIPTION_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.FILES_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.INSTALL_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PACKAGE_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POSTTRANS_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POSTUN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POST_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PREP_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PRETRANS_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PREUN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PRE_SECTION;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileSpecialSymbols;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.Messages;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileParseException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfilePatchMacro;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource.SourceType;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileTag;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileTaskHandler;

public class SpecfileParser {

	private static final String DEFINE_SEPARATOR = ":"; //$NON-NLS-1$

	private static final String SPACE_REGEX = "\\s+"; //$NON-NLS-1$

	/**
	 * These are SRPM-wide sections, and they also cannot have any flags like -n
	 * or -f. Hence they are called simple. This is probably a misleading name
	 * and it should be renamed to reflect that they are SRPM-wide sections.
	 */
	public static final String[] simpleSections = { PREP_SECTION, BUILD_SECTION,
			INSTALL_SECTION, CLEAN_SECTION, CHANGELOG_SECTION };

	/**
	 * These are sections that apply to a particular sub-package (i.e. binary
	 * RPM), including the main package. These can also have flags like -f or -n
	 * appended to them, hence they are called complex. This should probably be
	 * renamed to reflect that they are in fact per-RPM sections.
	 */
	private static String[] complexSections = { PRETRANS_SECTION, PRE_SECTION,
			PREUN_SECTION, POST_SECTION, POSTUN_SECTION, POSTTRANS_SECTION,
			FILES_SECTION, PACKAGE_SECTION, DESCRIPTION_SECTION };

	private static String[] simpleDefinitions = { RpmTags.EPOCH, RpmTags.NAME,
			RpmTags.VERSION, RpmTags.RELEASE, RpmTags.URL, RpmTags.BUILD_ARCH };

	private static String[] directValuesDefinitions = { RpmTags.LICENSE,
			RpmTags.BUILD_ROOT };
	// Note that the ordering here should match that in
	// SpecfileSource#SOURCETYPE
	private static String[] complexDefinitions = { "Source", "Patch" }; //$NON-NLS-1$ //$NON-NLS-2$

	private static String[] packageLevelDefinitions = { RpmTags.SUMMARY,
			RpmTags.GROUP, RpmTags.OBSOLETES, RpmTags.PROVIDES,
			RpmTags.REQUIRES, RpmTags.REQUIRES_PRE, RpmTags.REQUIRES_POST,
			RpmTags.REQUIRES_POSTUN };

	private SpecfileErrorHandler errorHandler;
	private SpecfileTaskHandler taskHandler;
	private IPreferenceStore store;
	private SpecfileSection lastSection;
	private SpecfilePackage activePackage;

	public SpecfileParser() {
		store = Activator.getDefault().getPreferenceStore();
	}

	public Specfile parse(IDocument specfileDocument) {

		// remove all existing markers, if a SpecfileErrorHandler is
		// instantiated.
		if (errorHandler != null) {
			errorHandler.removeExistingMarkers();
		}
		if (taskHandler != null) {
			taskHandler.removeExistingMarkers();
		}
		LineNumberReader reader = new LineNumberReader(new StringReader(
				specfileDocument.get()));
		String line = ""; //$NON-NLS-1$
		int lineStartPosition = 0;
		Specfile specfile = new Specfile();
		specfile.setDocument(specfileDocument);
		try {
			while ((line = reader.readLine()) != null) {
				if (taskHandler != null) {
					generateTaskMarker(reader.getLineNumber() - 1, line);
				}
				// IDocument.getLine(#) is 0-indexed whereas
				// reader.getLineNumber appears to be 1-indexed
				SpecfileElement element = parseLine(line, specfile, reader
						.getLineNumber() - 1);
				if (element != null) {
					element.setLineNumber(reader.getLineNumber() - 1);
					element.setLineStartPosition(lineStartPosition);
					element.setLineEndPosition(lineStartPosition
							+ line.length());
					if (element.getClass() == SpecfileTag.class) {
						SpecfileTag tag = (SpecfileTag) element;
						specfile.addDefine(tag);
					} else if ((element.getClass() == SpecfilePatchMacro.class)) {
						SpecfilePatchMacro thisPatchMacro = (SpecfilePatchMacro) element;
						if (thisPatchMacro != null) {
							thisPatchMacro.setSpecfile(specfile);
						}
						SpecfileSource thisPatch = specfile
								.getPatch(thisPatchMacro.getPatchNumber());
						if (thisPatch != null) {
							thisPatch.addLineUsed(reader.getLineNumber() - 1);
							thisPatch.setSpecfile(specfile);
						}
					} else if ((element.getClass() == SpecfileDefine.class)) {
						specfile.addDefine((SpecfileDefine) element);
					} else if ((element.getClass() == SpecfileSource.class)) {
						SpecfileSource source = (SpecfileSource) element;

						source.setLineNumber(reader.getLineNumber() - 1);
						if (source.getSourceType() == SpecfileSource.SourceType.SOURCE) {
							specfile.addSource(source);
						} else {
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

	private void generateTaskMarker(int lineNumber, String line) {
		String[] taskTags = store.getString(PreferenceConstants.P_TASK_TAGS)
				.split(";"); //$NON-NLS-1$
		int commentCharIndex = line
				.indexOf(ISpecfileSpecialSymbols.COMMENT_START);
		if (commentCharIndex > -1) {
			for (String item : taskTags) {
				int taskIndex = line.indexOf(item);
				if (taskIndex > commentCharIndex) {
					taskHandler.handleTask(lineNumber, line, item);
				}
			}
		}
	}

	public Specfile parse(String specfileContent) {
		return parse(new Document(specfileContent));
	}

	public SpecfileElement parseLine(String lineText, Specfile specfile,
			int lineNumber) {

		if (lineText.startsWith("%")) {//$NON-NLS-1$
			return parseMacro(lineText, specfile, lineNumber);
		}

		for (String simpleDefinition : simpleDefinitions) {
			if (lineText.startsWith(simpleDefinition + DEFINE_SEPARATOR)) {
				return parseSimpleDefinition(lineText, specfile, lineNumber,
						false);
			}
		}
		for (String directValuesDefinition : directValuesDefinitions) {
			if (lineText.startsWith(directValuesDefinition + DEFINE_SEPARATOR)) {
				return parseDirectDefinition(lineText, specfile, lineNumber);
			}
		}
		for (String directValuesDefinition : packageLevelDefinitions) {
			if (lineText.startsWith(directValuesDefinition + DEFINE_SEPARATOR)) {
				SpecfileElement definition = parseDirectDefinition(lineText,
						specfile, lineNumber);
				if (directValuesDefinition.equals(RpmTags.REQUIRES)) {
					if (activePackage != null) {
						activePackage.addRequire((SpecfileTag) definition);
					} else {
						specfile.addRequire((SpecfileTag) definition);
					}
				}
				return definition;
			}
		}

		if (lineText.startsWith(complexDefinitions[0])
				&& lineText.contains(DEFINE_SEPARATOR)) {
			return parseComplexDefinition(lineText, lineNumber,
					SourceType.SOURCE);
		} else if (lineText.startsWith(complexDefinitions[1])
				&& lineText.contains(DEFINE_SEPARATOR)) {
			return parseComplexDefinition(lineText, lineNumber,
					SourceType.PATCH);
		} else if (lineText.startsWith("BuildRequires")) { //$NON-NLS-1$
			return parseBuildRequire(lineText, lineNumber, specfile);
		}

		return null;
	}

	private SpecfileElement parseBuildRequire(String lineText, int lineNumber,
			Specfile specfile) {
		String value = lineText.substring(lineText.indexOf(':') + 1,
				lineText.length()).trim();
		SpecfileDefine buildRequire = new SpecfileDefine(
				"BuildRequires", value, specfile, null); //$NON-NLS-1$
		buildRequire.setLineNumber(lineNumber);
		specfile.addBuildRequire(buildRequire);
		return buildRequire;
	}

	private SpecfileSection parseSection(String lineText, Specfile specfile,
			int lineNumber) {
		List<String> tokens = Arrays.asList(lineText.split(SPACE_REGEX));
		SpecfileSection toReturn = null;
		boolean isSimpleSection = false;
		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			String token = iter.next();

			// Sections
			// Simple Section Headers
			for (String simpleSection : simpleSections) {
				if (token.equals(simpleSection)) {
					toReturn = new SpecfileSection(token.substring(1), specfile);
					specfile.addSection(toReturn);
					isSimpleSection = true;
				}

			}

			// Complex Section Headers
			for (String complexSection : complexSections) {
				if (token.equals(complexSection)) {
					String name = token.substring(1);
					if (!name.equals("package")) { //$NON-NLS-1$
						toReturn = new SpecfileSection(name, specfile);
						specfile.addComplexSection(toReturn);
					}
					while (iter.hasNext()) {
						String nextToken = iter.next();
						if (nextToken.equals("-n")) { //$NON-NLS-1$
							if (!iter.hasNext()) {
								errorHandler
										.handleError(new SpecfileParseException(
												Messages
														.getString("SpecfileParser.1") //$NON-NLS-1$
														+ name
														+ Messages
																.getString("SpecfileParser.2"), //$NON-NLS-1$
												lineNumber, 0, lineText
														.length(),
												IMarker.SEVERITY_ERROR));
								continue;
							}

							nextToken = iter.next();
							if (nextToken.startsWith("-")) { //$NON-NLS-1$
								errorHandler
										.handleError(new SpecfileParseException(
												Messages
														.getString("SpecfileParser.3") //$NON-NLS-1$
														+ nextToken
														+ Messages
																.getString("SpecfileParser.4"), //$NON-NLS-1$
												lineNumber, 0, lineText
														.length(),
												IMarker.SEVERITY_ERROR));
							}

						} else if (nextToken.equals("-p")) { //$NON-NLS-1$
							// FIXME: rest of line is the actual section
							break;
						} else if (nextToken.equals("-f")) { //$NON-NLS-1$
							break;
						}

						// this is a package
						if (toReturn == null) {
							SpecfilePackage tmpPackage = specfile
									.getPackage(nextToken);

							if (tmpPackage == null) {
								tmpPackage = new SpecfilePackage(nextToken,
										specfile);
								specfile.addPackage(tmpPackage);
							}
							activePackage = tmpPackage;
							return tmpPackage;
						}

						// this is another section
						SpecfilePackage enclosingPackage = specfile
								.getPackage(nextToken);
						if (enclosingPackage == null) {
							enclosingPackage = new SpecfilePackage(nextToken,
									specfile);
							specfile.addPackage(enclosingPackage);
						}
						toReturn.setPackage(enclosingPackage);
						enclosingPackage.addSection(toReturn);
					}
				}
			}
		}

		// if this package is part of the top level package, add it to
		// it
		if (toReturn != null && toReturn.getPackage() == null) {
			SpecfilePackage topPackage = specfile
					.getPackage(specfile.getName());
			if (topPackage == null) {
				topPackage = new SpecfilePackage(specfile.getName(), specfile);
				specfile.addPackage(topPackage);
			}
			if (!isSimpleSection) {
				topPackage.addSection(toReturn);
			}
		}
		if (lastSection != null) {
			lastSection.setSectionEndLine(lineNumber);
		}
		return toReturn;
	}

	private SpecfileElement parseMacro(String lineText, Specfile specfile,
			int lineNumber) {
		// FIXME: handle other macros

		if (lineText.startsWith("%define") || lineText.startsWith("%global")) { //$NON-NLS-1$ //$NON-NLS-2$
			return parseDefine(lineText, specfile, lineNumber);
		} else if (lineText.startsWith("%patch")) { //$NON-NLS-1$
			return parsePatch(lineText, lineNumber);
		}

		String[] sections = new String[simpleSections.length
				+ complexSections.length];
		System.arraycopy(simpleSections, 0, sections, 0, simpleSections.length);
		System.arraycopy(complexSections, 0, sections, simpleSections.length,
				complexSections.length);
		for (String section : sections) {
			if (lineText.startsWith(section)) {
				lastSection = parseSection(lineText, specfile, lineNumber);
				if (lastSection != null) {
					lastSection.setSectionEndLine(lineNumber + 1);
				}
				return lastSection;
			}
		}
		// FIXME: add handling of lines containing %{SOURCENNN}
		return null;
	}

	private SpecfileElement parsePatch(String lineText, int lineNumber) {

		SpecfilePatchMacro toReturn = null;

		List<String> tokens = Arrays.asList(lineText.split(SPACE_REGEX));

		for (String token : tokens) {
			// %patchN+
			try {
				if (token.startsWith("%patch")) { //$NON-NLS-1$
					int patchNumber = 0;
					if (token.length() > 6) {
						patchNumber = Integer.parseInt(token.substring(6));
					}
					toReturn = new SpecfilePatchMacro(patchNumber);
				}
			} catch (NumberFormatException e) {
				errorHandler.handleError(new SpecfileParseException(
						Messages.getString("SpecfileParser.5"), //$NON-NLS-1$
						lineNumber, 0, lineText.length(),
						IMarker.SEVERITY_ERROR));
				return null;
			}
		}

		return toReturn;
	}

	private SpecfileDefine parseDefine(String lineText, Specfile specfile,
			int lineNumber) {
		List<String> tokens = Arrays.asList(lineText.split(SPACE_REGEX));
		SpecfileDefine toReturn = null;
		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			// Eat the actual "%define" or "%global" token
			iter.next();
			while (iter.hasNext()) {
				String defineName = iter.next();
				// FIXME: is this true? investigate in rpmbuild source
				// Definitions must being with a letter
				if (!Character.isLetter(defineName.charAt(0))
						&& (defineName.charAt(0) != '_')) {
					errorHandler.handleError(new SpecfileParseException(
							Messages.getString("SpecfileParser.6"), //$NON-NLS-1$
							lineNumber, 0, lineText.length(),
							IMarker.SEVERITY_ERROR));
					return null;
				} else {
					if (iter.hasNext()) {
						String defineStringValue = iter.next();
						// Defines that are more than one token
						if (iter.hasNext()) {
							defineStringValue = lineText.substring(lineText
									.indexOf(defineStringValue));
							// Eat up the rest of the tokens
							while (iter.hasNext()) {
								iter.next();
							}
						}
						int defineIntValue = -1;
						try {
							defineIntValue = Integer
									.parseInt(defineStringValue);
						} catch (NumberFormatException e) {
							toReturn = new SpecfileDefine(defineName,
									defineStringValue, specfile, null);
						}
						if (toReturn == null) {
							toReturn = new SpecfileDefine(defineName,
									defineIntValue, specfile, null);
						}
					} else {
						errorHandler.handleError(new SpecfileParseException(defineName+
								Messages.getString("SpecfileParser.14"), //$NON-NLS-1$
								lineNumber, 0, lineText.length(),
								IMarker.SEVERITY_ERROR));
					}
				}
			}
		}
		return toReturn;
	}

	private SpecfileElement parseComplexDefinition(String lineText,
			int lineNumber, SourceType sourceType) {
		SpecfileSource toReturn = null;
		List<String> tokens = Arrays.asList(lineText.split(SPACE_REGEX));
		int number = -1;
		boolean firstToken = true;

		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			String token = iter.next();
			if (token != null && token.length() > 0) {
				if (firstToken) {
					if (token.endsWith(DEFINE_SEPARATOR)) {
						token = token.substring(0, token.length() - 1);
					} else {
						// FIXME: come up with a better error message here
						// FIXME: what about descriptions that begin a line with
						// the word "Source" or "Patch"?
						errorHandler.handleError(new SpecfileParseException(
								Messages.getString("SpecfileParser.8"), //$NON-NLS-1$
								lineNumber, 0, lineText.length(),
								IMarker.SEVERITY_WARNING));
						return null;
					}
					if (sourceType == SourceType.PATCH) {
						if (token.length() > 5) {
							number = Integer.parseInt(token.substring(5));
							if (!("patch" + number).equalsIgnoreCase(token)) { //$NON-NLS-1$
								errorHandler
										.handleError(new SpecfileParseException(
												Messages
														.getString("SpecfileParser.10"), //$NON-NLS-1$
												lineNumber, 0, lineText
														.length(),
												IMarker.SEVERITY_ERROR));
								return null;
							}
						} else {
							number = 0;
						}
					} else {
						if (token.length() > 6) {
							number = Integer.parseInt(token.substring(6));
							if (!("source" + number).equalsIgnoreCase(token)) { //$NON-NLS-1$
								errorHandler
										.handleError(new SpecfileParseException(
												Messages
														.getString("SpecfileParser.11"), //$NON-NLS-1$
												lineNumber, 0, lineText
														.length(),
												IMarker.SEVERITY_ERROR));
								return null;
							}
						} else {
							number = 0;
						}
					}
					toReturn = new SpecfileSource(number, ""); //$NON-NLS-1$
					toReturn.setSourceType(sourceType);
					firstToken = false;
				} else {
					// toReturn should never be null but check just in case
					if (toReturn != null) {
						toReturn.setFileName(token);
					}
					if (iter.hasNext()) {
						errorHandler.handleError(new SpecfileParseException(
								Messages.getString("SpecfileParser.12"), //$NON-NLS-1$
								lineNumber, 0, lineText.length(),
								IMarker.SEVERITY_ERROR));
					}
				}
			}
		}

		return toReturn;
	}

	private SpecfileElement parseSimpleDefinition(String lineText,
			Specfile specfile, int lineNumber, boolean warnMultipleValues) {
		List<String> tokens = Arrays.asList(lineText.split(SPACE_REGEX));
		SpecfileTag toReturn = null;

		for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
			String token = iter.next();

			if (token.length() <= 0) {
				break;
			}

			if (iter.hasNext()) {
				String possValue = iter.next();
				if (possValue.startsWith("%") && iter.hasNext()) { //$NON-NLS-1$
					possValue += ' ' + iter.next();
				}
				toReturn = new SpecfileTag(token.substring(0,
						token.length() - 1).toLowerCase(), possValue, specfile,
						null);
				if (iter.hasNext() && !warnMultipleValues) {
					errorHandler.handleError(new SpecfileParseException(
							token.substring(0, token.length() - 1)
									+ Messages.getString("SpecfileParser.13"), //$NON-NLS-1$
							lineNumber, 0, lineText.length(),
							IMarker.SEVERITY_ERROR));
					return null;
				}
			} else {
				errorHandler.handleError(new SpecfileParseException(token
						.substring(0, token.length() - 1)
						+ Messages.getString("SpecfileParser.14"), lineNumber, //$NON-NLS-1$
						0, lineText.length(), IMarker.SEVERITY_ERROR));
				toReturn = null;
			}
		}
		if ((toReturn != null) && (toReturn.getStringValue() != null)) {
			try {
				int intValue = Integer.parseInt(toReturn.getStringValue());
				toReturn.setValue(intValue);
			} catch (NumberFormatException e) {
				if (toReturn.getName().equalsIgnoreCase(RpmTags.EPOCH)) {
					errorHandler
							.handleError(new SpecfileParseException(
									Messages.getString("SpecfileParser.16"), lineNumber, //$NON-NLS-1$
									0, lineText.length(),
									IMarker.SEVERITY_ERROR));
					toReturn = null;
				}
			}
		}
		return toReturn;
	}

	private SpecfileElement parseDirectDefinition(String lineText,
			Specfile specfile, int lineNumber) {
		String[] parts = lineText.split(DEFINE_SEPARATOR, 2);
		SpecfileTag directDefinition;
		if (parts.length == 2) {
			directDefinition = new SpecfileTag(parts[0], parts[1].trim(),
					specfile, activePackage);
			directDefinition.setLineNumber(lineNumber);
		} else {
			errorHandler.handleError(new SpecfileParseException(parts[0]
					+ Messages.getString("SpecfileParser.14"), lineNumber, //$NON-NLS-1$
					0, lineText.length(), IMarker.SEVERITY_ERROR));
			directDefinition = null;
		}
		return directDefinition;
	}

	public void setErrorHandler(SpecfileErrorHandler specfileErrorHandler) {
		errorHandler = specfileErrorHandler;
	}

	public void setTaskHandler(SpecfileTaskHandler specfileTaskHandler) {
		taskHandler = specfileTaskHandler;
	}
}
