/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.scanner;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.linuxtools.internal.docker.editor.Activator;
import org.eclipse.linuxtools.internal.docker.editor.util.AssetLoader;

public class InstructionWordRule extends WordRule {

	public static final String[] INSTRUCTIONS;

	private static final String INSTRUCTION_FILE_PATH = "assets/instructions.txt";

	// Load instructions from the text file.
	static {
		String[] loadedInstructions = new String[0];
		try {
			byte[] contents = AssetLoader.loadAsset(INSTRUCTION_FILE_PATH);
			String contentsString = new String(contents);
			String lineSeparator = (contentsString.contains("\r\n")) ? "\r\n" : "\n";
			loadedInstructions = contentsString.split(lineSeparator);
		} catch (IOException e) {
			Activator.log(IStatus.WARNING,
					"No content assist available: error while reading instruction file: " + INSTRUCTION_FILE_PATH, e);
		}
		INSTRUCTIONS = loadedInstructions;
	}

	public InstructionWordRule(IToken instructionToken) {
		super(new InstructionWordDetector(), Token.UNDEFINED, true);
		setColumnConstraint(0);
		for (String instruction : INSTRUCTIONS)
			addWord(instruction, instructionToken);
	}

	private static class InstructionWordDetector implements IWordDetector {

		@Override
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}

	}
}