/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core;

import java.util.ArrayList;

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Interface for the core to utilize opxml. Platform plugins should define/register an
 * OpxmlProvider for the core to use.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public interface IOpxmlProvider {
	
	/**
	 * Returns an <code>IRunnableWithProgress</code> that fetches generic information from opxml
	 * @param info <code>OpInfo</code> object for results
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	public IRunnableWithProgress info(OpInfo info);
	
	/**
	 * Returns an <code>IRunnableWithProgress</code> that fetches samples for the
	 * given <code>SampleSession</code>
	 * @param session the session for which to fetch samples
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	public IRunnableWithProgress samples(SampleSession session);
	
	/**
	 * Returns an <code>IRunnableWithProgress</code> that fetches the debug info for the
	 * given <code>ProfileImage</code>
	 * @param image the <code>ProfileImage</code> for which debug info is desired
	 * @param infoList the <code>ArrayList</code> in which to return the debug info
	 * @return <code>IRunnableWithProgressv that may be run by the caller
	 */
	public IRunnableWithProgress debugInfo(ProfileImage image, ArrayList infoList);
	
	/**
	 * Returns an <code>IRunnableWithProgress</code> that checks the validity of the given
	 * event, unit mask, and counter combination 
	 * @param ctr the counter
	 * @param event the integer event number
	 * @param um the integer unit mask
	 * @param eventValid a size one array to hold the return result (see <code>CheckEventsProcessor</code>)
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	public IRunnableWithProgress checkEvents(int ctr, int event, int um, int[] eventValid);
	
	/**
	 * Returns an /code>IRunnableWithProgress</code> that fetches the list of sessions
	 * @param info the <code>OpInfo</code> for oprofile
	 * @param sessionList an <code>ArrayList</code> in which to return the list of sessions
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	public IRunnableWithProgress sessions(OpInfo info, ArrayList sessionList);
}
