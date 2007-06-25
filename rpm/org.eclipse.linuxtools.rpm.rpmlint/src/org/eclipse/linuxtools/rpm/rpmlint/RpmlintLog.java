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

package org.eclipse.linuxtools.rpm.rpmlint;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The logger of convenience for the Rpmlint Plug-In.
 */
public class RpmlintLog {
   /**
    * Log the specified information.
    * 
    * @param message, a human-readable message, localized to the
    *           current locale.
    */
   public static void logInfo(String message) {
      log(IStatus.INFO, IStatus.OK, message, null);
   }

   /**
    * Log the specified error.
    * 
    * @param exception, a low-level exception.
    */
   public static void logError(Throwable exception) {
      logError("Unexpected Exception", exception);
   }

   /**
    * Log the specified error.
    * 
    * @param message, a human-readable message, localized to the
    *           current locale.
    * @param exception, a low-level exception, or <code>null</code>
    *           if not applicable.
    */
   public static void logError(String message, Throwable exception) {
      log(IStatus.ERROR, IStatus.OK, message, exception);
   }

   /**
    * Log the specified information.
    * 
    * @param severity, the severity; one of the following:
    *           <code>IStatus.OK</code>,
    *           <code>IStatus.ERROR</code>,
    *           <code>IStatus.INFO</code>, or
    *           <code>IStatus.WARNING</code>.
    * @param pluginId. the unique identifier of the relevant
    *           plug-in.
    * @param code, the plug-in-specific status code, or
    *           <code>OK</code>.
    * @param message, a human-readable message, localized to the
    *           current locale.
    * @param exception, a low-level exception, or <code>null</code>
    *           if not applicable.
    */
   public static void log(int severity, int code, String message,
         Throwable exception) {

      log(createStatus(severity, code, message, exception));
   }

   /**
    * Create a status object representing the specified information.
    * 
    * @param severity, the severity; one of the following:
    *           <code>IStatus.OK</code>,
    *           <code>IStatus.ERROR</code>,
    *           <code>IStatus.INFO</code>, or
    *           <code>IStatus.WARNING</code>.
    * @param pluginId, the unique identifier of the relevant
    *           plug-in.
    * @param code, the plug-in-specific status code, or
    *           <code>OK</code>.
    * @param message, a human-readable message, localized to the
    *           current locale.
    * @param exception, a low-level exception, or <code>null</code>
    *           if not applicable.
    * @return, the status object (not <code>null</code>).
    */
   public static IStatus createStatus(int severity, int code,
         String message, Throwable exception) {

      return new Status(severity, Activator.PLUGIN_ID, code,
            message, exception);
   }

   /**
    * Log the given status.
    * 
    * @param status, the status to log.
    */
   public static void log(IStatus status) {
	   Activator.getDefault().getLog().log(status);
   }
}