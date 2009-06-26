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

import java.io.DataInput;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.linuxtools.gprof.symbolManager.Bucket;
import org.eclipse.linuxtools.gprof.view.histogram.HistRoot;


/**
 * Reads the histogram in gmon files
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistogramDecoder {

	/** the decoder */
	private final GmonDecoder decoder;

	// histogram header
	/** Base pc address of sampled buffer */
	private long lowpc;
	/** Max pc address of sampled buffer */
	private long highpc;
	/** Profiling clock rate */
	private int prof_rate;
	/** physical dimension - usually "seconds" */
	private String dimen;
	/** usually 's' for seconds, 'm' for milliseconds... */
	private char dimen_abbrev;
	/** used when aggregate several gmon files */
	private boolean initialized = false;


	/** Histogram samples (shorts in the file!). */
	private int[] hist_sample;
	/** Total time for all routines.  */
	private double total_time;
	private double scale;

	private long bucketSize;


	/**
	 * Constructor 
	 * @param decoder the Gmon decoder
	 */
	public HistogramDecoder(GmonDecoder decoder) {
		this.decoder = decoder;
	}

	public boolean hasValues() {
		return (this.hist_sample != null && this.hist_sample.length > 0);
	}


	/**
	 * Shortcut for the following three methods:
	 * <ul>
	 *   <li> {@link #decodeHeader(DataInput)}
	 *   <li> {@link #decodeHistRecord(DataInput)}
	 *   <li> {@link #AssignSamplesSymbol()}
	 * </ul>
	 * 
	 * Useful only when no aggrgation is performed
	 * (ie when there is only one gmon file)
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public void decodeAll(DataInput stream) throws IOException {
		decodeHeader(stream);
		decodeHistRecord(stream);
		AssignSamplesSymbol();
	}


	/**
	 * Decode the given stream
	 * @param stream a DataInputStream, pointing on a histogram header in a gmon file.
	 * @throws IOException if an IO error occurs
	 */
	public void decodeHeader(DataInput stream) throws IOException {
		int _lowpc        = stream.readInt();
		long lowpc        = (_lowpc & 0xFFFFFFFFL);
		int _highpc       = stream.readInt();
		long highpc       = (_highpc & 0xFFFFFFFFL);
		int hist_num_bins = stream.readInt();
		int prof_rate     = stream.readInt();
		byte[] bytes      = new byte[15];
		stream.readFully(bytes);
		byte b            = stream.readByte();

		if (!isCompatible(lowpc, highpc, prof_rate, hist_num_bins))
		{
			// TODO exception to normalize
			throw new RuntimeException("Histogram header's incompatibility among gmon files");
		}
		this.lowpc     = lowpc;
		this.highpc    = highpc;
		this.prof_rate = prof_rate;
		hist_sample    = new int[hist_num_bins]; // Impl note: JVM sets all integers to 0
		dimen          = new String(bytes);
		dimen_abbrev   = (char) b;
		scale          = (highpc - lowpc)/(double)hist_num_bins;
		bucketSize     = (highpc - lowpc)/(hist_num_bins - 1);
		if (bucketSize > scale) scale = bucketSize;
	}

	/**
	 * Checks whether the gmon file currently parsed is compatible with the previous one (if any).
	 * @param lowpc
	 * @param highpc
	 * @param profrate
	 * @param sample_count
	 * @return whether the gmon file currently parsed is compatible with the previous one (if any).
	 */
	public boolean isCompatible(long lowpc, long highpc, int profrate, int sample_count) {
		if (!initialized) return true;
		return (
				(this.lowpc     == lowpc) &&
				(this.highpc    == highpc) &&
				(this.prof_rate == profrate) &&
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
			short _rv = stream.readShort();
			int hist_size = (_rv & 0xFFFF);
			hist_sample[i] += hist_size;
		}
	}


	/**
	 * Print the histogram header, for debug usage.
	 * @param ps a printstream (typically System.out)
	 */
	public void printHistHeader(PrintStream ps)
	{
		ps.println(" \nHistogram Header : \n");
		ps.print("  Base pc address of sample buffer = 0x");
		ps.println(Long.toHexString(lowpc));
		ps.print("  Max pc address of sampled buffer = 0x");
		ps.println(Long.toHexString(highpc));
		ps.print("  Number of histogram samples      = ");
		ps.println(hist_sample.length);
		ps.print("  Profiling clock rate             = ");
		ps.println(prof_rate);
//		ps.print("  Physical dimension usually \"seconds\" = ");
//		ps.println(dimen);
		ps.print("  Physical dimension abreviation : 's' for \"seconds\"  'm' for \"milliseconds\" = ");
		ps.println(dimen_abbrev);
	}

	/**
	 * Print the histogram, for debug usage.
	 * @param ps a printstream (typically System.out)
	 */
	public void printHistRecords(PrintStream ps) {
		ps.println();
		ps.println(" ==  HISTOGRAM RECORDS  == ");
		ps.println(" ========================= ");

		printHistHeader(ps);

		/*ps.println(" \nHistogram Samples : ");
        ISymbol[] symbols = this.program.getSymbols();
        for (int i = 0 ; i< symbols.length; i++) {
        	if (buckets[i] != null) {
            	System.out.println(symbols[i].getName());
        		for (Bucket b : buckets[i]) {
        			System.out.println("  " + b.start_addr + " :: " + b.time);
        		}
        	}
		}*/

	}

	/**
	 * Assign the hits to the given symbols
	 * @param symblist 
	 */
	public void AssignSamplesSymbol()
	{
		if (hist_sample == null || hist_sample.length == 0) return;
		ISymbol[] symblist = this.decoder.getProgram().getSymbols();
		/* read samples and assign to namelist symbols */
		int j = 1;
		for (int i = 0; i < hist_sample.length; i++)
		{
			int ccnt = hist_sample[i];
			if (ccnt != 0)
			{
				long pcl = lowpc + (long) (scale*i);
				long pch = lowpc + (long) (scale * (i+1));
				total_time += ccnt;
				long svalue0;
				long svalue1 = symblist[j-1].getAddress().getValue().longValue();
				for (j = j-1; j < symblist.length - 1; j++)
				{
					svalue0 = svalue1;
					svalue1 = symblist[j+1].getAddress().getValue().longValue();
					/* if high end of tick is below entry address,
					 * go for next tick. */
					if(pch < svalue0)
						break;
					/* if low end of tick into next routine,
					 * go for next routine. */
					if(pcl < svalue1)
					{   
						long start_addr = pcl>svalue0?pcl:svalue0;
						long end_addr   = pch<svalue1?pch:svalue1;
						long overlap = end_addr - start_addr;
						if(overlap > 0)
						{
							int time = (int) ((overlap * ccnt) / scale);
							Bucket   bck = new Bucket(start_addr, end_addr, time);
							addBucket(bck,symblist[j]);
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
	 * @return the prof_rate
	 */
	public int getProf_rate() {
		return prof_rate;
	}

	/**
	 * 
	 * @return 's' for seconds, 'm' for ms, 'u' for �s....
	 */
	public char getTimeDimension() {
		return dimen_abbrev;
	}

	/**
	 * get the bucket size
	 */
	public long getBucketSize(){
		return bucketSize;
	}
	
	
	
	
}
