/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.util.Arrays;

/**
 * Alignment management
 *
 * @since 4.0
 */
public class STPAlignment {
	// Alignment names.
	public static final String ASSIGNMENT_EXPRESSION = "assignmentExpression"; //$NON-NLS-1$
	public static final String BINARY_EXPRESSION = "binaryExpression"; //$NON-NLS-1$
	public static final String COMPACT_IF = "compactIf"; //$NON-NLS-1$
	public static final String CONDITIONAL_EXPRESSION = "conditionalExpression"; //$NON-NLS-1$
	public static final String CONDITIONAL_EXPRESSION_CHAIN = "conditionalExpressionChain"; //$NON-NLS-1$
	public static final String DECLARATION_INITIALIZER = "declarationInitializer"; //$NON-NLS-1$
	public static final String DESIGNATED_INITIALIZER = "designatedInitializer"; //$NON-NLS-1$
	public static final String EXCEPTION_SPECIFICATION = "exceptionSpecification"; //$NON-NLS-1$
	public static final String FIELD_REFERENCE = "fieldReference"; //$NON-NLS-1$
	public static final String FOR = "for"; //$NON-NLS-1$
	public static final String LIST_ELEMENTS_PREFIX = "listElements_"; //$NON-NLS-1$
	public static final String MACRO_ARGUMENTS = "macroArguments"; //$NON-NLS-1$
	public static final String OVERLOADED_LEFT_SHIFT_CHAIN = "overloadedLeftShiftChain"; //$NON-NLS-1$
	public static final String TRAILING_TEXT = "trailingText"; //$NON-NLS-1$

	/** The name of the alignment */
	public String name;

	/** Link to the enclosing alignment. */
	public STPAlignment enclosing;

	/** Start location of this alignment. */
	public STPLocation location;

	/**
	 * Tail formatter is an encapsulation mechanism for formatting of the trailing text that should
	 * be kept together with the last element of a list.
	 */
	public Runnable tailFormatter;

	// Indentation management
	private int fragmentIndex;
	private int fragmentCount;
	private int[] fragmentIndentations;

	// Break management
	public int breakIndentationLevel;
	private int alternativeBreakIndentationLevel;
	public int[] fragmentBreaks;
	public boolean wasSplit;
	private int currentFragmentStartLine;

	private final STPScribe scribe;

	/*
	 * Alignment modes
	 */
	public static final int M_FORCE = 1; // if bit set, then alignment will be non-optional (default is optional)
	public static final int M_INDENT_ON_COLUMN = 2; // if bit set, broken fragments will be aligned on current location column (default is to break at current indentation level)
	public static final int	M_INDENT_BY_ONE = 4; // if bit set, broken fragments will be indented one level below current (not using continuation indentation)

	// Split modes can be combined either with M_FORCE or M_INDENT_ON_COLUMN

	/** foobar(#fragment1, #fragment2, <ul>
	 *  <li>    #fragment3, #fragment4 </li>
	 * </ul>
	 */
	public static final int M_COMPACT_SPLIT = 16; // fill each line with all possible fragments

	/** foobar(<ul>
	 * <li>    #fragment1, #fragment2,  </li>
	 * <li>    #fragment5, #fragment4, </li>
	 * </ul>
	 */
	public static final int M_COMPACT_FIRST_BREAK_SPLIT = 32; //  compact mode, but will first try to break before first fragment

	/** foobar(<ul>
	 * <li>     #fragment1,  </li>
	 * <li>     #fragment2,  </li>
	 * <li>     #fragment3 </li>
	 * <li>     #fragment4,  </li>
	 * </ul>
	 */
	public static final int M_ONE_PER_LINE_SPLIT = 32 + 16; // one fragment per line

	/**
	 * foobar(<ul>
	 * <li>     #fragment1, </li>
	 * <li>     #fragment2, </li>
	 * <li>     #fragment3, </li>
	 * <li>     #fragment4, </li>
	 * </ul>
	 */
	public static final int M_NEXT_SHIFTED_SPLIT = 64; // one fragment per line, subsequent are indented further

	/** foobar(#fragment1, <ul>
	 * <li>      #fragment2,  </li>
	 * <li>      #fragment3 </li>
	 * <li>      #fragment4,  </li>
	 * </ul>
	 */
	public static final int M_NEXT_PER_LINE_SPLIT = 64 + 16; // one per line, except first fragment (if possible)

	//64+32
	//64+32+16

	// Mode controlling column alignments
	/**
	 * <table BORDER COLS=4 WIDTH="100%" >
	 * <tr><td>#fragment1A</td>            <td>#fragment2A</td>       <td>#fragment3A</td>  <td>#very-long-fragment4A</td></tr>
	 * <tr><td>#fragment1B</td>            <td>#long-fragment2B</td>  <td>#fragment3B</td>  <td>#fragment4B</td></tr>
	 * <tr><td>#very-long-fragment1C</td>  <td>#fragment2C</td>       <td>#fragment3C</td>  <td>#fragment4C</td></tr>
	 * </table>
	 */
	public static final int M_MULTICOLUMN = 256; // fragments are on same line, but multiple line of fragments will be aligned vertically

	public static final int M_NO_ALIGNMENT = 0;

	private int mode;

	public static final int SPLIT_MASK = M_ONE_PER_LINE_SPLIT | M_NEXT_SHIFTED_SPLIT | M_COMPACT_SPLIT | M_COMPACT_FIRST_BREAK_SPLIT | M_NEXT_PER_LINE_SPLIT;

	// Alignment tie-break rules - when split is needed, will decide whether innermost/outermost alignment is to be chosen
	public static final int R_OUTERMOST = 1;
	public static final int R_INNERMOST = 2;
	public int tieBreakRule;

	// Alignment effects on a per fragment basis
	public static final int BREAK_NOT_ALLOWED = -1;
	public static final int NONE = 0;
	public static final int BREAK = 2;

	// Location to align and break on.
	public STPAlignment(String name, int mode, int tieBreakRule, STPScribe scribe, int fragmentCount,
			int sourceRestart, int continuationIndent) {
		this.name = name;
		this.location = new STPLocation(scribe, sourceRestart);
		this.mode = mode;
		this.tieBreakRule = tieBreakRule;
		this.fragmentCount = fragmentCount;
		this.scribe = scribe;
		this.wasSplit = false;
		this.fragmentIndentations = new int[this.fragmentCount];
		this.fragmentBreaks = new int[this.fragmentCount];
		Arrays.fill(this.fragmentBreaks, (this.mode & M_FORCE) == 0 ? BREAK_NOT_ALLOWED : NONE);
		this.currentFragmentStartLine = this.scribe.line;

		int currentColumn = this.location.outputColumn;
		if (currentColumn == 1) {
		    currentColumn = this.location.outputIndentationLevel + 1;
		}

		// Initialize the break indentation level, using modes and continuationIndentationLevel
		// preference.
		final int indentSize = this.scribe.indentationSize;

		if ((mode & M_INDENT_ON_COLUMN) != 0) {
			// Indent broken fragments at next indentation level, based on current column
			this.breakIndentationLevel = this.scribe.getNextIndentationLevel(currentColumn);
			this.alternativeBreakIndentationLevel =
					this.location.outputIndentationLevel + continuationIndent * indentSize;
		} else {
			int baseIndentationLevel = this.location.outputIndentationLevel;
			if (name != TRAILING_TEXT && this.scribe.currentAlignment != null &&
					(this.scribe.currentAlignment.mode & M_INDENT_ON_COLUMN) != 0 &&
					this.scribe.currentAlignment.fragmentCount > 1) {
				baseIndentationLevel = this.scribe.currentAlignment.breakIndentationLevel;
			}
			if ((mode & M_INDENT_BY_ONE) != 0) {
				// Indent broken fragments exactly one level deeper than current indentation
				this.breakIndentationLevel = baseIndentationLevel + indentSize;
			} else {
				this.breakIndentationLevel = baseIndentationLevel + continuationIndent * indentSize;
			}
			this.alternativeBreakIndentationLevel = this.breakIndentationLevel;
		}

		// Check for forced alignments
		if ((this.mode & M_FORCE) != 0) {
			this.fragmentBreaks[this.fragmentIndex] = NONE;
			couldBreak();
		}
	}

	public boolean couldBreak() {
		int i;
		switch (mode & SPLIT_MASK) {
			/*  # aligned fragment
			 *  foo(
			 *     #AAAAA, #BBBBB,
			 *     #CCCC);
			 */
			case M_COMPACT_FIRST_BREAK_SPLIT:
				if (this.fragmentBreaks[0] == NONE) {
					if ((this.mode & M_INDENT_ON_COLUMN) != 0) {
						if (this.breakIndentationLevel <= this.alternativeBreakIndentationLevel) {
							// Does not make sense to break here unless indentation is reduced.
							break;
						}
						// Change break indentation level and erase previously created breaks.
						this.breakIndentationLevel = this.alternativeBreakIndentationLevel;
						eraseExistingBreaks(0);
					}
					this.fragmentBreaks[0] = BREAK;
					this.fragmentIndentations[0] = this.breakIndentationLevel;
					return wasSplit = true;
				}
				i = this.fragmentIndex;
				do {
					if (this.fragmentBreaks[i] == NONE) {
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.breakIndentationLevel;
						return wasSplit = true;
					}
				} while (--i >= 0);
				break;
			/*  # aligned fragment
			 *  foo(#AAAAA, #BBBBB,
			 *     #CCCC);
			 */
			case M_COMPACT_SPLIT:
				i = this.fragmentIndex;
				do {
					if (this.fragmentBreaks[i] == NONE) {
						if ((this.mode & M_INDENT_ON_COLUMN) != 0 && isFirstBreakableFragment(i)) {
							if (this.breakIndentationLevel <= this.alternativeBreakIndentationLevel) {
								// Does not make sense to break here unless indentation is reduced.
								break;
							}
							// Change break indentation level and erase previously created breaks.
							this.breakIndentationLevel = this.alternativeBreakIndentationLevel;
							eraseExistingBreaks(i);
						}
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.breakIndentationLevel;
						return wasSplit = true;
					}
				} while ((this.fragmentBreaks[i] != BREAK || (this.mode & M_INDENT_ON_COLUMN) != 0) && --i >= 0);
				break;

			/*  # aligned fragment
			 *  foo(
			 *      #AAAAA,
			 *          #BBBBB,
			 *          #CCCC);
			 */
			case M_NEXT_SHIFTED_SPLIT:
				if (this.fragmentBreaks[0] == NONE) {
					this.fragmentBreaks[0] = BREAK;
					this.fragmentIndentations[0] = this.breakIndentationLevel;
					for (i = 1; i < this.fragmentCount; i++) {
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] =
								this.breakIndentationLevel + this.scribe.indentationSize;
					}
					return wasSplit = true;
				}
				break;

			/*  # aligned fragment
			 *  foo(
			 *      #AAAAA,
			 *      #BBBBB,
			 *      #CCCC);
			 */
			case M_ONE_PER_LINE_SPLIT:
				if (this.fragmentBreaks[0] == NONE) {
					for (i = 0; i < this.fragmentCount; i++) {
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.breakIndentationLevel;
					}
					return wasSplit = true;
				}
				break;
			/*  # aligned fragment
			 *  foo(#AAAAA,
			 *      #BBBBB,
			 *      #CCCC);
			 */
			case M_NEXT_PER_LINE_SPLIT:
				if (this.fragmentBreaks[0] != BREAK) {
					if (this.fragmentCount > 1 && this.fragmentBreaks[1] == NONE) {
						if ((this.mode & M_INDENT_ON_COLUMN) != 0) {
							this.fragmentIndentations[0] = this.breakIndentationLevel;
						}
						for (i = 1; i < this.fragmentCount; i++) {
							this.fragmentBreaks[i] = BREAK;
							this.fragmentIndentations[i] = this.breakIndentationLevel;
						}
						return wasSplit = true;
					}
				}
				break;
		}
		return false; // Cannot split better
	}

	private boolean isFirstBreakableFragment(int i) {
		while (--i >= 0) {
			if (this.fragmentBreaks[i] != BREAK_NOT_ALLOWED)
				return false;
		}
		return true;
	}

	private void eraseExistingBreaks(int startFragmentIndex) {
		for (int j = startFragmentIndex + 1; j < this.fragmentIndentations.length; j++) {
			if (this.fragmentBreaks[j] == BREAK) {
				this.fragmentBreaks[j] = NONE;
				this.fragmentIndentations[j] = 0;
			}
		}
	}

	// Performs alignment effect for current fragment.
	public void performFragmentEffect() {
		if ((this.mode & M_MULTICOLUMN) == 0) {
			switch (this.mode & SPLIT_MASK) {
				case STPAlignment.M_COMPACT_SPLIT:
				case STPAlignment.M_COMPACT_FIRST_BREAK_SPLIT:
				case STPAlignment.M_NEXT_PER_LINE_SPLIT:
				case STPAlignment.M_NEXT_SHIFTED_SPLIT:
				case STPAlignment.M_ONE_PER_LINE_SPLIT:
					break;
				default:
					return;
			}
		}

		if ((this.mode & M_INDENT_ON_COLUMN) != 0 && this.fragmentIndex > 0 &&
				this.scribe.line > currentFragmentStartLine) {
			// The previous fragment spans multiple line. Put the current fragment on a new line.
			this.fragmentBreaks[this.fragmentIndex] = BREAK;
			this.fragmentIndentations[this.fragmentIndex] = this.breakIndentationLevel;
			wasSplit = true;
		}
		if (this.fragmentBreaks[this.fragmentIndex] == BREAK) {
			this.scribe.startNewLine();
		}
		if (this.fragmentIndentations[this.fragmentIndex] > 0) {
			this.scribe.indentationLevel = this.fragmentIndentations[this.fragmentIndex];
		}
		currentFragmentStartLine = this.scribe.line;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer
			.append(getClass().getName())
			.append(':')
			.append("<name: ")	//$NON-NLS-1$
			.append(this.name)
			.append(">");	//$NON-NLS-1$
		if (this.enclosing != null) {
			buffer
				.append("<enclosingName: ")	//$NON-NLS-1$
				.append(this.enclosing.name)
				.append('>');
		}
		buffer.append('\n');

		for (int i = 0; i < this.fragmentCount; i++) {
			buffer
				.append(" - fragment ")	//$NON-NLS-1$
				.append(i)
				.append(": ")	//$NON-NLS-1$
				.append("<break: ")	//$NON-NLS-1$
				.append(this.fragmentBreaks[i] > 0 ? "YES" : "NO")	//$NON-NLS-1$ //$NON-NLS-2$
				.append(">")	//$NON-NLS-1$
				.append("<indent: ")	//$NON-NLS-1$
				.append(this.fragmentIndentations[i])
				.append(">\n");	//$NON-NLS-1$
		}
		buffer.append('\n');
		return buffer.toString();
	}
}
