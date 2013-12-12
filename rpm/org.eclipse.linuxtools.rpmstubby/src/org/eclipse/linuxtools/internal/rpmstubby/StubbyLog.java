/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpmstubby;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * The logger of convenience for the Specfile Plug-In.
 */
public final class StubbyLog {

	private StubbyLog() {
		//don't allow instantiation
	}

   /**
    * Log the specified information.
    *
    * @param message A human-readable message, localized to the
    *           current locale.
    */
   public static void logInfo(String message) {
      log(IStatus.INFO, IStatus.OK, message, null);
   }

   /**
    * Log the specified error.
    *
    * @param exception A low-level exception.
    */
   public static void logError(Throwable exception) {
      logError("Unexpected Exception", exception);
   }

   /**
    * Log the specified error.
    *
    * @param message A human-readable message, localized to the
    *           current locale.
    * @param exception A low-level exception, or <code>null</code>
    *           if not applicable.
    */
   public static void logError(String message, Throwable exception) {
      log(IStatus.ERROR, IStatus.OK, message, exception);
   }

   /**
    * Log the specified information.
    *
    * @param severity The severity; one of the following:
    *           <code>IStatus.OK</code>,
    *           <code>IStatus.ERROR</code>,
    *           <code>IStatus.INFO</code>, or
    *           <code>IStatus.WARNING</code>.
    * @param code The plug-in-specific status code, or
    *           <code>OK</code>.
    * @param message A human-readable message, localized to the
    *           current locale.
    * @param exception A low-level exception, or <code>null</code>
    *           if not applicable.
    */
   public static void log(int severity, int code, String message,
         Throwable exception) {

      log(createStatus(severity, code, message, exception));
   }

   /**
    * Create a status object representing the specified information.
    *
    * @param severity The severity; one of the following:
    *           <code>IStatus.OK</code>,
    *           <code>IStatus.ERROR</code>,
    *           <code>IStatus.INFO</code>, or
    *           <code>IStatus.WARNING</code>.
    * @param code The plug-in-specific status code, or
    *           <code>OK</code>.
    * @param message A human-readable message, localized to the
    *           current locale.
    * @param exception A low-level exception, or <code>null</code>
    *           if not applicable.
    * @return the status object (not <code>null</code>).
    */
   public static IStatus createStatus(int severity, int code,
         String message, Throwable exception) {

      return new Status(severity, StubbyPlugin.PLUGIN_ID, code,
            message, exception);
   }

   /**
    * Log the given status.
    *
    * @param status The status to log.
    */
   public static void log(IStatus status) {
	   Platform.getLog(Platform.getBundle(StubbyPlugin.PLUGIN_ID)).log(status);
   }
}