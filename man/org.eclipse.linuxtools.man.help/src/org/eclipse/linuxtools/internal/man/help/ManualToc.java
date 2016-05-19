/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.help;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.linuxtools.internal.man.Activator;
import org.eclipse.linuxtools.internal.man.parser.ManParser;

/**
 * A table of contents that will have one topic for every manual section that
 * contains at least one installed man page. Each of those topics will have one
 * sub-topic for manual page in the section.
 */
public class ManualToc implements IToc {

	private final Map<String, SectionTopic> sections = new HashMap<>();

	@Override
	public ITopic[] getTopics() {
		if (sections.isEmpty()) {
			generateSections();
		}

		List<SectionTopic> sectionList = new ArrayList<>(sections.values());
		Collections.sort(sectionList);
		return sectionList.toArray(new SectionTopic[sectionList.size()]);
	}

	private void generateSections() {
		// Filter to make sure we only get manual section directories
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			private final Pattern p = Pattern.compile("^man\\d[xp]?"); //$NON-NLS-1$

			@Override
			public boolean accept(Path path) throws IOException {
				Matcher m = p.matcher(path.getFileName().toString());
				return Files.isDirectory(path) && m.matches();
			}
		};

		// Search all man paths in the order that man would, adding sections and
		// pages as we encounter them
		List<Path> manPaths = ManParser.getManPaths();
		for (Path manPath : manPaths) {
			if (Files.notExists(manPath)) {
				continue;
			}
			try (DirectoryStream<Path> manPathStream = Files
					.newDirectoryStream(manPath, filter)) {
				for (Path sectionPath : manPathStream) {
					try (DirectoryStream<Path> sectionPathStream = Files
							.newDirectoryStream(sectionPath)) {
						for (Path pagePath : sectionPathStream) {
							String pageName = pagePath.getFileName().toString();
							if (pageName.endsWith(".gz")) { //$NON-NLS-1$
								pageName = pageName.substring(0,
										pageName.length() - 3);
							}

							// Add page to section
							int dot = pageName.lastIndexOf('.');
							String sectionId = pageName.substring(dot + 1,
									pageName.length());
							String pageId = pageName.substring(0, dot);
							addSectionPage(sectionId, pageId);
						}
					}
				}
			} catch (IOException e) {
				Status status = new Status(IStatus.ERROR, e.getMessage(),
						Activator.getDefault().getPluginId());
				Activator.getDefault().getLog().log(status);
			}
		}
	}

	@Override
	public ITopic getTopic(String href) {
		return null;
	}

	@Override
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	@Override
	public IUAElement[] getChildren() {
		return getTopics();
	}

	@Override
	public String getHref() {
		return null;
	}

	@Override
	public String getLabel() {
		return Messages.ManualToc_TocLabel;
	}

	private void addSectionPage(String sectionId, String pageId) {
		StringBuilder label = new StringBuilder();
		String displaySectionId = sectionId;

		// Decide section label
		switch (sectionId.substring(0, 1)) {
		case "0": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section0);
			break;
		case "1": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section1);
			break;
		case "2": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section2);
			break;
		case "3": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section3);
			break;
		case "4": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section4);
			break;
		case "5": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section5);
			break;
		case "6": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section6);
			break;
		case "7": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section7);
			break;
		case "8": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section8);
			break;
		case "9": //$NON-NLS-1$
			label.append(Messages.ManualToc_Section9);
			break;
		}

		// Decide sub-section label
		switch (sectionId.substring(1, sectionId.length())) {
		case "am": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionAM);
			break;
		case "G": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionG);
			break;
		case "p": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionP);
			break;
		case "pm": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionPM);
			break;
		case "python": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionPY);
			break;
		case "x": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionX);
			break;
		case "ssl": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionSSL);
			break;
		case "stap": //$NON-NLS-1$
			label.append(Messages.ManualToc_SectionSTAP);
			break;
		default:
			// If the sub section is not known to us, truncate it and just
			// display the page in the main section
			displaySectionId = sectionId.substring(0, 1);
			break;
		}

		SectionTopic section = sections.get(displaySectionId);
		if (section == null) {
			section = new SectionTopic(displaySectionId, label.toString());
			sections.put(displaySectionId, section);
		}
		section.addPage(sectionId, pageId);
	}
}
