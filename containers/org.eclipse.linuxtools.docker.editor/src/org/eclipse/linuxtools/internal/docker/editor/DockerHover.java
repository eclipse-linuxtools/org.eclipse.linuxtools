/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others..
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implmentation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.linuxtools.internal.docker.editor.util.AssetLoader;

public class DockerHover implements ITextHover, ITextHoverExtension {

	private AssetLoader loader;

	public DockerHover() {
		loader = new AssetLoader();
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		String varName = null;
		try {
			varName = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
		} catch (BadLocationException e) {
			return null;
		}

		return loader.getInfo(varName);
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findWord(textViewer.getDocument(), offset);
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return parent -> new DefaultInformationControl(parent, false);
	}

	private IRegion findWord(IDocument document, int offset) {
		int start = -2;
		int end = -1;

		try {

			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c))
					break;
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c))
					break;
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

}
