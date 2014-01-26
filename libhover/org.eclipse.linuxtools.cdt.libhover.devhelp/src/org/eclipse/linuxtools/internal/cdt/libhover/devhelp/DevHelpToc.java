/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;

public class DevHelpToc implements IToc {

	@Override
	public String getLabel() {
		return "Devhelp Documents"; //$NON-NLS-1$
	}

	@Override
	public String getHref() {
		return null;
	}

	@Override
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	@Override
	public IUAElement[] getChildren() {
		return getTopics();
	}

	@Override
	public ITopic[] getTopics() {
		try {
			ArrayList<ITopic> topics = new ArrayList<>();
			IPreferenceStore ps = DevHelpPlugin.getDefault()
					.getPreferenceStore();
			IPath devhelpLocation = new Path(
					ps.getString(PreferenceConstants.DEVHELP_DIRECTORY));
			IFileSystem fs = EFS.getLocalFileSystem();
			IFileStore htmlDir = fs.getStore(devhelpLocation);
			IFileStore[] files = htmlDir.childStores(EFS.NONE, null);
			Arrays.sort(files, new Comparator<IFileStore>() {

				@Override
				public int compare(IFileStore arg0, IFileStore arg1) {
					return (arg0.getName().compareToIgnoreCase(arg1.getName()));
				}

			});
			for (IFileStore file: files) {
				String name = file.fetchInfo().getName();
				if (fs.getStore(
						devhelpLocation.append(name).append(name + ".devhelp2"))
						.fetchInfo().exists()) {
					ITopic topic = new DevHelpTopic(name);
					topics.add(topic);
				}
			}
			ITopic[] retval = new ITopic[topics.size()];
			return topics.toArray(retval);
		} catch (CoreException e) {
		}
		return null;
	}

	@Override
	public ITopic getTopic(String href) {
		// TODO Auto-generated method stub
		return null;
	}

}
