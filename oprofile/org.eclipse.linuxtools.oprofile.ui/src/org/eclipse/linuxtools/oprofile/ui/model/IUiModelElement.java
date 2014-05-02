/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.ui.model;

import org.eclipse.swt.graphics.Image;

/**
 * Interface for all model elements to ease use with the tree viewer.
 * @since 1.1
 */
public interface IUiModelElement {
    /**
     * Returns the text to display in the tree viewer as required by the label provider.
     * @return text describing this element
     */
    String getLabelText();

    /**
     * Returns the children of this element.
     * @return an array of IUiModelElements
     */
    IUiModelElement[] getChildren();

    /**
     * Returns if this element has any children. Unless there is no data in
     * a session, only samples should not have any children.
     * @return true if children, false if no children
     */
    boolean hasChildren();

    /**
     * Returns the parent element.
     * @return the parent element
     */
    IUiModelElement getParent();

    /**
     * Returns the Image to display next to the text in the tree viewer.
     * @return an Image object of the icon
     */
    Image getLabelImage();
}
