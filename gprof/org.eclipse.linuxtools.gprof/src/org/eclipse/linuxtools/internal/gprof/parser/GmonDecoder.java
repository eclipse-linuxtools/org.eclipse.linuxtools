/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.parser;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.utils.LEDataInputStream;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;

/**
 * Parser of gmon file
 */
public class GmonDecoder {

    /** Histogram record type. */
    public static final int VPF_GMON_RECORD_TYPE_HISTOGRAM = 0;
    /** Callgraph record type. */
    public static final int VPF_GMON_RECORD_TYPE_CALLGRAPH = 1;
    /** Unkwown record type. */
    public static final int VPF_GMON_RECORD_TYPE_UNKNOWN = -1;

    public static final int GMONVERSION = 0x00051879;

    // header
    private String cookie;
    private int gmon_version;
    private byte[] spare;

    private final IBinaryObject program;
    final boolean _32_bit_platform;
    private HistogramDecoder histo;
    private CallGraphDecoder callGraph;
    private final PrintStream ps;
    private final HistRoot rootNode = new HistRoot(this);
    private String file;
    private int tag = -1;

    private final HashMap<ISymbol, String> filenames = new HashMap<ISymbol, String>();
    private final IProject project;

    // for dump
    private boolean shouldDump = false;

    /**
     * Constructor
     *
     * @param program
     * @throws IOException
     */
    public GmonDecoder(IBinaryObject program, IProject project) {
        this(program, null, project);
    }

    /**
     * Constructor
     *
     * @param program
     * @throws IOException
     */
    public GmonDecoder(IBinaryObject program, PrintStream ps, IProject project) {
        this.program = program;
        this.ps = ps;
        this.project = project;
        program.getBinaryParser().getFormat();
        String cpu = program.getCPU();
        if (Platform.ARCH_X86_64.equals(cpu) || "ppc64".equals(cpu)) { //$NON-NLS-1$
            histo = new HistogramDecoder_64(this);
            callGraph = new CallGraphDecoder_64(this);
            _32_bit_platform = false;
        } else {
            _32_bit_platform = true;
            histo = new HistogramDecoder(this);
            callGraph = new CallGraphDecoder(this);
        }
    }

    /**
     * Reads the given file
     *
     * @param file
     * @throws IOException
     */
    public void read(String file) throws IOException {
        this.file = file;
        DataInputStream beStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        if (program.isLittleEndian()) {
            LEDataInputStream leStream = new LEDataInputStream(beStream);
            leStream.mark(1000);
            boolean gmonType = readHeader(leStream);
            if (gmonType)
                ReadGmonContent(leStream);
            else {
                leStream.reset();
                histo.decodeOldHeader(leStream);
                histo.decodeHistRecord(leStream);
                try {
                    do {
                        this.callGraph.decodeCallGraphRecord(leStream, true);
                    } while (true);
                } catch (EOFException _) {
                    // normal. End of file reached.
                }
                this.callGraph.populate(rootNode);
                this.histo.AssignSamplesSymbol();
            }
            leStream.close();
        } else {
            beStream.mark(1000);
            boolean gmonType = readHeader(beStream);
            if (gmonType)
                ReadGmonContent(beStream);
            else {
                beStream.reset();
                histo.decodeOldHeader(beStream);
                histo.decodeHistRecord(beStream);
                try {
                    do {
                        this.callGraph.decodeCallGraphRecord(beStream, true);
                    } while (true);
                } catch (EOFException _) {
                    // normal. End of file reached.
                }
                this.callGraph.populate(rootNode);
                this.histo.AssignSamplesSymbol();
            }
            beStream.close();
        }
    }

    /**
     * Read gmon header
     *
     * @param stream
     *            the gmon as a stream
     * @throws IOException
     *             if an IO error occurs or if the stream is not a gmon file.
     */
    public boolean readHeader(DataInput stream) throws IOException {
        byte[] _cookie = new byte[4];
        stream.readFully(_cookie);
        cookie = new String(_cookie);
        gmon_version = stream.readInt();
        spare = new byte[12];
        stream.readFully(spare);
        return "gmon".equals(cookie); //$NON-NLS-1$
    }

    /**
     * Read the whole content of the GMON file The header should be read before calling this function.
     *
     * @param stream
     * @throws IOException
     */
    public void ReadGmonContent(DataInput stream) throws IOException {
        do {
            // int tag = -1;
            tag = -1;

            try {
                tag = stream.readByte();
            } catch (EOFException _) {
                break;
            }
            switch (tag) {
            case VPF_GMON_RECORD_TYPE_HISTOGRAM:
                histo.decodeHeader(stream);
                histo.decodeHistRecord(stream);
                break;
            case VPF_GMON_RECORD_TYPE_CALLGRAPH:
                callGraph.decodeCallGraphRecord(stream, false);
                break;
            default:
                throw new IOException(Messages.GmonDecoder_BAD_TAG_ERROR);
            }

            if (shouldDump == true)
                dumpGmonResult(ps == null ? System.out : ps);

        } while (true);

        this.callGraph.populate(rootNode);
        this.histo.AssignSamplesSymbol();

    }

    public void dumpGmonResult(PrintStream ps) {

        ps.println("-- gmon Results --"); //$NON-NLS-1$
        ps.println("cookie " + cookie); //$NON-NLS-1$
        ps.println("gmon_version " + gmon_version); //$NON-NLS-1$
        // ps.println("spare "+new String(spare));
        ps.println("tag " + tag); //$NON-NLS-1$

        switch (tag) {
        case VPF_GMON_RECORD_TYPE_HISTOGRAM:
            histo.printHistHeader(ps);
            histo.printHistRecords(ps);
            break;
        default:
            break;
        }
    }

    /**
     * @return the histogram decoder
     */
    public HistogramDecoder getHistogramDecoder() {
        return histo;
    }

    /**
     * @return the call graph decoder
     */
    public CallGraphDecoder getCallGraphDecoder() {
        return callGraph;
    }

    /**
     * @return the program
     */
    public IBinaryObject getProgram() {
        return program;
    }

    /**
     * @return the rootNode
     */
    public HistRoot getRootNode() {
        return rootNode;
    }

    /**
     * Gets the version number parsed in the gmon file
     *
     * @return a gmon version
     */
    public int getGmonVersion() {
        return gmon_version;
    }

    /**
     * @return the (last) parsed gmon file
     */
    public String getGmonFile() {
        return file;
    }

    public String getFileName(ISymbol s) {
        String ret = filenames.get(s);
        if (ret == null) {
            ret = STSymbolManager.sharedInstance.getFilename(s, project);
            if (ret == null)
                ret = "??"; //$NON-NLS-1$
            filenames.put(s, ret);
        }
        return ret;
    }

    public void setShouldDump(boolean shouldDump) {
        this.shouldDump = shouldDump;
    }

    public IProject getProject() {
        return this.project;
    }
}
