package org.eclipse.linuxtools.gprof.parser;

import java.io.DataInput;
import java.io.IOException;

public class HistogramDecoder_64 extends HistogramDecoder {
	
	
	public HistogramDecoder_64(GmonDecoder decoder) {
		super(decoder);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void decodeHeader(DataInput stream) throws IOException {
		//int _lowpc        = stream.readInt();
		//long lowpc        = (_lowpc & 0xFFFFFFFFL);
		//int _highpc       = stream.readInt();
		//long highpc       = (_highpc & 0xFFFFFFFFL);
		long lowpc = stream.readLong();
		long highpc = stream.readLong();
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
		long temp = highpc - lowpc;
		bucketSize = Math.round(temp/(double)hist_num_bins);
	}

	
}
