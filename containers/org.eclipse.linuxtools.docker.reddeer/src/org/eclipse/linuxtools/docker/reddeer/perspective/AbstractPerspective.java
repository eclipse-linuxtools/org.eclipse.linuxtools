/******************************************************************************* 
 * Copyright (c) 2016, 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.linuxtools.docker.reddeer.perspective;

import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;

/**
 * Abstract parent for each Perspective implementation
 * 
 * @author vlado pakan
 * 
 */
public abstract class AbstractPerspective extends org.eclipse.reddeer.eclipse.ui.perspectives.AbstractPerspective {

	/**
	 * Constructs the perspective with a given label.
	 * 
	 * @param perspectiveLabel Perspective label
	 */
	public AbstractPerspective(String perspectiveLabel) {
		super(perspectiveLabel);
	}

	/**
	 * Opens the perspective.
	 */
	@Override
	public void open() {
		log.info("Open perspective: '" + getPerspectiveLabel() + "'");
		if (isOpened()){
			log.debug("Perspective '" + getPerspectiveLabel() + "' is already opened.");
		}
		else{
			log.debug("Tryyying to open perspective: '" + getPerspectiveLabel() + "'");
			new DefaultToolItem(new DefaultShell(),"Open Perspective").click();
			new DefaultShell("Open Perspective");
			DefaultTable table = new DefaultTable();
			try{
				// Try to select perspective label within available perspectives
				table.select(getPerspectiveLabel());
			} catch (CoreLayerException swtLayerException){
				// Try to select perspective label within available perspectives with "(default)" suffix
				table.select(getPerspectiveLabel() + " (default)");
			}
			new PushButton("Open").click();
		}
	}

}
