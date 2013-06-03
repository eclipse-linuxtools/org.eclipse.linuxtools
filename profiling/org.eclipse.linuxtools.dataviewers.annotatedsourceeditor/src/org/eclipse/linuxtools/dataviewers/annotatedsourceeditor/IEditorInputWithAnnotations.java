/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import org.eclipse.ui.IEditorInput;

/**
 * 
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 * @since 5.0
 */
public interface IEditorInputWithAnnotations extends IEditorInput {

    IAnnotationProvider createAnnotationProvider();

}
