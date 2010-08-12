/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.IStreams.DoneRead;

public class ValgrindTCFInputStream extends InputStream {
	private IStreams streamsService;
	private String streamId;
	private transient boolean done;
	private boolean eos;
	private byte[] buf;
	private Exception ex;
	
	public ValgrindTCFInputStream(IChannel channel, String streamId) {
		this.streamId = streamId;
		streamsService = channel.getRemoteService(IStreams.class);
	}

	@Override
	public int read() throws IOException {
		if (eos) {
			return -1;
		}
		
		read1(1);
		
		// Check again if we read EOS
		if (eos) {
			return -1;
		}
		
		return buf[0];
	}

	private void read1(final int size) throws IOException {		
		done = false;
		ex = null;
		buf = null;

		streamsService.read(streamId, size, new DoneRead() {

			public void doneRead(IToken token, Exception error, int lost_size,
					byte[] data, boolean eos) {
				if (error != null) {
					ex = error;
				}
				else {
					buf = data;
					if (eos) { // FIXME We are getting EOS too soon, and losing data
						ValgrindTCFInputStream.this.eos = true;
						streamsService.disconnect(streamId, new IStreams.DoneDisconnect() {
							public void doneDisconnect(IToken token, Exception error) {
								if (error != null) {
									ex = error;
								}
							}
						});
					}
				}
				done = true;
			}
		});
		
		try {
			while (!done) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
		}
		
		if (ex != null) {
			IOException ioe = new IOException("Read failed");
			ioe.initCause(ex);
			throw ioe;
		}
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		
		if (eos) {
			return -1;
		}
		
		read1(len);
		
		// Check again if we read EOS
		if (eos) {
			return -1;
		}
		
		// Did we read less than requested?
		if (buf.length < len) {
			len = buf.length;
		}
		
		System.arraycopy(buf, 0, b, off, len);
		return len;
	}
}
