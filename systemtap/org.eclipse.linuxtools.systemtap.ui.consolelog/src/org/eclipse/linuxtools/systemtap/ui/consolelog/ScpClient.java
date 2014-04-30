/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.RemoteScriptOptions;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ScpClient {

    private Session session;

    /**
     * @since 3.0
     */
    public ScpClient(RemoteScriptOptions remoteOptions) throws JSchException {

        JSch jsch = new JSch();

        session = jsch.getSession(remoteOptions.userName, remoteOptions.hostName, 22);

        session.setPassword(remoteOptions.password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no"); //$NON-NLS-1$ //$NON-NLS-2$
        session.setConfig(config);
        session.connect();
    }

    public void transfer(String fromFile, String toFile) throws IOException,
    JSchException {
        String rfile = toFile;
        String lfile = fromFile;
        String command = "scp -t " + rfile; //$NON-NLS-1$
        Channel channel = session.openChannel("exec"); //$NON-NLS-1$
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        try (OutputStream out = channel.getOutputStream();
                InputStream in = channel.getInputStream()) {

            channel.connect();

            if (checkAck(in) != 0) {
                System.out.println("err"); //$NON-NLS-1$
            }

            // send "C0644 filesize filename", where filename should not include
            // '/'
            long filesize = (new File(lfile)).length();
            command = "C0644 " + filesize + " "; //$NON-NLS-1$ //$NON-NLS-2$
            if (lfile.lastIndexOf('/') > 0) {
                command += lfile.substring(lfile.lastIndexOf('/') + 1);
            } else {
                command += lfile;
            }
            command += "\n"; //$NON-NLS-1$

            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                System.out.println("err"); //$NON-NLS-1$
            }

            // send a content of lfile
            byte[] buf = new byte[1024];
            try (FileInputStream fis = new FileInputStream(lfile)) {
                while (true) {
                    int len = fis.read(buf, 0, buf.length);
                    if (len <= 0) {
                        break;
                    }
                    out.write(buf, 0, len);

                }
            }
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                System.out.println("err"); //$NON-NLS-1$
            }
        }

        channel.disconnect();
        session.disconnect();

    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
        }
        return b;
    }

}
