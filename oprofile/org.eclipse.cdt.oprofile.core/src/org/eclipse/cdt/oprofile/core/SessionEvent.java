/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

/**
 * A class which represents the event collected in a given session.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SessionEvent {
	public String eventName;
	public SampleSession[] sessions;
	public SessionEvent(String name) {
		eventName = name;
	}
}
