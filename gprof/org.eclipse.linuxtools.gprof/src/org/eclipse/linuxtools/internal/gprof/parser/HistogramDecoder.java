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

import java.io.DataInput;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.symbolManager.Bucket;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;


/**
 * Reads the histogram in gmon files
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistogramDecoder {

	private static final int GMON_HDRSIZE_BSD44 = (3 * 4);

	private static final int GMON_HDRSIZE_BSD44_32 = (4 + 4 + 4 + 4 + 4 + GMON_HDRSIZE_BSD44);
	private static final int GMON_HDRSIZE_BSD44_64 = (8 + 8 + 4 + 4 + 4 + GMON_HDRSIZE_BSD44);
	private static final int GMON_HDRSIZE_OLDBSD_32 = (4 + 4 + 4) ;
	private static final int GMON_HDRSIZE_OLDBSD_64 = (8 + 8 + 4);


	/** the decoder */
	protected final GmonDecoder decoder;

	// histogram header
	/** Base pc address of sampled buffer */
	protected long lowpc;
	/** Max pc address of sampled buffer */
	protected long highpc;
	/** Profiling clock rate */
	protected int profRate;
	/** usually 's' for seconds, 'm' for milliseconds... */
	protected char dimenAbbrev;
	/** used when aggregate several gmon files */
	private boolean initialized = false;


	/** Histogram samples (shorts in the file!). */
	protected int[] hist_sample;
	/** Total time for all routines.  */
	protected double total_time;

	protected long bucketSize;


	/**
	 * Constructor
	 * @param decoder the Gmon decoder
	 */
	public HistogramDecoder(GmonDecoder decoder) {
		this.decoder = decoder;
	}

	protected long readAddress(DataInput stream) throws IOException {
		long ret = stream.readInt() & 0xFFFFFFFFL;
		return ret;
	}

	public boolean hasValues() {
		return (this.hist_sample != null && this.hist_sample.length > 0);
	}

	/**
	 * Decode the given stream
	 * @param stream a DataInputStream, pointing on a histogram header in a gmon file.
	 * @throws IOException if an IO error occurs
	 */
	public void decodeHeader(DataInput stream) throws IOException {
		long lowpc        = readAddress(stream);
		long highpc       = readAddress(stream);
		int hist_num_bins = stream.readInt();
		int prof_rate     = stream.readInt();
		byte[] bytes      = new byte[15];
		stream.readFully(bytes);
		byte b            = stream.readByte();

		if (!isCompatible(lowpc, highpc, prof_rate, hist_num_bins))	{
			// TODO exception to normalize
			throw new RuntimeException(Messages.HistogramDecoder_INCOMPATIBLE_HIST_HEADER_ERROR_MSG);
		}
		this.lowpc     = lowpc;
		this.highpc    = highpc;
		this.profRate = prof_rate;
		hist_sample    = new int[hist_num_bins]; // Impl note: JVM sets all integers to 0
		dimenAbbrev   = (char) b;
		long temp = highpc - lowpc;
		bucketSize = Math.round(temp/(double)hist_num_bins);
	}

	/**
	 * Decode the given stream
	 * @param stream a DataInputStream, pointing on a histogram header in a gmon file.
	 * @throws IOException if an IO error occurs
	 */
	public void decodeOldHeader(DataInput stream) throws IOException {
		long low_pc = readAddress(stream);
		long high_pc = readAddress(stream);
		int ncnt = stream.readInt();
		int version = stream.readInt();
		int header_size;
		int profrate = 0;
		if (version == GmonDecoder.GMONVERSION)
		{
			profrate = stream.readInt();
			stream.skipBytes(GMON_HDRSIZE_BSD44);
			if (decoder._32_bit_platform) {
		      header_size = GMON_HDRSIZE_BSD44_32;
			} else {
		      header_size = GMON_HDRSIZE_BSD44_64;
		    }
		} else {
		  /* Old style BSD format.  */
			if (decoder._32_bit_platform) {
				header_size = GMON_HDRSIZE_OLDBSD_32;
			} else {
				header_size = GMON_HDRSIZE_OLDBSD_64;
			}
		}

		int samp_bytes = ncnt - header_size;
		int hist_num_bins = samp_bytes / 2;

		if (!isCompatible(low_pc, high_pc, profrate, hist_num_bins))
		{
			// TODO exception to normalize
			throw new RuntimeException(Messages.HistogramDecoder_INCOMPATIBLE_HIST_HEADER_ERROR_MSG);
		}


		this.lowpc     = low_pc;
		this.highpc    = high_pc;
		this.profRate = profrate;
		hist_sample    = new int[hist_num_bins]; // Impl note: JVM sets all integers to 0
		dimenAbbrev   = 's';
		long temp = highpc - lowpc;
		bucketSize = Math.round(temp/(double)hist_num_bins);
	}


	/**
	 * Checks whether the gmon file currently parsed is compatible with the previous one (if any).
	 * @param lowpc
	 * @param highpc
	 * @param profrate
	 * @param sample_count
	 * @return whether the gmon file currently parsed is compatible with the previous one (if any).
	 */
	private boolean isCompatible(long lowpc, long highpc, int profrate, int sample_count) {
		if (!initialized) return true;
		return (
				(this.lowpc     == lowpc) &&
				(this.highpc    == highpc) &&
				(this.profRate == profrate) &&
				(this.hist_sample.length == sample_count)
		);
	}



	/**
	 * Reads hitogram record
	 * @param stream a DataInputStream, pointing just after histogram header in a gmon file.
	 * @throws IOException if an IO error occurs
	 */
	public void decodeHistRecord(DataInput stream) throws IOException {
		for (int i = 0; i<hist_sample.length; i++) {
			short rv = stream.readShort();
			if (rv != 0) {
				int hist_size = (rv & 0xFFFF);
				hist_sample[i] += hist_size;
			}
		}
	}


	/**
	 * Print the histogram header, for debug usage.
	 * @param ps a printstream (typically System.out)
	 */
	public void printHistHeader(PrintStream ps)	{
		ps.println(" \nHistogram Header : \n"); //$NON-NLS-1$
		ps.print("  Base pc address of sample buffer = 0x"); //$NON-NLS-1$
		ps.println(Long.toHexString(lowpc));
		ps.print("  Max pc address of sampled buffer = 0x"); //$NON-NLS-1$
		ps.println(Long.toHexString(highpc));
		ps.print("  Number of histogram samples      = "); //$NON-NLS-1$
		ps.println(hist_sample.length);
		ps.print("  Profiling clock rate             = "); //$NON-NLS-1$
		ps.println(profRate);
//		ps.print("  Physical dimension usually \"seconds\" = ");
//		ps.println(dimen);
		ps.print("  Physical dimension abreviation : 's' for \"seconds\"  'm' for \"milliseconds\" = "); //$NON-NLS-1$
		ps.println(dimenAbbrev);
	}

	/**
	 * Print the histogram, for debug usage.
	 * @param ps a printstream (typically System.out)
	 */
	public void printHistRecords(PrintStream ps) {
		ps.println();
		ps.println(" ==  HISTOGRAM RECORDS  == "); //$NON-NLS-1$
		ps.println(" ========================= "); //$NON-NLS-1$

		printHistHeader(ps);

		/*ps.println(" \nHistogram Samples : ");
        ISymbol[] symbols = this.decoder.getProgram().getSymbols();
        for (ISymbol iSymbol : symbols) {
			ps.println(iSymbol.getName() + "\t" + iSymbol.getAddress());
		}
        for (int i = 0; i<hist_sample.length; i++) {
        	ps.println("histSample[" + i + "]\t" + hist_sample[i]);
        }*/
	}

	/**
	 * Assign the hits to the given symbols
	 * @param symblist
	 */
	public void assignSamplesSymbol() {
		if (hist_sample == null || hist_sample.length == 0) return;
		ISymbol[] symblist = this.decoder.getProgram().getSymbols();
		/* read samples and assign to namelist symbols */
		int j = 1;
		for (int i = 0; i < hist_sample.length; i++)
		{
			int ccnt = hist_sample[i];
			if (ccnt != 0)
			{
				long pcl = lowpc + (bucketSize*i);
				long pch = pcl+bucketSize;
				total_time += ccnt;
				long svalue0;
				long svalue1 = symblist[j-1].getAddress().getValue().longValue();
				for (j = j-1; j < symblist.length - 1; j++)
				{
					svalue0 = svalue1;
					svalue1 = symblist[j+1].getAddress().getValue().longValue();
					/* if high end of tick is below entry address,
					 * go for next tick. */
					if(pch < svalue0) {
						break;
					}
					/* if low end of tick into next routine,
					 * go for next routine. */
					if(pcl < svalue1) {
						long start_addr = pcl>svalue0?pcl:svalue0;
						long end_addr   = pch<svalue1?pch:svalue1;
						long overlap = end_addr - start_addr;
						if(overlap > 0)	{
							ISymbol symbol = symblist[j];
							int time = (int) ((overlap * ccnt) / bucketSize);
							Bucket   bck = new Bucket(start_addr, end_addr, time);
							addBucket(bck,symbol);
						}
					}
				}
			}
		}
	}

	private void addBucket(Bucket b, ISymbol s) {
		HistRoot root = this.decoder.getRootNode();
		root.addBucket(b, s, decoder.getProgram());
	}

	/**
	 * @return the profRate
	 */
	public int getProfRate() {
		return profRate;
	}

	/**
	 *
	 * @return 's' for seconds, 'm' for ms, 'u' for �s....
	 */
	public char getTimeDimension() {
		return dimenAbbrev;
	}

	/**
	 * get the bucket size
	 */
	public long getBucketSize(){
		return bucketSize;
	}
}
