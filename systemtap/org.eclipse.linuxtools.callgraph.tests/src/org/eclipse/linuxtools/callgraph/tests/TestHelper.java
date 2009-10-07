package org.eclipse.linuxtools.callgraph.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.callgraph.core.SystemTapView;
import org.eclipse.linuxtools.callgraph.core.ViewFactory;
import org.eclipse.ui.IViewPart;

public class TestHelper {

	public static SystemTapView makeView(String viewID){
		SystemTapView cView = null;
//		IExtensionRegistry reg = Platform.getExtensionRegistry();
//		IConfigurationElement[] extensions = reg.getConfigurationElementsFor(
//				PluginConstants.VIEW_RESOURCE, PluginConstants.VIEW_NAME,
//				viewID);
//
//		if (extensions == null || extensions.length < 1) {
//			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
//					"Couldn't load view", "Could not load view",
//					"Could not load view with id: " + viewID); //$NON-NLS-1$
//			mess.schedule();
//			return null;
//		}
//
//		IConfigurationElement element = null;
//		for (IConfigurationElement el : extensions) {
//			System.out.println(el.getName());
//			if (el.getName().equals("view")) {
//				element = el;
//				break;
//			}
//		}		

		try {
//			SystemTapView view;
//
//			Object o = element.createExecutableExtension(PluginConstants.ATTR_CLASS);
//			if (! (o instanceof SystemTapView)){
//				throw new Exception("The Returned Object was not of type SystemTapView");
//			}
//			
//			view = (SystemTapView)o;				
//			view.forceCreate();
			IViewPart vp =  ViewFactory.createView(viewID);
			 
			if (! (vp instanceof SystemTapView))
				throw new Exception("createView did not create a stapview.");
			cView = (SystemTapView) vp;
//			cView.initialize(null, new NullProgressMonitor());

		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cView;

	}
}
