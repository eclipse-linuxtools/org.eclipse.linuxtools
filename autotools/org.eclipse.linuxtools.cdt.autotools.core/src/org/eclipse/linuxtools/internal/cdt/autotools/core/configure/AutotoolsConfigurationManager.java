/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.core.configure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.linuxtools.internal.cdt.autotools.core.configure.AutotoolsConfiguration.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AutotoolsConfigurationManager implements IResourceChangeListener {
	
	public final static String CFG_FILE_NAME = ".autotools"; //$NON-NLS-1$
	private final static String CFG_ALREADY_EXISTS = "Configure.Error.AlreadyExists"; //$NON-NLS-1$
	private final static String CFG_CANT_SAVE = "Configure.Error.NoProjectToSave"; //$NON-NLS-1$
	
	
	private static AutotoolsConfigurationManager instance;
	
	private static Map<String, ArrayList<IAConfiguration>> configs;
	
	private AutotoolsConfigurationManager() {
		configs = new HashMap<String, ArrayList<IAConfiguration>>();
		AutotoolsPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public static AutotoolsConfigurationManager getInstance() {
		if (instance == null) {
			instance = new AutotoolsConfigurationManager();
		}
		return instance;
	}

	public IAConfiguration createDefaultConfiguration(IProject project, String name) {
		IAConfiguration cfg = new AutotoolsConfiguration(name);
		return cfg;
	}
	
	private IAConfiguration findCfg(IProject p, String name) {
		ArrayList<IAConfiguration> cfgs = getConfigurations(p);
		for (int i = 0; i < cfgs.size(); ++i) {
			IAConfiguration cfg = cfgs.get(i);
			if (cfg.getName().equals(name))
				return cfg;
		}
		return null;
	}

	public IAConfiguration getConfiguration(IProject p, String cfgName) {
		return getConfiguration(p, cfgName, true);
	}

	public IAConfiguration getConfiguration(IProject p, String cfgName, boolean persist) {
		IAConfiguration cfg = findCfg(p, cfgName);
		if (cfg == null) {
			cfg = createDefaultConfiguration(p, cfgName);
			if (persist) {
				try {
					addConfiguration(p, cfg);
				} catch (CoreException e) {
					// Should never get here
					AutotoolsPlugin.log(e);
				}
			}
		} else {
			if (!persist)
				cfg = cfg.copy(cfg.getName());
		}
		return cfg;
	}
	
	
	private boolean configurationAlreadyExists(ArrayList<IAConfiguration> cfgs,
			IAConfiguration cfg) {
		String cfgName = cfg.getName();
		for (Iterator<IAConfiguration> i = cfgs.iterator(); i.hasNext(); ) {
			IAConfiguration x = i.next();
			if (x.getName().equals(cfgName))
				return true;
		}
		return false;
	}
	
	public void addConfiguration(IProject project, IAConfiguration cfg) throws CoreException {
		String projectName = project.getName();
		ArrayList<IAConfiguration> cfgs = getConfigs(project);
		if (cfgs == null) {
			cfgs = new ArrayList<IAConfiguration>();
			cfgs.add(cfg);
			configs.put(projectName, cfgs);
			saveConfigs(projectName);
		} else if (!configurationAlreadyExists(cfgs, cfg)) {
			cfgs.add(cfg);
			saveConfigs(projectName);
		} else {
			String errMsg = ConfigureMessages.getFormattedString(CFG_ALREADY_EXISTS, new String[]{cfg.getName()});
			throw new CoreException(new Status(IStatus.ERROR, AutotoolsPlugin.PLUGIN_ID, errMsg));
		}
	}
	
	public void replaceConfiguration(IProject project, IAConfiguration cfg) {
		String projectName = project.getName();
		ArrayList<IAConfiguration> cfgs = getConfigs(project);
		if (cfgs == null) {
			cfgs = new ArrayList<IAConfiguration>();
			cfgs.add(cfg);
			configs.put(projectName, cfgs);
			saveConfigs(projectName);
		} else {
			String cfgName = cfg.getName();
			boolean found = false;
			for (int i = 0; i < cfgs.size(); ++i) {
				IAConfiguration x = cfgs.get(i);
				if (x.getName().equals(cfgName)) {
				   cfgs.set(i, cfg);
				   found = true;
				   break;
				}
			}
			if (!found)
				cfgs.add(cfg);
			saveConfigs(projectName);
		}
	}

	public void replaceProjectConfigurations(IProject project, ArrayList<IAConfiguration> cfgs) {
		String projectName = project.getName();
		configs.put(projectName, cfgs);
		saveConfigs(projectName);
	}

	private ArrayList<IAConfiguration> getConfigs(IProject project) {
		String projectName = project.getName();
		ArrayList<IAConfiguration> list = configs.get(projectName);
		if (list == null) {
			try {
				IPath fileLocation = project.getLocation().append(CFG_FILE_NAME);
				File dirFile = fileLocation.toFile();
				ArrayList<IAConfiguration> cfgList = new ArrayList<IAConfiguration>();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				if (dirFile.exists()) {
					Document d = db.parse(dirFile);
					Element e = d.getDocumentElement();
					Set<String> nameSet = new HashSet<String>();
					// Figure out the name of the active configuration.
					NodeList cfgs = e.getElementsByTagName("configuration"); // $NON-NLS-1$
					for (int x = 0; x < cfgs.getLength(); ++x) {
						Node n = cfgs.item(x);
						NamedNodeMap attrs = n.getAttributes();
						Node name = attrs.getNamedItem("name"); // $NON-NLS-1$
						if (name != null && !nameSet.contains(name)) {
							String cfgName = name.getNodeValue();
							IAConfiguration cfg = new AutotoolsConfiguration(cfgName);
							NodeList l = n.getChildNodes();
							for (int y = 0; y < l.getLength(); ++y) {
								Node child = l.item(y);
								if (child.getNodeName().equals("option")) { // $NON-NLS-1$
									NamedNodeMap optionAttrs = child.getAttributes();
									Node id = optionAttrs.getNamedItem("id"); // $NON-NLS-1$
									Node value = optionAttrs.getNamedItem("value"); // $NON-NLS-1$
									if (id != null && value != null)
										cfg.setOption(id.getNodeValue(), value.getNodeValue());
								}
							}
							cfg.setDirty(false);
							cfgList.add(cfg);
						}
					}
					if (cfgList.size() > 0) {
						configs.put(projectName, cfgList);
						list = cfgList;
					}
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	public void saveConfigs(String projectName) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource res = (IProject)root.findMember(projectName, false);
			if (res == null || res.getType() != IResource.PROJECT) {
				AutotoolsPlugin.logErrorMessage(ConfigureMessages.getFormattedString(CFG_CANT_SAVE,
						new String[]{projectName}));
				return;
			}
			IProject project = (IProject)res;
			IPath output = project.getLocation().append(CFG_FILE_NAME);
			File f = output.toFile();
			if (!f.exists())
				f.createNewFile();
			if (f.exists()) {
				PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(f)));
				ArrayList<IAConfiguration> cfgs = configs.get(projectName);
				p.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
				p.println("<configurations>"); // $NON-NLS-1$
				Set<String> names = new HashSet<String>();
				Option[] optionList = AutotoolsConfiguration.getOptionList();
				for (int i = 0; i < cfgs.size(); ++i) {
					IAConfiguration cfg = cfgs.get(i);
					if (!names.contains(cfg.getName())) {
						p.println("<configuration name=\"" + cfg.getName() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ 
						for (int j = 0; j < optionList.length; ++j) {
							Option option = optionList[j];
							IConfigureOption opt = cfg.getOption(option.getName());
							if (!opt.isCategory())
								p.println("<option id=\"" + option.getName() + "\" value=\"" + opt.getValue() + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ // $NON-NLS-3$
						}
						p.println("</configuration>"); //$NON-NLS-1$
					} else {
						System.out.println("extra " + cfg.getName());
					}
				}
				p.println("</configurations>");
				p.close();
			}
		} catch (IOException e) {
			AutotoolsPlugin.log(e);
		}
	}
	
	public void saveAllConfigs() {
		Set<String> projectNames = configs.keySet();
		for (Iterator<String> i = projectNames.iterator(); i.hasNext();) {
			String projectName = i.next();
			saveConfigs(projectName);
		}
	}
	
	public ArrayList<IAConfiguration> getConfigurations(IProject project) {
		ArrayList<IAConfiguration> list = getConfigs(project);
		if (list == null) {
			list = new ArrayList<IAConfiguration>();
			configs.put(project.getName(), list);
		}
		return list;
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		IResource res = event.getResource();
		if (!(res instanceof IProject))
			return;
		String name = res.getName();
		IResourceDelta delta = event.getDelta();
		int kind = delta.getKind();
		if (configs.containsKey(name)) {
			if (kind == IResourceDelta.REMOVED) {
				configs.remove(name);
			} else if (kind == IResourceDelta.CHANGED) {
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.MOVED_TO) != 0) {
					IPath path = delta.getMovedToPath();
					ArrayList<IAConfiguration> cfgs = configs.get(name);
					String newName = path.lastSegment();
					configs.remove(name);
					configs.put(newName, cfgs);
				}
			}
		}
	}
	
}
