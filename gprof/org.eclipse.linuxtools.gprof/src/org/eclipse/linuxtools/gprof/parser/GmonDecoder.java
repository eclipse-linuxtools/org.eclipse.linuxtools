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
package org.eclipse.linuxtools.gprof.parser;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.gprof.utils.LEDataInputStream;
import org.eclipse.linuxtools.gprof.view.histogram.HistRoot;



/** 
 * Parser of gmon file
 */
public class GmonDecoder {

	/** Histogram record type.  */ 
	public static final int VPF_GMON_RECORD_TYPE_HISTOGRAM = 0;
	/** Callgraph record type. */
	public static final int VPF_GMON_RECORD_TYPE_CALLGRAPH = 1;
	/** Unkwown record type. */
	public static final int VPF_GMON_RECORD_TYPE_UNKNOWN   = -1;

	// header
	private String cookie;
	private int gmon_version;
	private byte[] spare;

	private final IBinaryObject program;
	private final HistogramDecoder histo;
	private final CallGraphDecoder callGraph;
	private final PrintStream ps;
	private final HistRoot rootNode = new HistRoot(this);
	private String file;
	private int tag = -1;
	
	private final HashMap<ISymbol, String> filenames = new HashMap<ISymbol, String>();
	
	// for dump
	private boolean shouldDump = false;


	/**
	 * Constructor
	 * @param program 
	 */
	public GmonDecoder(IBinaryObject program) {
		this(program, null);
	}
	
	
	/**
	 * Constructor
	 * @param program 
	 */
	public GmonDecoder(IBinaryObject program, PrintStream ps) {
		this.program = program;
		histo = new HistogramDecoder(this);
		callGraph = new CallGraphDecoder(this);
		this.ps = ps;
	}
	

	/**
	 * Reads the given file
	 * @param file
	 * @throws IOException
	 */
	public void read(String file) throws IOException {
		this.file = file;
		DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		if(program.isLittleEndian()){
			LEDataInputStream s = new LEDataInputStream(stream);
			read(s);
		}else read(stream);
	}

	/**
	 * Reads the given file as a stream
	 * @param stream
	 * @throws IOException
	 */
	public void read(DataInput stream) throws IOException {
		readHeader(stream);
		ReadGmonContent(stream);
		((FilterInputStream)stream).close();		
	}



	/**
	 * Read gmon header
	 * @param stream the gmon as a stream
	 * @throws IOException if an IO error occurs or if the stream is not a gmon file.
	 */
	public void readHeader(DataInput stream) throws IOException {
		byte[] _cookie = new byte[4];
		stream.readFully(_cookie);
		cookie = new String(_cookie);
		if (!"gmon".equals(cookie)) {
			throw new IOException("Invalid gmon file");
		}
		gmon_version = stream.readInt();
		spare = new byte[12];
		stream.readFully(spare);
	}

	/**
	 * Read the whole content of the GMON file
	 * The header should be read before calling this function.
	 * @param stream
	 * @throws IOException 
	 */
	public void ReadGmonContent(DataInput stream) throws IOException
	{  
		do {
			//int tag = -1;
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
				callGraph.decodeCallGraphRecord(stream);
				break;
			default:
				throw new IOException("Error while reading GMON content : Found bad tag (file corrupted?) ");
			}
			
			if (shouldDump == true)
			 dumpGmonResult(ps==null?System.out:ps);
						
		} while (true);

		this.callGraph.populate(rootNode);
		this.histo.AssignSamplesSymbol();	
	
	}

	
	public void dumpGmonResult(PrintStream ps) throws FileNotFoundException{
		
		ps.println("-- gmon Results --");
		ps.println("cookie "+cookie);
		ps.println("gmon_version "+gmon_version);
		//ps.println("spare "+new String(spare));
		ps.println("tag "+tag);
		
		switch (tag) {
		case VPF_GMON_RECORD_TYPE_HISTOGRAM:
			histo.printHistHeader(ps);
			histo.printHistRecords(ps);
			break;
		default :
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
			ret = STSymbolManager.sharedInstance.getFilename(s);
			if (ret == null) ret = "??";
			filenames.put(s, ret);
		}
		return ret;
	}


	public boolean isICache() {
		IPath p = new Path(this.file);
		String s = p.lastSegment();
		return (s.endsWith("ICACHE"));
	}

	public boolean isDCache() {
		IPath p = new Path(this.file);
		String s = p.lastSegment();
		return (s.endsWith("DCACHE"));
	}

	public void setShouldDump(boolean shouldDump) {
		this.shouldDump = shouldDump;
	}

	
}
