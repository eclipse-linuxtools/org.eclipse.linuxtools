package org.eclipse.linuxtools.internal.oprofile.remote.launch.configuration;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
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
	private Boolean hasPermissions = null;


	public OprofileRemoteEventConfigTab(){
	}

	@Override
	protected boolean hasPermissions(IProject project){
		try{
			if (this.hasPermissions == null){
				IOpcontrolProvider provider = OprofileCorePlugin.getDefault().getOpcontrolProvider();
				this.hasPermissions = provider.hasPermissions(project);
			}
		} catch(OpcontrolException e){
			return false;
		}
		return this.hasPermissions;

	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		// Force re-check of permissions every time the view is initialized
		this.hasPermissions = null;
		super.initializeFrom(config);
	}
}
