/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.utils;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
/**
 *
 */
public class BEDataInputStream implements DataInput {

    private final DataInputStream in;
    private final byte[] buffer = new byte[8];

    /**
     * Constructor
     * @param in
     */
    public BEDataInputStream(DataInputStream in) {
        this.in = in;
    }


    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readShort()
     */
    @Override
    public final short readShort() throws IOException
    {
        return in.readShort();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readUnsignedShort()
     */
    @Override
    public final int readUnsignedShort() throws IOException
    {
        return in.readUnsignedShort();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readChar()
     */
    @Override
    public final char readChar() throws IOException
    {
        return in.readChar();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readInt()
     */
    @Override
    public final int readInt() throws IOException
    {
        return in.readInt();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readLong()
     */
    @Override
    public final long readLong() throws IOException
    {
        in.readFully(buffer, 0, 8);
        return (((long)buffer[7] << 32) +
                ((long)(buffer[6] & 255) << 40) +
                ((long)(buffer[5] & 255) << 48) +
                ((long)(buffer[4] & 255) << 56) +
                ((long)(buffer[3] & 255) << 0) +
                ((buffer[2] & 255) << 8) +
                ((buffer[1] & 255) <<  16) +
                ((buffer[0] & 255) <<  24));
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readFloat()
     */
    @Override
    public final float readFloat() throws IOException
    {
        return Float.intBitsToFloat(readInt());
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readDouble()
     */
    @Override
    public final double readDouble() throws IOException
    {
        return Double.longBitsToDouble(readLong());
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readBoolean()
     */
    @Override
    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readByte()
     */
    @Override
    public byte readByte() throws IOException {
        return in.readByte();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readFully(byte[])
     */
    @Override
    public void readFully(byte[] b) throws IOException {
        in.readFully(b);
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        in.readFully(b,off,len);
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readLine()
     */
    @Override
    @Deprecated
    public String readLine() throws IOException {
        return in.readLine();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readUTF()
     */
    @Override
    public String readUTF() throws IOException {
        return in.readUTF();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readUnsignedByte()
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return in.readUnsignedByte();
    }

    /*
     * (non-Javadoc)
     * @see java.io.DataInput#skipBytes(int)
     */
    @Override
    public int skipBytes(int n) throws IOException {
        return in.skipBytes(n);
    }

    /**
     * Close this stream.
     */
    public void close() throws IOException {
        in.close();
    }

    public final long readUnsignedInt() throws IOException
    {
        in.readFully(buffer, 0, 4);
        return
        ((
        (buffer[0])      << 24 |
        (buffer[1]&0xff) << 16 |
        (buffer[2]&0xff) <<  8 |
        (buffer[3]&0xff)
        )
         & MasksGenerator.UNSIGNED_INT_MASK );
    }



}
