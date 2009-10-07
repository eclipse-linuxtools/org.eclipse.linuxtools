package org.eclipse.linuxtools.callgraph.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.callgraph.core.SystemTapView;

public class TestHelper {

	public static SystemTapView makeView(String viewID) {
		SystemTapView cView = null;
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg.getConfigurationElementsFor(
				PluginConstants.VIEW_RESOURCE, PluginConstants.VIEW_NAME,
				viewID);

		if (extensions == null || extensions.length < 1) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					"Couldn't load view", "Could not load view",
					"Could not load view with id: " + viewID); //$NON-NLS-1$
			mess.schedule();
			return null;
		}

		IConfigurationElement element = null;
		for (IConfigurationElement el : extensions) {
			System.out.println(el.getName());
			if (el.getName().equals("view")) {
				element = el;
				break;
			}
		}		

		try {
			SystemTapView view;

			view = (SystemTapView) element
					.createExecutableExtension(PluginConstants.ATTR_CLASS);
			view.forceCreate();
			 
			cView = view.getSingleInstance();
			if (cView == null)
				return null;
//			cView.initialize(null, new NullProgressMonitor());

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return cView;

	}
}
