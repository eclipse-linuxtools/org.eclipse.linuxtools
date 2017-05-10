/*******************************************************************************
 * Copyright (c) 2006, 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *    Remy Chi Jian Suen <remy.suen@gmail.com> - clean up internal API references (bug #179389)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.rules.Token;

public class GNUHyperlinkDetector extends AbstractHyperlinkDetector {

	private IPath documentLocation;

	/**
	 * Detector using RuleBasedScanner.
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (documentLocation == null) {
			ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			ITextFileBuffer buffer = bufferManager.getTextFileBuffer(textViewer.getDocument());
			if (buffer == null) {
				return null;
			}
			documentLocation = buffer.getLocation().removeLastSegments(1);
		}

		IDocument thisDoc = textViewer.getDocument();

		GNUHyperlinkScanner scanner = new GNUHyperlinkScanner();

		ITypedRegion partitionInfo = null;

		try {
			partitionInfo = thisDoc.getPartition(region.getOffset());
		} catch (org.eclipse.jface.text.BadLocationException e1) {
			e1.printStackTrace();
			return null;
		}

		scanner.setRange(thisDoc, partitionInfo.getOffset(), partitionInfo.getLength());

		Token tmpToken = (Token) scanner.nextToken();

		String tokenStr = (String) tmpToken.getData();

		if (tokenStr == null) {
			return null;
		}

		// try to find non-default token containing region..if none, return
		// null.
		while (region.getOffset() < scanner.getTokenOffset() || region.getOffset() > scanner.getOffset()
				|| tokenStr.equals("_other")) {
			tmpToken = (Token) scanner.nextToken();
			tokenStr = (String) tmpToken.getData();
			if (tokenStr == null)
				return null;
		}

		Region tokenRegion = new Region(scanner.getTokenOffset(), scanner.getTokenLength());

		String line = "";
		try {
			line = thisDoc.get(tokenRegion.getOffset(), tokenRegion.getLength());
		} catch (org.eclipse.jface.text.BadLocationException e1) {
			e1.printStackTrace();
			return null;
		}

		// process file link
		if (tokenStr.equals(GNUHyperlinkScanner.FILE_NAME)) {

			Region pathRegion = null;

			int lineOffset = 0;

			// cut "* " if necessary
			if (line.startsWith("* ")) {
				lineOffset = 2;
				line = line.substring(2);
			}
			pathRegion = new Region(tokenRegion.getOffset() + lineOffset, line.length());

			if (documentLocation == null)
				return null;

			// Replace any escape characters added to name
			line = line.replaceAll("\\\\(.)", "$1");

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile fileLoc = (IFile) root.findMember(documentLocation.append(line));
			if (fileLoc != null && fileLoc.exists()) {
				return new IHyperlink[] { new FileHyperlink(pathRegion, fileLoc) };
			}

		}

		return null;
	}
}
