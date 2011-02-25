/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.utils.internal;

import java.io.InputStreamReader;
import java.io.InputStream;

/**
 * Thread for reading input and output streams
 */
public class StreamReaderThread extends Thread
{
    StringBuffer mOut;
    InputStreamReader mIn;
    
    public StreamReaderThread(InputStream in, StringBuffer out)
    {
    mOut=out;
    mIn=new InputStreamReader(in);
    }
    
    public void run()
    {
    int ch;
    try {
        while(-1 != (ch=mIn.read()))
            mOut.append((char)ch);
        }
    catch (Exception e)
        {
        mOut.append("\nRead error:"+e.getMessage());
        }
    }
}
