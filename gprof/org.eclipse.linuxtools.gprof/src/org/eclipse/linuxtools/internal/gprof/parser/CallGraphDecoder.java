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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphArc;
import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphNode;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;


/**
 * This class in on charge of parsing the call graph section
 * of gmon files
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CallGraphDecoder {

    protected final GmonDecoder decoder;


    private final Map<ISymbol, CallGraphNode> nodes = new HashMap<>();

    /**
     * Constructor
     * @param decoder the Gmon decoder
     */
    public CallGraphDecoder(GmonDecoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Decode call-graph record from gmon file.
     * @param stream
     * @throws IOException
     */
    public void decodeCallGraphRecord(DataInput stream, boolean bsdFormat) throws IOException {
        long from_pc = readAddress(stream);
        long self_pc = readAddress(stream);
        int count    = bsdFormat?(int)readAddress(stream):stream.readInt();
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


    protected long readAddress(DataInput stream) throws IOException {
        return stream.readInt() & 0xFFFFFFFFL;
    }


    private void addCallArc(ISymbol parent, IAddress parentAddress, ISymbol child, int count) {
        CallGraphNode parentNode = nodes.get(parent);
        CallGraphNode childNode  = nodes.get(child);
        if (parentNode == null) {
            parentNode = new CallGraphNode(parent);
            nodes.put(parent, parentNode);
        }
        if (childNode == null) {
            childNode = new CallGraphNode(child);
            nodes.put(child, childNode);
        }
        CallGraphArc arc = parentNode.getOutputArc(childNode);
        if (arc == null) {
            arc = new CallGraphArc(parentNode, parentAddress, childNode, count, decoder.getProgram(), decoder.getProject());
            parentNode.getChildren().add(arc);
            childNode.getParents().add(arc);
        } else {
            arc.setCount(arc.getCount() + count);
        }
    }

    void populate(HistRoot rootNode) {
        for (CallGraphNode callGraphNode : nodes.values()) {
            rootNode.addCallGraphNode(callGraphNode);
        }
    }


}
