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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
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
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Content assist processor
 */
public class SpecfileCompletionProcessor implements IContentAssistProcessor {

	private static final String TEMPLATE_ICON = "icons/template.gif";

	private static final String MACRO_ICON = "icons/rpmmacro.gif";
	
	private static final String PACKAGE_ICON = "icons/rpm.gif";

	private static final String PREAMBLE_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.preambleSection";

	private static final String PRE_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.preSection";

	private static final String BUILD_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.buildSection";

	private static final String INSTALL_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.installSection";

	private static final String CHANGELOG_SECTION_TEMPLATE = "org.eclipse.linuxtools.rpm.ui.editor.changelogSection";

	/**
	 * Implentation of a <code>Comparator</code> used sort template Proposals
	 * @author Van Assche Alphonse
	 *
	 */
	private static final class ProposalComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return ((TemplateProposal) o2).getRelevance()
					- ((TemplateProposal) o1).getRelevance();
		}
	}

	private static final Comparator proposalComparator = new ProposalComparator();
	private final SpecfileEditor sEditor;

	/**
	 * Default constructor
	 */
	public SpecfileCompletionProcessor(SpecfileEditor editor) {
		sEditor = editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		List result = new ArrayList();
		Specfile specfile = sEditor.getSpecfile();
		if (specfile == null)
			return null;
		ITextSelection selection = (ITextSelection) viewer
				.getSelectionProvider().getSelection();
		// adjust offset to start of normalized selection:
		if (selection.getOffset() != offset)
			offset = selection.getOffset();
		String prefix = getPrefix(viewer, offset);
		Region region = new Region(offset - prefix.length(), prefix.length()
				+ selection.getLength());
		ICompletionProposal[] templateProposals = computeTemplateProposals(
				viewer, region, specfile, prefix);
		ICompletionProposal[] rpmMacroProposals = computeRpmMacroProposals(
				viewer, region, prefix);
		ICompletionProposal[] rpmPackageProposals = computeRpmPackageProposals(
				viewer, region, specfile, prefix);		
		result.addAll(Arrays.asList(templateProposals));
		result.addAll(Arrays.asList(rpmMacroProposals));
		result.addAll(Arrays.asList(rpmPackageProposals));
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
						getImage(TEMPLATE_ICON), relevance));
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
			IRegion region, String prefix) {
		Map rpmMacroProposalsMap = Activator.getDefault().getRpmMacroList().getProposals(prefix);
		if (rpmMacroProposalsMap == null)
			return new ICompletionProposal[0];
		ArrayList proposals = new ArrayList();
		String key;
		Iterator iterator = rpmMacroProposalsMap.keySet().iterator();
		while (iterator.hasNext()) {
			key = (String) iterator.next();
			proposals.add(new CompletionProposal("%{" + key.substring(1) + "}", 
							region.getOffset(), region.getLength(),
							key.length() + 2, getImage(MACRO_ICON),
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
	 * @param specfile
	 * 			  the specfile element
	 * @param prefix
	 * 			  the prefix string
	 * @return 
	 *            a ICompletionProposal[]
	 */
	private ICompletionProposal[] computeRpmPackageProposals(ITextViewer viewer,
			IRegion region, Specfile specfile, String prefix) {
		List rpmPkgProposalsList = Activator.getDefault().getRpmPackageList().getProposals(prefix);
		SpecfileElement[] elements = specfile.getSectionsElements();
		// Show rpm packages proposals only in the preamble section 
		if (elements.length == 0 || region.getOffset() < elements[0].getLineEndPosition()) {
			if (rpmPkgProposalsList == null)
				return new ICompletionProposal[0];
			ArrayList proposals = new ArrayList();
			String[] item;
			Iterator iterator = rpmPkgProposalsList.iterator();
			while (iterator.hasNext()) {
				item = (String[]) iterator.next();
				proposals.add(new CompletionProposal(item[0], 
								region.getOffset(), region.getLength(),
								item[0].length(), getImage(PACKAGE_ICON),
								item[0], null, item[1]));
			}
			return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
		} else {
			return new ICompletionProposal[0];	
		}
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
			} else if (offset < elements[1].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(PRE_SECTION_TEMPLATE);
			} else if (offset < elements[2].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(BUILD_SECTION_TEMPLATE);
			} else if (offset < elements[3].getLineEndPosition()) {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(INSTALL_SECTION_TEMPLATE);
			} else {
				return Activator.getDefault().getContextTypeRegistry()
						.getContextType(CHANGELOG_SECTION_TEMPLATE);
			}
		
	}

	/**
	 * Creta a template context for the given Specfile, offset.
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
	 * Get a <code>Image</code> object for the given relative path.
	 * 
	 * @param imageRelativePath
	 * 		the relative path to the image.
	 * @return
	 * 		a <code>Image</code>
	 */
	private Image getImage(String imageRelativePath) {
		ImageRegistry registry = Activator.getDefault().getImageRegistry();
		Image image = registry.get(imageRelativePath);
		if (image == null) {
			ImageDescriptor desc = Activator.getImageDescriptor(imageRelativePath);
			registry.put(imageRelativePath, desc);
			image = registry.get(imageRelativePath);
		}
		return image;
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
