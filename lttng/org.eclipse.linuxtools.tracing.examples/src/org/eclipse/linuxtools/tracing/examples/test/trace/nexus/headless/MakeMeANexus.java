/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tracing.examples.test.trace.nexus.headless;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Make a nexus file
 *
 * @author Matthew Khouzam
 */
public class MakeMeANexus {
  private static final byte[] intToByteArray(int value) {
    return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16),
        (byte) (value >>> 8), (byte) value};
  }

  /**
   * Main
   *
   * @param args
   *          arguments
   */
  public static void main(String[] args) {
    Random rnd = new Random();
    String[] eventTypes = new String[64];
    final String fileLoc = System.getProperty("user.home") + File.separator
        + "nexusTrace";
    try (FileOutputStream fos = new FileOutputStream(fileLoc)) {
      for (int i = 0; i < 64; i++) {
        eventTypes[i] = new String("Event " + i);
      }
      for (int i = 0; i < 64; i++) {
        fos.write(eventTypes[i].getBytes());
        if (i != 63) {
          fos.write(',');
        } else {
          fos.write('\n');
        }
      }
      int timestamp = 500 + rnd.nextInt(500);
      for (int i = 0; i < 400000; i++) {
        timestamp = timestamp + rnd.nextInt(50) + 1;
        int data = rnd.nextInt();
        fos.write(intToByteArray(timestamp));
        fos.write(intToByteArray(data));
        if (i % 10000 == 0 && i != 0) {
          System.out.print('.');
        }
      }
      System.out.print('\n');
      System.out.println("Trace written to : " + fileLoc);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
