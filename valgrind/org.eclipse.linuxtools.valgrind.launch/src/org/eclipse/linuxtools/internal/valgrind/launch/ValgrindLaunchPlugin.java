/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Patrick Hofer (Noser Engineering AG) - fix for Bug 275685
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class ValgrindLaunchPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.launch"; //$NON-NLS-1$
    public static final String LAUNCH_ID = PLUGIN_ID + ".valgrindLaunch"; //$NON-NLS-1$
    public static final String MARKER_TYPE = PLUGIN_ID + ".marker"; //$NON-NLS-1$
    public static final String OUTPUT_DIR_EXT_ID = "outputDirectoryProviders"; //$NON-NLS-1$

    // Extension point constants
    public static final String TOOL_EXT_ID = "valgrindTools"; //$NON-NLS-1$
    public static final String TOOL_EXT_DEFAULT = PLUGIN_ID + ".memcheck"; //$NON-NLS-1$
    protected static final String EXT_ELEMENT_TOOL = "tool"; //$NON-NLS-1$
    protected static final String EXT_ATTR_NAME = "name"; //$NON-NLS-1$
    protected static final String EXT_ATTR_ID = "id"; //$NON-NLS-1$
    protected static final String EXT_ATTR_PAGE = "page"; //$NON-NLS-1$
    protected static final String EXT_ATTR_DELEGATE = "delegate"; //$NON-NLS-1$

    protected static final String EXT_ELEMENT_PROVIDER = "provider"; //$NON-NLS-1$
    protected static final String EXT_ATTR_CLASS = "class"; //$NON-NLS-1$

    public static final Version VER_3_3_0 = new Version(3, 3, 0);
    public static final Version VER_3_4_0 = new Version(3, 4, 0);
    public static final Version VER_3_6_0 = new Version(3, 6, 0);

    private static final Version MIN_VER = VER_3_3_0;
    private static final String VERSION_PREFIX = "valgrind-"; //$NON-NLS-1$
    private static final char VERSION_DELIMITER = '-';

    protected HashMap<String, IConfigurationElement> toolMap;

    private ValgrindCommand valgrindCommand;
    private ILaunchConfiguration config;
    private ILaunch launch;

    // The shared instance
    private static ValgrindLaunchPlugin plugin;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static ValgrindLaunchPlugin getDefault() {
        return plugin;
    }

    /**
     * @since 0.10
     */
    public Version getValgrindVersion(IProject project) throws CoreException {
        Version valgrindVersion;
        try {
            String verString = getValgrindCommand().whichVersion(project);
            verString = verString.replace(VERSION_PREFIX, ""); //$NON-NLS-1$
            if (verString.indexOf(VERSION_DELIMITER) > 0) {
                verString = verString.substring(0, verString.indexOf(VERSION_DELIMITER));
            }
            if (!verString.isEmpty()) {
                valgrindVersion = Version.parseVersion(verString);
            } else {
                throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Couldn't_determine_version"))); //$NON-NLS-1$
            }
        } catch (IOException e) {
            IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Couldn't_determine_version"), e); //$NON-NLS-1$
            throw new CoreException(status);
        }

        // check for minimum supported version
        if (valgrindVersion.compareTo(MIN_VER) < 0) {
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, NLS.bind(Messages.getString("ValgrindLaunchPlugin.Error_min_version"), valgrindVersion.toString(), MIN_VER.toString()))); //$NON-NLS-1$
        }
        return valgrindVersion;
    }

    public void setValgrindCommand(ValgrindCommand command) {
        valgrindCommand = command;
    }

    /**
     * @since 0.10
     */
    protected ValgrindCommand getValgrindCommand() {
        if (valgrindCommand == null) {
            valgrindCommand = new ValgrindCommand();
        }
        return valgrindCommand;
    }

    public String[] getRegisteredToolIDs() {
        Set<String> ids = getToolMap().keySet();
        return ids.toArray(new String[ids.size()]);
    }

    public String getToolName(String id) {
        String name = null;
        IConfigurationElement config = getToolMap().get(id);
        if (config != null) {
            name = config.getAttribute(EXT_ATTR_NAME);
        }
        return name;
    }

    public IValgrindToolPage getToolPage(String id) throws CoreException {
        IValgrindToolPage tab = null;
        IConfigurationElement config = getToolMap().get(id);
        if (config != null) {
            Object obj = config.createExecutableExtension(EXT_ATTR_PAGE);
            if (obj instanceof IValgrindToolPage) {
                tab = (IValgrindToolPage) obj;
            }
        }
        if (tab == null) {
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Cannot_retrieve_page"))); //$NON-NLS-1$
        }
        return tab;
    }

    public IValgrindLaunchDelegate getToolDelegate(String id) throws CoreException {
        IValgrindLaunchDelegate delegate = null;
        IConfigurationElement config = getToolMap().get(id);
        if (config != null) {
            Object obj = config.createExecutableExtension(EXT_ATTR_DELEGATE);
            if (obj instanceof IValgrindLaunchDelegate) {
                delegate = (IValgrindLaunchDelegate) obj;
            }
        }
        if (delegate == null) {
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Cannot_retrieve_delegate"))); //$NON-NLS-1$
        }
        return delegate;
    }

    public IValgrindOutputDirectoryProvider getOutputDirectoryProvider() throws CoreException {
        IValgrindOutputDirectoryProvider provider = null;
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, OUTPUT_DIR_EXT_ID);

        // if we find more than one provider just take the first one
        IConfigurationElement[] configs = extPoint.getConfigurationElements();
        for (int i = 0; i < configs.length && provider == null; i++) {
            IConfigurationElement config = configs[i];
            if (config.getName().equals(EXT_ELEMENT_PROVIDER)) {
                Object obj = config.createExecutableExtension(EXT_ATTR_CLASS);
                if (obj instanceof IValgrindOutputDirectoryProvider) {
                    provider = (IValgrindOutputDirectoryProvider) obj;
                }
            }
        }

        // if no extender, use default
        if (provider == null) {
            provider = new ValgrindOutputDirectoryProvider();
        }

        return provider;
    }

    public void setCurrentLaunchConfiguration(ILaunchConfiguration config) {
        this.config = config;
    }

    /**
     * @return ILaunchConfiguration associated with Valgrind execution
     * currently displayed in the Valgrind view.
     */
    public ILaunchConfiguration getCurrentLaunchConfiguration() {
        return config;
    }

    public void setCurrentLaunch(ILaunch launch) {
        this.launch = launch;
    }

    /**
     * @return ILaunch associated with Valgrind execution currently displayed
     * in the Valgrind view.
     */
    public ILaunch getCurrentLaunch() {
        return launch;
    }

    IPath parseWSPath(String strpath) throws CoreException {
        strpath = LaunchUtils.getStringVariableManager().performStringSubstitution(strpath, false);
        IPath path = new Path(strpath);
        if (!path.isAbsolute()) {
            IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
            if (res != null) {
                path = res.getLocation();
            }
        }
        return path;
    }

    private void initializeToolMap() {
        toolMap = new HashMap<>();
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, ValgrindLaunchPlugin.TOOL_EXT_ID);
        IConfigurationElement[] configs = extPoint.getConfigurationElements();
        for (IConfigurationElement config : configs) {
            if (config.getName().equals(EXT_ELEMENT_TOOL)) {
                String id = config.getAttribute(EXT_ATTR_ID);
                if (id != null && config.getAttribute(EXT_ATTR_NAME) != null
                        && config.getAttribute(EXT_ATTR_PAGE) != null
                        && config.getAttribute(EXT_ATTR_DELEGATE) != null) {
                    toolMap.put(id, config);
                }
            }
        }
    }

    private HashMap<String, IConfigurationElement> getToolMap() {
        if (toolMap == null) {
            initializeToolMap();
        }
        return toolMap;
    }
}
