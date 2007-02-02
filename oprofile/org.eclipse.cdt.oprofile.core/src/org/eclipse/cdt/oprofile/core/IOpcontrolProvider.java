/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core;

/**
 * Interface for oprofile core to utilize opcontrol program. Platform plugins should define/register
 * an OpcontrolProvider for the core to use.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public interface IOpcontrolProvider {
	
	/**
	 * Initialize the Oprofile kernel module
	 * @throws OpcontrolException
	 */
	public void initModule() throws OpcontrolException;
	
	/**
	 * De-initialize (unload) the kernel module
	 * @throws OpcontrolException
	 */
	public void deinitModule() throws OpcontrolException;
	
	/**
	 * Clears out data from the current session
	 * @throws OpcontrolException
	 */
	public void reset() throws OpcontrolException;
	
	/**
	 * Flush the current oprofiled sample buffers to disk
	 * @throws OpcontrolException
	 */
	public void dumpSamples() throws OpcontrolException;
	
	/**
	 * Setup oprofiled collection parameters
	 * @param options a list of command-line arguments for opcontrol
	 * @param events list of events to collect
	 * @throws OpcontrolException
	 */
	public void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException;
	
	/**
	 * Start data collection by oprofiled (will start oprofiled if necessary)
	 * @throws OpcontrolException
	 */
	public void startCollection() throws OpcontrolException;
	
	/**
	 * Stop data collection (does NOT stop daemon)
	 * @throws OpcontrolException
	 */
	public void stopCollection() throws OpcontrolException;
	
	/**
	 * Stop data collection and shutdown oprofiled
	 * @throws OpcontrolException
	 */
	public void shutdownDaemon() throws OpcontrolException;
	
	/**
	 * Start oprofiled (does NOT start data collection)
	 * @throws OpcontrolException
	 */
	public void startDaemon() throws OpcontrolException;
	
	/**
	 * Save the current session
	 * @throws OpcontrolException
	 */
	public void saveSession(String name) throws OpcontrolException;
}
