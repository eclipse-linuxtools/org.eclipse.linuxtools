/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
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
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePatchMacro;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;


/**
 * Content assist processor
 * 
 * @author Alphonse Van Assche
 *
 */
public class SpecfileCompletionProcessor implements IContentAssistProcessor {
	
	/**
	 * <code>Comparator</code> implementation used to sort template proposals
	 * @author Van Assche Alphonse
	 */
	private static final class ProposalComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return ((TemplateProposal) o2).getRelevance()
					- ((TemplateProposal) o1).getRelevance();
		}
	}

	private static final String TEMPLATE_ICON = "icons/template_obj.gif";

	private static final String MACRO_ICON = "icons/macro_obj.gif";
	
	private static final String PACKAGE_ICON = "icons/rpm.gif";

	private static final String PREAMBLE_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.preambleSection";

	private static final String PRE_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.preSection";

	private static final String BUILD_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.buildSection";

	private static final String INSTALL_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.installSection";

	private static final String CHANGELOG_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.changelogSection";

	private final SpecfileEditor editor;
	
	private static final Comparator proposalComparator = new ProposalComparator();

	/**
	 * Default constructor
	 */
	public SpecfileCompletionProcessor(SpecfileEditor editor) {
		this.editor = editor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		List result = new ArrayList();
		Specfile specfile = editor.getSpecfile();
		if (specfile == null)
			return null;
		ITextSelection selection = (ITextSelection) viewer
				.getSelectionProvider().getSelection();
		// adjust offset to start of normalized selection
		if (selection.getOffset() != offset)
			offset = selection.getOffset();
		String prefix = getPrefix(viewer, offset);
		Region region = new Region(offset - prefix.length(), prefix.length()
				+ selection.getLength());
		// RPM macro's are useful in the whole specfile.
		ICompletionProposal[] rpmMacroProposals = computeRpmMacroProposals(
				viewer, region, specfile, prefix);
		// Get the current content type
		String currentContentType = editor.getInputDocument().getDocumentPartitioner().getContentType(region.getOffset());		
		if (currentContentType.equals(SpecfilePartitionScanner.SPEC_PACKAGES)) {
			// don't show template in the RPM packages content type.
			// (when the line begin with Requires, BuildRequires etc...)
			ICompletionProposal[] rpmPackageProposals = computeRpmPackageProposals(
					viewer, region, prefix);
			result.addAll(Arrays.asList(rpmPackageProposals));
			result.addAll(Arrays.asList(rpmMacroProposals));
		} else {
			// don't show RPM packages proposals in all others content type. 
			ICompletionProposal[] templateProposals = computeTemplateProposals(
					viewer, region, specfile, prefix);
			result.addAll(Arrays.asList(templateProposals));
			result.addAll(Arrays.asList(rpmMacroProposals));
		}
		return (ICompletionProposal[]) result
				.toArray(new ICompletionProposal[result.size()]);
	}
	
	/**
	 * Compute the templates proposals, these proposals are contextual on sections.
	 * Return an array of template proposals for the given viewer, region, specfile, prefix.
	 * 
	 * @param viewer
	 *            the viewer for which the context is created
	 * @param region
	 *            the region into <code>document</code> for which the context
	 *            is created
	 * @param specfile
	 * 			  the specfile element
	 * @param prefix
	 * 			  the prefix string
	 * @return 
	 *            a ICompletionProposal[]
	 */
	private ICompletionProposal[] computeTemplateProposals(ITextViewer viewer,
			IRegion region, Specfile specfile, String prefix) {
		TemplateContext context = createContext(viewer, region, specfile);
		if (context == null) {
			return new ICompletionProposal[0];
		}
		ITextSelection selection = (ITextSelection) viewer
				.getSelectionProvider().getSelection();
		context.setVariable("selection", selection.getText());
		String id = context.getContextType().getId();
		Template[] templates = Activator.getDefault().getTemplateStore()
				.getTemplates(id);
		List matches = new ArrayList();
		for (int i = 0; i < templates.length; i++) {
			Template template = templates[i];
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
		Collections.sort(matches, proposalComparator);
		return (ICompletionProposal[]) matches
				.toArray(new ICompletionProposal[matches.size()]);
	}

	/**
	 * Compute RPM macro proposals, these proposals are usable in the whole document.
	 * Return an array of RPM macro proposals for the given viewer, region, prefix.
	 * 
	 * @param viewer
	 *            the viewer for which the context is created
	 * @param region
	 *            the region into <code>document</code> for which the context
	 *            is created
	 * @param prefix
	 * 			  the prefix string to find
	 * @return 
	 *            a ICompletionProposal[]
	 */
	private ICompletionProposal[] computeRpmMacroProposals(ITextViewer viewer,
			IRegion region, Specfile specfile, String prefix) {
		Map rpmMacroProposalsMap = Activator.getDefault().getRpmMacroList().getProposals(prefix);
		
		// grab defines and put them into the proposals map
		rpmMacroProposalsMap.putAll(getDefinesName(specfile, prefix));
		
		if (rpmMacroProposalsMap == null)
			return new ICompletionProposal[0];
		ArrayList proposals = new ArrayList();
		String key;
		Iterator iterator = rpmMacroProposalsMap.keySet().iterator();
		while (iterator.hasNext()) {
			key = (String) iterator.next();
			proposals.add(new CompletionProposal("%{" + key.substring(1) + "}", 
							region.getOffset(), region.getLength(),
							key.length() + 2, Activator.getDefault().getImage(MACRO_ICON),
							key, null, (String) rpmMacroProposalsMap.get(key)));
		}
		return (ICompletionProposal[]) proposals
				.toArray(new ICompletionProposal[proposals.size()]);
	}

	/**
	 * Compute RPM package proposals, these proposals are usable only in the preambule section.
	 * Return an array of RPM macro proposals for the given viewer, region, specfile, prefix.
	 * 
	 * @param viewer
	 *            the viewer for which the context is created
	 * @param region
	 *            the region into <code>document</code> for which the context
	 *            is created
	 * @param prefix
	 * 			  the prefix string
	 * @return 
	 *            a ICompletionProposal[]
	 */
	private ICompletionProposal[] computeRpmPackageProposals(ITextViewer viewer,
			IRegion region, String prefix) {
		List rpmPkgProposalsList = Activator.getDefault().getRpmPackageList().getProposals(prefix);
		if (rpmPkgProposalsList == null)
			return new ICompletionProposal[0];
		ArrayList proposals = new ArrayList();
		String[] item;
		Iterator iterator = rpmPkgProposalsList.iterator();
		while (iterator.hasNext()) {
			item = (String[]) iterator.next();
			proposals.add(new CompletionProposal(item[0], 
					region.getOffset(), region.getLength(),
					item[0].length(), Activator.getDefault().getImage(PACKAGE_ICON),
					item[0], null, item[1]));
		}
		return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	/**
	 * Create a template context for the givens Specfile, offset.
	 * 
	 * @param specfile
	 *            the sepcfile element
	 * @param offset
	 *            the offset of the <code>documment</code>
	 * @return a TemplateContextType
	 */
	private TemplateContextType getContextType(Specfile specfile, int offset) {
		SpecfileElement[] elements = specfile.getSectionsElements();
			if (elements.length == 0 || offset < elements[0].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(PREAMBLE_SECTION_TEMPLATE);
			} else if (elements.length == 1 || offset < elements[1].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(PRE_SECTION_TEMPLATE);
			} else if (elements.length == 2 || offset < elements[2].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(BUILD_SECTION_TEMPLATE);
			} else if (elements.length == 3 || offset < elements[3].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(INSTALL_SECTION_TEMPLATE);
			} else {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(CHANGELOG_SECTION_TEMPLATE);
			}
		
	}

	/**
	 * Create a template context for the given Specfile and offset.
	 * 
	 * @param viewer
	 *            the viewer for which the context is created
	 * @param region
	 *            the region into <code>document</code> for which the context
	 *            is created
	 * @param specfile
	 * 			  the specfile element
	 * @return a TemplateContextType
	 */
	private TemplateContext createContext(ITextViewer viewer, IRegion region,
			Specfile specfile) {
		TemplateContextType contextType = getContextType(specfile, region
				.getOffset());
		if (contextType != null) {
			IDocument document = viewer.getDocument();
			return new DocumentTemplateContext(contextType, document, region
					.getOffset(), region.getLength());
		}
		return null;
	}
	
	/**
	 * Get relevance on templates for the given template and prefix.
	 * @param template
	 * 			the <code>Template</code> to get relevance
	 * @param prefix
	 * 			the prefix <code>String</code> to check.
	 * @return
	 * 			a relevant code (90 if <code>true</code> and 0 if not)
	 */
	private int getRelevance(Template template, String prefix) {
		if (template.getName().startsWith(prefix))
			return 90;
		return 0;
	}

	/**
	 * Get the prefix for a given viewer, offset.
	 * 
	 * @param viewer
	 *            the viewer for which the context is created
	 * @param offset
	 *            the offset into <code>document</code> for which the prefix
	 *            is research
	 * @return the prefix
	 */
	private String getPrefix(ITextViewer viewer, int offset) {
		int i = offset;
		IDocument document = viewer.getDocument();
		if (i > document.getLength())
			return "";

		try {
			while (i > 0) {
				char ch = document.getChar(i - 1);
				if (!Character.isLetterOrDigit(ch) && (ch != '%') && (ch != '_') && (ch != '-') && (ch != '{'))
					break;
				i--;
			}
			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return "";
		}
	}

	/**
	 * Get defines and patches as a String key->value pair for a given specfile
	 * and prefix.
	 * 
	 * @param specfile
	 *            to get defines from.
	 * @param prefix
	 *            used to find defines.
	 * @return a <code>HashMap</code> of defines.
	 * 
	 */
	private Map getDefinesName(Specfile specfile, String prefix) {
		// Get proposals for defines.
		Collection defines = specfile.getDefinesAsList();
		Map ret = new HashMap();
		String defineName;
		for (Iterator iterator = defines.iterator(); iterator.hasNext();) {
			SpecfileDefine define = (SpecfileDefine) iterator.next();
			defineName = "%" + define.getName();
			if (defineName.startsWith(prefix.replaceFirst("\\{", "")))
				ret.put(defineName, define.getStringValue());
		}
		// get proposals for patches
		Collection patches = specfile.getPatchesAsList();
		String patchName;
		for (Iterator iterator = patches.iterator(); iterator.hasNext();) {
			SpecfileSource patch = (SpecfileSource) iterator.next();
			patchName = "%patch" + patch.getNumber();
			System.out.println(patchName);
			if (patchName.startsWith(prefix))
				ret.put(patchName.toLowerCase(), SpecfileHover
						.getSourceOrPatchValue(specfile, "patch"
								+ patch.getNumber()));
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
}
