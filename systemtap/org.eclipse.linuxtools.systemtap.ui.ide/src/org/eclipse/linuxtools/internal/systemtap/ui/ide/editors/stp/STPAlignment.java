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


/**
 * Alignment management
 *
 * @since 4.0
 */
public class STPAlignment {
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

	public static final int SPLIT_MASK = M_ONE_PER_LINE_SPLIT | M_NEXT_SHIFTED_SPLIT | M_COMPACT_SPLIT | M_COMPACT_FIRST_BREAK_SPLIT | M_NEXT_PER_LINE_SPLIT;
}