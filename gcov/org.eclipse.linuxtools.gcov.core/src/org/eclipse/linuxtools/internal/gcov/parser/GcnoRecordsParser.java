/*******************************************************************************
 * Copyright (c) 2009, 2021 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.parser;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.gcov.utils.BEDataInputStream;
import org.eclipse.linuxtools.internal.gcov.utils.GcovStringReader;
import org.eclipse.linuxtools.internal.gcov.utils.LEDataInputStream;
import org.eclipse.linuxtools.internal.gcov.utils.MasksGenerator;
import org.eclipse.osgi.util.NLS;

public class GcnoRecordsParser {

    private static final int GCOV_NOTE_MAGIC = 0x67636e6f; // en ASCII: 67=g 63=c 6e=n 6f=o
    private static final int GCOV_TAG_FUNCTION = 0x01000000;
    private static final int GCOV_TAG_BLOCKS = 0x01410000;
    private static final int GCOV_TAG_ARCS = 0x01430000;
    private static final int GCOV_TAG_LINES = 0x01450000;

    private static final int GCC_VER_810 = 1094201642; // GCC 8.1.0 ('A81*')
	private static final int GCC_VER_910 = 1094267178; // GCC 9.1.0 ('A91*')
	private static final int GCC_VER_1210 = 1110585632;// GCC 12.1.0 ('B21*')
    private static final int GCC_VER_407 = 875575082; // GCC 4.0.7

    private GcnoFunction fnctn = null;
    private final ArrayList<GcnoFunction> fnctns = new ArrayList<>();
    private final ArrayList<SourceFile> currentAllSrcs;
    private final HashMap<String, SourceFile> sourceMap;

    public GcnoRecordsParser(HashMap<String, SourceFile> sourceMap, ArrayList<SourceFile> allSrcs) {
        this.sourceMap = sourceMap;
        this.currentAllSrcs = allSrcs;
    }

    private SourceFile findOrAdd(String fileName) {
        SourceFile newsrc = sourceMap.get(fileName);
        if (newsrc == null) {
            newsrc = new SourceFile(fileName, currentAllSrcs.size() + 1);
            currentAllSrcs.add(newsrc);
            sourceMap.put(fileName, newsrc);
        }
        return newsrc; // return the new added element
    }

    public void parseData(DataInput stream) throws IOException, CoreException {
        // header data
        int magic = 0;
        // blocks data
        ArrayList<Block> blocks = null;
        // source file data
        SourceFile source = null;
        // flag
        boolean parseFirstFnctn = false;
		boolean readBytes = false;

        magic = stream.readInt();
        if (magic == GCOV_NOTE_MAGIC) {
            stream = new BEDataInputStream((DataInputStream) stream);
        } else {
            magic = (magic >> 16) | (magic << 16);
            magic = ((magic & 0xff00ff) << 8) | ((magic >> 8) & 0xff00ff);
            if (magic == GCOV_NOTE_MAGIC) {
                stream = new LEDataInputStream((DataInputStream) stream);
            } else {
                String message = NLS.bind(Messages.GcnoRecordsParser_magic_num_error, magic);
				IStatus status = Status.error(message);
                throw new CoreException(status);
            }
        }

        int version = stream.readInt();
        // stamp = stream.readInt();
        stream.readInt();

		if (version >= GCC_VER_1210) {
			stream.readInt(); // checksum
			readBytes = true;
		}

		if (version >= GCC_VER_910) {
			GcovStringReader.readString(stream, readBytes); // cwd
		}

		if (version >= GCC_VER_810) {
			stream.readInt(); // supports_has_unexecuted_blocks
		}

        /*------------------------------------------------------------------------------
        System.out.println("Gcno LE, Magic "+magic+" version "+version+" stamp "+stamp);
         */

        while (true) {
            try {
                int tag;
				int currentTag = 0;
                // parse header
				tag = stream.readInt();
                int length = stream.readInt();
				if (length < 0) {
					length = 0;
				}

                // parse gcno data
                if (tag == GCOV_TAG_FUNCTION) {
                    // before parse new function, add current function to functions list
                    if (parseFirstFnctn) {
                        fnctns.add(fnctn);
                    }

                    long fnctnIdent = (stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK);
                    long fnctnChksm = (stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK);
                    /*
                     * danielhb, 2012-08-06: Gcov versions 4.7.0 or later (long value = 875575082) has different format
                     * for the data file: prior format: announce_function: header int32:ident int32:checksum new format:
                     * announce_function: header int32:ident int32:lineno_checksum int32:cfg_checksum TL;DR Need to
                     * consume the extra long value.
                     */
                    if (version >= GCC_VER_407) {
                        // long cfgChksm = (stream.readInt()&MasksGenerator.UNSIGNED_INT_MASK);
                        stream.readInt();
                    }
					String fnctnName = GcovStringReader.readString(stream, readBytes);
                    if (version >= GCC_VER_810) {
                        // long artificial = (stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK);
                        stream.readInt();
                    }
					String fnctnSrcFle = GcovStringReader.readString(stream, readBytes);
                    long fnctnFrstLnNmbr = (stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK);
                    if (version >= GCC_VER_810) {
						// long fnctnFrstColumnNmbr = (stream.readInt() &
						// MasksGenerator.UNSIGNED_INT_MASK);
                        stream.readInt();
                        // long fnctnLastLnNmbr = (stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK);
                        stream.readInt();
                    }

					if (version >= GCC_VER_910) {
						// long fnctnLastColumnNmbr = (stream.readInt() &
						// MasksGenerator.UNSIGNED_INT_MASK);
						stream.readInt();
					}

                    fnctn = new GcnoFunction(fnctnIdent, fnctnChksm, fnctnName, fnctnSrcFle, fnctnFrstLnNmbr);
                    SourceFile srcFle2 = findOrAdd(fnctn.getSrcFile());
                    if (fnctn.getFirstLineNmbr() >= srcFle2.getNumLines()) {
                        srcFle2.setNumLines((int) fnctn.getFirstLineNmbr() + 1);
                    }
                    srcFle2.addFnctn(fnctn);
                    parseFirstFnctn = true;
					currentTag = tag;
                    continue;
				} else if (fnctn != null && tag == GCOV_TAG_BLOCKS) {
					int blkLength = length;
                    if (version >= GCC_VER_810) {
						blkLength = stream.readInt();
                    }
                    blocks = new ArrayList<>();
                    Block blck;
					for (int i = 0; i < blkLength; i++) {
                        if (version >= GCC_VER_810) {
                            blck = new Block(0); // value not used anywhere
                        } else {
                            long BlckFlag = stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK;
                            blck = new Block(BlckFlag);
                        }
                        blocks.add(blck);
                    }
					fnctn.setNumBlocks(blkLength);
                    continue;
				} else if (fnctn != null && tag == GCOV_TAG_ARCS) {
                    boolean mark_catches = false;
                    int srcBlockIndice = stream.readInt();
                    Block srcBlk = blocks.get(srcBlockIndice);
					int nmbrArcs = readBytes ? ((length >> 2) - 1) / 2 : (length - 1) / 2;

                    for (int i = 0; i < nmbrArcs; i++) {
                        int dstnatnBlockIndice = stream.readInt();
                        long flag = (stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK);
                        Arc arc = new Arc(srcBlockIndice, dstnatnBlockIndice, flag, blocks);

                        // each arc, register it as exit of the src block
                        srcBlk.addExitArcs(arc);
                        srcBlk.incNumSuccs();

                        // each arc, register it as entry of its dstntn block
                        Block dstntnBlk = arc.getDstnatnBlock();
                        dstntnBlk.addEntryArcs(arc);
                        dstntnBlk.incNumPreds();

                        if (arc.isFake()) {
                            if (arc.getSrcBlock() != null) {
                                // Exceptional exit from this function, the
                                // source block must be a call.
                                srcBlk = blocks.get(srcBlockIndice);
                                srcBlk.setCallSite(true);
                                arc.setCallNonReturn(true);
								mark_catches = true;
                            } else {
                            	arc.setNonLoclaReturn(true);
                                dstntnBlk.setNonLocalReturn(true);
                            }
                        }

                        if (!arc.isOnTree()) {
                            fnctn.incNumCounts();
                        }
                        // nbrCounts++;
                    }

					if (mark_catches) {
						for (Arc a : srcBlk.getExitArcs()) {
							if (!a.isFake() && !a.isFallthrough()) {
								a.setIsThrow(true);
								fnctn.setHasCatch(true);
							}
						}
					}

                    fnctn.setFunctionBlocks(blocks);
                    continue;
				} else if (fnctn != null && tag == GCOV_TAG_LINES) {
                    int numBlock = stream.readInt();
                    long[] lineNos = new long[length - 1];
                    int ix = 0;
                    do {
                        long lineNumber = stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK;
                        if (lineNumber != 0) {
                            if (ix == 0) {
                                lineNos[ix++] = 0;
                                lineNos[ix++] = source.getIndex();
                            }
                            lineNos[ix++] = lineNumber;
                            if (lineNumber >= source.getNumLines()) {
                                source.setNumLines((int) lineNumber + 1);
                            }
                        } else {
							String fileName = GcovStringReader.readString(stream, readBytes);
                            if (fileName.equals(Messages.GcnoRecordsParser_null_string)){
                                break;
                            }

                            source = findOrAdd(fileName);
                            lineNos[ix++] = 0;
                            lineNos[ix++] = source.getIndex();
                        }
                    } while (true);

                    fnctn.getFunctionBlocks().get((numBlock)).setEncoding(lineNos);
                    fnctn.getFunctionBlocks().get((numBlock)).setNumLine(ix);
                    continue;
				} else {
					if (currentTag != 0 && !isSubTag(currentTag, tag)) {
						fnctn = null;
						currentTag = 0;
					}
					// must skip data according to tag length (4 byte chunks) to get to next tag
					stream.skipBytes(readBytes ? length : length << 2);
				}
            } catch (EOFException e) {
                fnctn.setFunctionBlocks(blocks);
                fnctns.add(fnctn);
                break;
            }
        }// while
    }

	private boolean isSubTag(int tag1, int tag2) {
		int tagMask1 = (tag1 - 1) ^ tag1;
		int tagMask2 = (tag2 - 1) ^ tag2;

		return (tagMask1 >> 8) == tagMask2 && !(((tag2 ^ tag1) & ~tagMask1) != 0);
	}

	/* Getters */
    public ArrayList<GcnoFunction> getFnctns() {
        return fnctns;
    }
}
