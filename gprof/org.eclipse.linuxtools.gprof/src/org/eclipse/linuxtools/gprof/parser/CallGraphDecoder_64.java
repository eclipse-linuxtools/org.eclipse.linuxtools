package org.eclipse.linuxtools.gprof.parser;

import java.io.DataInput;
import java.io.IOException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;

public class CallGraphDecoder_64 extends CallGraphDecoder{

	
	
	public CallGraphDecoder_64(GmonDecoder decoder) {
		super(decoder);
		
	}

	@Override
	public void decodeCallGraphRecord(DataInput stream) throws IOException {
		/*int _from_pc = stream.readInt();
		int _self_pc = stream.readInt();
		long from_pc = _from_pc & 0xFFFFFFFFL;
		long self_pc = _self_pc & 0xFFFFFFFFL;*/
		long from_pc = stream.readLong();
		long self_pc = stream.readLong();
		int count    = stream.readInt();
		IBinaryObject program = decoder.getProgram();
		IAddressFactory addressFactory = program.getAddressFactory();
		IAddress parentAddress = addressFactory.createAddress(Long.toString(from_pc));
		ISymbol  parentSymbol  = program.getSymbol(parentAddress);
		IAddress childAddress  = addressFactory.createAddress(Long.toString(self_pc));
		ISymbol  childSymbol   = program.getSymbol(childAddress);
		if (childSymbol == null || parentSymbol == null) {
			return;
		}
		addCallArc(parentSymbol, parentAddress, childSymbol, count);
	}

	
}
