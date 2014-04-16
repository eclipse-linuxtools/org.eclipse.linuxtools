package org.eclipse.linuxtools.internal.gprof.parser;

import java.io.DataInput;
import java.io.IOException;

public class CallGraphDecoder64 extends CallGraphDecoder{

    public CallGraphDecoder64(GmonDecoder decoder) {
        super(decoder);
    }

    @Override
    protected long readAddress(DataInput stream) throws IOException {
        return stream.readLong();
    }

}
