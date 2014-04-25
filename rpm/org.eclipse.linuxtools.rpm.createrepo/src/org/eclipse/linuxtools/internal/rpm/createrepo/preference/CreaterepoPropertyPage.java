/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Abstract class for createrepo property pages.
 */
public abstract class CreaterepoPropertyPage extends PropertyPage implements IPropertyChangeListener {

    protected static final String CREATEREPO_PREFERENCE_ID = "org.eclipse.linuxtools.rpm.createrepo.preferences"; //$NON-NLS-1$

    protected IProject project;
    protected IPreferenceStore preferenceStore;

    /**
     * Default constructor has no description.
     */
    public CreaterepoPropertyPage() {
        this(null);
    }

    /**
     * Constructor sets the description.
     *
     * @param description The description of the preference page.
     */
    public CreaterepoPropertyPage(String description) {
        setDescription(description);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        if (getElement() instanceof IResource) {
            project = ((IResource) getElement()).getProject();
        } else {
            Object adapter = getElement().getAdapter(IResource.class);
            if (adapter instanceof IResource) {
                project = ((IResource) adapter).getProject();
            }
        }
        setPreferenceStore(new ScopedPreferenceStore(new ProjectScope(project),
                Activator.PLUGIN_ID));
        preferenceStore = getPreferenceStore();
        return addContents(parent);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }
    }

    /**
     * Must be implemented by the subclasses. These are the contents of the
     * property page. Subclasses should implement this rather than createContents
     * to add controls to the property page.
     *
     * @param parent The composite.
     * @return The newly customized composite.
     */
    protected abstract Composite addContents(Composite parent);

    /**
     * This method will be called whenever a field editor is changed. Note that
     * the field editor must first set the property change listener to this.
     */
    protected abstract void checkState();

    /**
     * Make sure there is space above the group as well as space
     * between the contents of the group and its border.
     *
     * @param group The group to update the spacing of.
     */
    protected static void updateGroupSpacing(Group group) {
        GridLayout layout = (GridLayout) group.getLayout();
        GridData data = (GridData) group.getLayoutData();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        data.verticalIndent = 20;
    }

}
