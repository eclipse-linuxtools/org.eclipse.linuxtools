package org.eclipse.linuxtools.internal.oprofile.remote.launch.configuration;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileEventConfigTab;
import org.eclipse.swt.widgets.Button;


/**
 * Thic class represents the event configuration tab of the launcher dialog.
 * @since 1.1
 */
public class OprofileRemoteEventConfigTab extends OprofileEventConfigTab  {
	protected Button defaultEventCheck;
	protected OprofileCounter[] counters = null;
	protected CounterSubTab[] counterSubTabs;


	public OprofileRemoteEventConfigTab(){
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
	}
}
