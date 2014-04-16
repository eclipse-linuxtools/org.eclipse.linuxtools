package org.eclipse.linuxtools.internal.gprof.parser;

import java.io.DataInput;
import java.io.IOException;

public class HistogramDecoder64 extends HistogramDecoder {

    public HistogramDecoder64(GmonDecoder decoder) {
        super(decoder);
    }

    @Override
    protected long readAddress(DataInput stream) throws IOException {
        return stream.readLong();
    }


}
