/*******************************************************************************
 * Copyright (c) 2007, 2019 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Neil Guzman - autocomplete with "." and "+" in name (B#375195)
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;

/**
 * Content assist processor.
 */
public class SpecfileCompletionProcessor implements IContentAssistProcessor {

	private static final String SOURCE = "SOURCE"; //$NON-NLS-1$

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Comparator implementation for a generic proposal
	 *
	 * @author Sami Wagiaalla
	 */
	private static final String TEMPLATE_ICON = "icons/template_obj.gif"; //$NON-NLS-1$

	private static final String MACRO_ICON = "icons/macro_obj.gif"; //$NON-NLS-1$

	private static final String PATCH_ICON = "icons/macro_obj.gif"; //$NON-NLS-1$

	private static final String PACKAGE_ICON = "icons/rpm.gif"; //$NON-NLS-1$

	private static final String PREAMBLE_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.preambleSection"; //$NON-NLS-1$

	private static final String PRE_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.preSection"; //$NON-NLS-1$

	private static final String BUILD_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.buildSection"; //$NON-NLS-1$

	private static final String INSTALL_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.installSection"; //$NON-NLS-1$

	private static final String CHANGELOG_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.changelogSection"; //$NON-NLS-1$

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<ICompletionProposal> result = new ArrayList<>();

		Specfile specfile = new SpecfileParser().parse(viewer.getDocument());
		String prefix = completionWord(viewer.getDocument(), offset);
		Region region = new Region(offset - prefix.length(), prefix.length());
		// RPM macro's are useful in the whole specfile.
		List<ICompletionProposal> rpmMacroProposals = computeRpmMacroProposals(region, specfile, prefix);
		// Sources completion
		List<ICompletionProposal> sourcesProposals = computeSourcesProposals(region, specfile, prefix);
		result.addAll(sourcesProposals);
		// Get the current content type
		String currentContentType = viewer.getDocument().getDocumentPartitioner().getContentType(region.getOffset());
		if (currentContentType.equals(SpecfilePartitionScanner.SPEC_PREP)) {
			List<ICompletionProposal> patchesProposals = computePatchesProposals(region, specfile, prefix);
			result.addAll(patchesProposals);
		}

		if (currentContentType.equals(SpecfilePartitionScanner.SPEC_PACKAGES)) {
			// don't show template in the RPM packages content type.
			// (when the line begin with Requires, BuildRequires etc...)
			List<ICompletionProposal> rpmPackageProposals = computeRpmPackageProposals(region, prefix);
			result.addAll(rpmPackageProposals);
			result.addAll(rpmMacroProposals);
		} else {
			// don't show RPM packages proposals in all others content type.
			List<? extends ICompletionProposal> templateProposals = computeTemplateProposals(viewer, region, specfile,
					prefix);
			result.addAll(templateProposals);
			result.addAll(rpmMacroProposals);
		}
		if (currentContentType.equals(SpecfilePartitionScanner.SPEC_GROUP)) {
			IDocument document = viewer.getDocument();
			try {
				int lineNumber = document.getLineOfOffset(region.getOffset());
				int lineOffset = document.getLineOffset(lineNumber);
				if (region.getOffset() - lineOffset > 5) {
					result.clear();
					String groupPrefix = getGroupPrefix(viewer, offset);
					result.addAll(computeRpmGroupProposals(region, groupPrefix));
				}
			} catch (BadLocationException e) {
				SpecfileLog.logError(e);
			}
		}

		return result.toArray(new ICompletionProposal[0]);
	}

	/**
	 * Compute the templates proposals, these proposals are contextual on sections.
	 * Return an array of template proposals for the given viewer, region, specfile,
	 * prefix.
	 *
	 * @param viewer   the viewer for which the context is created
	 * @param region   the region into <code>document</code> for which the context
	 *                 is created
	 * @param specfile the specfile element
	 * @param prefix   the prefix string
	 * @return a ICompletionProposal[]
	 */
	private List<? extends ICompletionProposal> computeTemplateProposals(ITextViewer viewer, IRegion region,
			Specfile specfile, String prefix) {
		TemplateContext context = createContext(viewer, region, specfile);
		List<TemplateProposal> matches = new ArrayList<>();
		if (context == null) {
			return matches;
		}
		String id = context.getContextType().getId();
		Template[] templates = Activator.getDefault().getTemplateStore().getTemplates(id);
		for (Template template : templates) {
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			int relevance = getRelevance(template, prefix);
			if (relevance > 0) {
				matches.add(new TemplateProposal(template, context, region,
						Activator.getDefault().getImage(TEMPLATE_ICON), relevance));
			}
		}
		Collections.sort(matches, (t1, t2) -> (t2.getRelevance() - t1.getRelevance()));
		return matches;
	}

	/**
	 * Compute RPM macro proposals, these proposals are usable in the whole
	 * document. Return an array of RPM macro proposals for the given viewer,
	 * region, prefix.
	 *
	 * @param viewer the viewer for which the context is created
	 * @param region the region into <code>document</code> for which the context is
	 *               created
	 * @param prefix the prefix string to find
	 * @return a ICompletionProposal[]
	 */
	private List<ICompletionProposal> computeRpmMacroProposals(IRegion region, Specfile specfile, String prefix) {
		Map<String, String> rpmMacroProposalsMap = Activator.getDefault().getRpmMacroList().getProposals(prefix);

		// grab defines and put them into the proposals map
		rpmMacroProposalsMap.putAll(getDefines(specfile, prefix));

		ArrayList<ICompletionProposal> proposals = new ArrayList<>();
		for (Map.Entry<String, String> entry : rpmMacroProposalsMap.entrySet()) {
			proposals.add(new SpecCompletionProposal(
					ISpecfileSpecialSymbols.MACRO_START_LONG + entry.getKey().substring(1)
							+ ISpecfileSpecialSymbols.MACRO_END_LONG,
					region.getOffset(), region.getLength(), entry.getKey().length() + 2,
					Activator.getDefault().getImage(MACRO_ICON), entry.getKey(), null, entry.getValue()));
		}
		return proposals;
	}

	/**
	 * Compute patches proposals, these proposals are usable in the whole document.
	 * Return an array of patches proposals for the given viewer, region, prefix.
	 *
	 * @param viewer the viewer for which the context is created
	 * @param region the region into <code>document</code> for which the context is
	 *               created
	 * @param prefix the prefix string to find
	 * @return a ICompletionProposal[]
	 */
	private List<ICompletionProposal> computePatchesProposals(IRegion region, Specfile specfile, String prefix) {
		// grab patches and put them into the proposals map
		Map<String, String> patchesProposalsMap = getPatches(specfile, prefix);
		ArrayList<ICompletionProposal> proposals = new ArrayList<>();
		for (Map.Entry<String, String> entry : patchesProposalsMap.entrySet()) {
			proposals.add(new SpecCompletionProposal(entry.getKey(), region.getOffset(), region.getLength(),
					entry.getKey().length(), Activator.getDefault().getImage(PATCH_ICON), entry.getKey(), null,
					entry.getValue()));
		}
		return proposals;
	}

	/**
	 * Compute sources proposals, these proposals are usable in the whole document.
	 * Return an array of sources proposals for the given viewer, region, prefix.
	 *
	 * @param viewer the viewer for which the context is created
	 * @param region the region into <code>document</code> for which the context is
	 *               created
	 * @param prefix the prefix string to find
	 * @return a ICompletionProposal[]
	 */
	private List<ICompletionProposal> computeSourcesProposals(IRegion region, Specfile specfile, String prefix) {
		// grab patches and put them into the proposals map
		Map<String, String> sourcesProposalsMap = getSources(specfile, prefix);
		ArrayList<ICompletionProposal> proposals = new ArrayList<>();
		for (Map.Entry<String, String> entry : sourcesProposalsMap.entrySet()) {
			proposals.add(new SpecCompletionProposal(entry.getKey(), region.getOffset(), region.getLength(),
					entry.getKey().length(), Activator.getDefault().getImage(PATCH_ICON), entry.getKey(), null,
					entry.getValue()));
		}
		return proposals;
	}

	/**
	 * Compute RPM package proposals, these proposals are usable only in the
	 * preambule section. Return an array of RPM macro proposals for the given
	 * viewer, region, specfile, prefix.
	 *
	 * @param viewer the viewer for which the context is created
	 * @param region the region into <code>document</code> for which the context is
	 *               created
	 * @param prefix the prefix string
	 * @return a ICompletionProposal[]
	 */
	private List<ICompletionProposal> computeRpmPackageProposals(IRegion region, String prefix) {
		List<String[]> rpmPkgProposalsList = Activator.getDefault().getRpmPackageList().getProposals(prefix);
		ArrayList<ICompletionProposal> proposals = new ArrayList<>();
		if (rpmPkgProposalsList != null) {
			for (String[] item : rpmPkgProposalsList) {
				proposals.add(new SpecCompletionProposal(item[0], region.getOffset(), region.getLength(),
						item[0].length(), Activator.getDefault().getImage(PACKAGE_ICON), item[0], null, item[1]));
			}
		}

		Collections.sort(proposals, (a, b) -> a.getDisplayString().compareToIgnoreCase(b.getDisplayString()));
		return proposals;
	}

	private List<ICompletionProposal> computeRpmGroupProposals(IRegion region, String prefix) {
		List<String> rpmGroupProposalsList = Activator.getDefault().getRpmGroups();
		ArrayList<ICompletionProposal> proposals = new ArrayList<>();
		for (String item : rpmGroupProposalsList) {
			if (item.startsWith(prefix)) {
				proposals.add(new SpecCompletionProposal(item, region.getOffset(), region.getLength(), item.length(),
						Activator.getDefault().getImage(PACKAGE_ICON), item, null, item));
			}
		}
		return proposals;
	}

	/**
	 * Create a template context for the givens Specfile, offset.
	 *
	 * @param specfile the sepcfile element
	 * @param offset   the offset of the <code>documment</code>
	 * @return a TemplateContextType
	 */
	private TemplateContextType getContextType(Specfile specfile, int offset) {
		List<SpecfileSection> elements = specfile.getSections();
		if (elements.isEmpty() || offset < elements.get(0).getLineEndPosition()) {
			return Activator.getDefault().getContextTypeRegistry().getContextType(PREAMBLE_SECTION_TEMPLATE);
		} else if (elements.size() == 1 || offset < elements.get(1).getLineEndPosition()) {
			return Activator.getDefault().getContextTypeRegistry().getContextType(PRE_SECTION_TEMPLATE);
		} else if (elements.size() == 2 || offset < elements.get(2).getLineEndPosition()) {
			return Activator.getDefault().getContextTypeRegistry().getContextType(BUILD_SECTION_TEMPLATE);
		} else if (elements.size() == 3 || offset < elements.get(3).getLineEndPosition()) {
			return Activator.getDefault().getContextTypeRegistry().getContextType(INSTALL_SECTION_TEMPLATE);
		} else {
			return Activator.getDefault().getContextTypeRegistry().getContextType(CHANGELOG_SECTION_TEMPLATE);
		}

	}

	/**
	 * Create a template context for the given Specfile and offset.
	 *
	 * @param viewer   the viewer for which the context is created
	 * @param region   the region into <code>document</code> for which the context
	 *                 is created
	 * @param specfile the specfile element
	 * @return a TemplateContextType
	 */
	private TemplateContext createContext(ITextViewer viewer, IRegion region, Specfile specfile) {
		TemplateContextType contextType = getContextType(specfile, region.getOffset());
		if (contextType != null) {
			IDocument document = viewer.getDocument();
			return new DocumentTemplateContext(contextType, document, region.getOffset(), region.getLength());
		}
		return null;
	}

	/**
	 * Get relevance on templates for the given template and prefix.
	 *
	 * @param template the <code>Template</code> to get relevance
	 * @param prefix   the prefix <code>String</code> to check.
	 * @return a relevant code (90 if <code>true</code> and 0 if not)
	 */
	private int getRelevance(Template template, String prefix) {
		if (template.getName().startsWith(prefix)) {
			return 90;
		}
		return 0;
	}

	/**
	 * Get the prefix for a given viewer, offset.
	 *
	 * @param viewer the viewer for which the context is created
	 * @param offset the offset into <code>document</code> for which the prefix is
	 *               research
	 * @return the prefix
	 */
	private String getGroupPrefix(ITextViewer viewer, int offset) {
		int i = offset;
		IDocument document = viewer.getDocument();
		if (i > document.getLength()) {
			return EMPTY_STRING;
		}

		try {
			while (i > 0) {
				char ch = document.getChar(i - 1);
				if (!Character.isLetterOrDigit(ch) && (ch != '/')) {
					break;
				}
				i--;
			}
			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return EMPTY_STRING;
		}
	}

	/**
	 * Get defines as a String key->value pair for a given specfile and prefix.
	 *
	 * @param specfile to get defines from.
	 * @param prefix   used to find defines.
	 * @return a <code>HashMap</code> of defines.
	 *
	 */
	private Map<String, String> getDefines(Specfile specfile, String prefix) {
		Collection<SpecfileDefine> defines = specfile.getDefines();
		Map<String, String> ret = new HashMap<>();
		String defineName;
		for (SpecfileDefine define : defines) {
			defineName = "%" + define.getName(); //$NON-NLS-1$
			if (defineName.startsWith(prefix.replaceFirst("\\{", EMPTY_STRING))) {//$NON-NLS-1$
				ret.put(defineName, define.getStringValue());
			}
		}
		return ret;
	}

	/**
	 * Get patches as a String key->value pair for a given specfile and prefix.
	 *
	 * @param specfile to get defines from.
	 * @param prefix   used to find defines.
	 * @return a <code>HashMap</code> of defines.
	 *
	 */
	private Map<String, String> getPatches(Specfile specfile, String prefix) {
		Collection<SpecfileSource> patches = specfile.getPatches();
		Map<String, String> ret = new HashMap<>();
		String patchName;
		for (SpecfileSource patch : patches) {
			patchName = "%patch" + patch.getNumber(); //$NON-NLS-1$
			if (patchName.startsWith(prefix)) {
				ret.put(patchName.toLowerCase(), RPMUtils.getSourceOrPatchValue(specfile, "patch" //$NON-NLS-1$
						+ patch.getNumber()));
			}
		}
		return ret;
	}

	/**
	 * Get sources as a String key->value pair for a given specfile and prefix.
	 *
	 * @param specfile to get defines from.
	 * @param prefix   used to find defines.
	 * @return a <code>HashMap</code> of defines.
	 *
	 */
	private Map<String, String> getSources(Specfile specfile, String prefix) {
		Collection<SpecfileSource> sources = specfile.getSources();
		Map<String, String> ret = new HashMap<>();
		String sourceName;
		for (SpecfileSource source : sources) {
			sourceName = ISpecfileSpecialSymbols.MACRO_START_LONG + SOURCE + source.getNumber()
					+ ISpecfileSpecialSymbols.MACRO_END_LONG;
			if (sourceName.startsWith(prefix)) {
				ret.put(sourceName, RPMUtils.getSourceOrPatchValue(specfile, SOURCE + source.getNumber()));
			}
		}
		return ret;
	}

	private String completionWord(IDocument doc, int offset) {
		String word = null;
		if (offset > 0) {
			try {
				for (int n = offset - 1; n >= 0 && word == null; n--) {
					char c = doc.getChar(n);
					if (Character.isWhitespace(c)) {
						word = doc.get(n + 1, offset - n - 1);
					} else if (n == 0) {
						// beginning of file
						word = doc.get(0, offset - n);
					}
				}
			} catch (BadLocationException e) {
				// ignore
			}
		}
		return word == null ? "" : word; //$NON-NLS-1$
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
}
