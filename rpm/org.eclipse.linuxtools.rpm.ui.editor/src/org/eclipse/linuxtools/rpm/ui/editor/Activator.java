package org.eclipse.linuxtools.rpm.ui.editor;

import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.rpm.ui.editor";

	// The shared instance
	private static Activator plugin;
	
	private ContributionTemplateStore fTemplateStore;
	private ContributionContextTypeRegistry fContextTypeRegistry;
	
	// RPM macros list
	private RpmMacroProposalsList macrosList ;
	
	// RPM package list
	private RpmPackageProposalsList packagesList ;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			fTemplateStore= new ContributionTemplateStore(getContextTypeRegistry(), getPreferenceStore(), "templates");
			try {
				fTemplateStore.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return fTemplateStore;
	}

	public RpmMacroProposalsList getRpmMacroList() {
		macrosList = new RpmMacroProposalsList();
		return macrosList;
	}
	
	public RpmPackageProposalsList getRpmPackageList() {
		if (packagesList == null){
			packagesList = new RpmPackageProposalsList();
		} else if (packagesList.getProposals("").size() == 0) {
			packagesList = new RpmPackageProposalsList();
		}
		return packagesList;
	}
	

	public ContextTypeRegistry getContextTypeRegistry() {
		if (fContextTypeRegistry == null) {
			fContextTypeRegistry = new ContributionContextTypeRegistry();
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.preambleSection");
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.preSection");
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.buildSection");
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.installSection");
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.changelogSection");			
		}
		return fContextTypeRegistry;
	}
	
}
