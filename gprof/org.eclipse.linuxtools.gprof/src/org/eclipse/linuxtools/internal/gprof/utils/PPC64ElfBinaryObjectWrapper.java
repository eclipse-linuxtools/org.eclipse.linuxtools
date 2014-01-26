/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.parser.ElfBinaryObject;
import org.eclipse.core.runtime.IPath;

public class PPC64ElfBinaryObjectWrapper extends ElfBinaryObject {
	private Elf.Section dataSection = null;
	private ISymbol[] symbols = null;

	public PPC64ElfBinaryObjectWrapper(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

	private IAddress fixAddr(IAddress addr) {
		try {
			//PPC64 is big endian, so we don't need to worry with byte order
			InputStream input = getContents();
			byte bytes[]=new byte[8];
			long index = addr.getValue().longValue() - dataSection.sh_addr.getValue().longValue() +
					dataSection.sh_offset;

			input.skip(index);
			input.read(bytes);
			return new Addr64(new BigInteger(bytes));
		} catch(IOException e) {
			return null;
		}
	}

	@Override
	public ISymbol[] getSymbols() {
		if (symbols != null)
			return symbols;

		symbols = super.getSymbols();
		try {
			if (dataSection == null) {
				Elf elf = new Elf(getPath().toOSString());
				dataSection = elf.getSectionByName(".data"); //$NON-NLS-1$
			}
		} catch  (IOException e) {
		}

		//Failed to load data Section
		if (dataSection == null)
			return symbols;

		LinkedList<ISymbol> list = new LinkedList<>();
		for (ISymbol s : symbols)
			if (s.getType() == ISymbol.FUNCTION && s instanceof Symbol){
				IAddress addr = fixAddr(s.getAddress());
				if (addr == null)
					addr = s.getAddress();
				list.add(new Symbol((BinaryObjectAdapter)s.getBinaryObject(), s.getName(), s.getType(), addr, s.getSize()));
			} else {
				list.add(s);
			}

		symbols = list.toArray(new Symbol[0]);
		Arrays.sort(symbols);

		return symbols;
	}
}
