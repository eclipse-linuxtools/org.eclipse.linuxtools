package org.eclipse.linuxtools.internal.gprof.parser;

import java.io.DataInput;
import java.io.IOException;

public class CallGraphDecoder_64 extends CallGraphDecoder{
	
	public CallGraphDecoder_64(GmonDecoder decoder) {
		super(decoder);
	}

	@Override
	protected long readAddress(DataInput stream) throws IOException {
		long ret = stream.readLong();
		return ret;
	}
	
}
