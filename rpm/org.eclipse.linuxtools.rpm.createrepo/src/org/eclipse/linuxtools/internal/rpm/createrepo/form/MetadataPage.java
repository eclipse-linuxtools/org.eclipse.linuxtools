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
package org.eclipse.linuxtools.internal.rpm.createrepo.form;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoCategoryModel;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeCategory;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeContentProvider;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.menus.IMenuService;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This page will allow the user to view/edit some of the repo
 * xml metadata (i.e., repo, revision, etc.).
 */
public class MetadataPage extends FormPage {

    private CreaterepoProject project;
    private IEclipsePreferences eclipsePreferences;

    private FormToolkit toolkit;
    private ScrolledForm form;

    private Text revisionTxt;
    private Text tagTxt;
    private Tree tagsTree;
    private TreeViewer tagsTreeViewer;
    private Composite buttonList;

    private static final String MENU_URI = "toolbar:formsToolbar";     //$NON-NLS-1$
    private static final String HEADER_ICON = "/icons/library_obj.gif"; //$NON-NLS-1$

    /** Default constructor. */
    public MetadataPage(FormEditor editor, CreaterepoProject project) {
        super(editor, Messages.MetadataPage_title, Messages.MetadataPage_title);
        this.project = project;
        eclipsePreferences = project.getEclipsePreferences();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        // setting up the form page
        super.createFormContent(managedForm);
        GridLayout layout = new GridLayout();
        toolkit = managedForm.getToolkit();
        form = managedForm.getForm();
        form.setText(Messages.MetadataPage_formHeaderText);
        form.setImage(Activator.getImageDescriptor(HEADER_ICON).createImage());
        ToolBarManager toolbarManager = (ToolBarManager) form.getToolBarManager();
        toolkit.decorateFormHeading(form.getForm());

        // add the menuContribution from MANIFEST.MF to the form
        IMenuService menuService = (IMenuService) getSite().getService(IMenuService.class);
        menuService.populateContributionManager(toolbarManager, MENU_URI);
        toolbarManager.update(true);

        layout = new GridLayout();
        layout.marginWidth = 6; layout.marginHeight = 12;
        form.getBody().setLayout(layout);

        //--------------------------------- REVISION SECTION START ----------
        // Section and its client area to manage updating revision info
        Section revSection = toolkit.createSection(form.getBody(), Section.DESCRIPTION
                | ExpandableComposite.TITLE_BAR);
        layout = new GridLayout();
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        revSection.setText(Messages.MetadataPage_sectionTitleRevision);
        revSection.setDescription(Messages.MetadataPage_sectionInstructionRevision);
        revSection.setLayoutData(data);

        // the client area containing the editing fields
        Composite sectionClient = toolkit.createComposite(revSection);
        layout = new GridLayout(2, false);
        layout.marginWidth = 1; layout.marginHeight = 7;
        sectionClient.setLayout(layout);

        revisionTxt = createTextFieldWithLabel(sectionClient, Messages.MetadataPage_labelRevision);
        String prefRevisionTxt = eclipsePreferences.get(CreaterepoPreferenceConstants.PREF_REVISION, ICreaterepoConstants.EMPTY_STRING);
        if (!prefRevisionTxt.isEmpty()) {
            revisionTxt.setText(prefRevisionTxt);
        }
        revisionTxt.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                String revisionText = revisionTxt.getText().trim();
                savePreferences(CreaterepoPreferenceConstants.PREF_REVISION, revisionText);
            }
        });
        revSection.setClient(sectionClient);
        //---------- REVISION SECTION END

        //--------------------------------- TAGS SECTION START ----------
        // Section and its client area to manage tags
        Section tagSection = toolkit.createSection(form.getBody(), Section.DESCRIPTION
                | ExpandableComposite.TITLE_BAR);
        layout = new GridLayout();
        tagSection.setText(Messages.MetadataPage_sectionTitleTags);
        tagSection.setDescription(Messages.MetadataPage_sectionInstructionTags);
        tagSection.setLayoutData(expandComposite());

        // the client area containing the tags
        Composite sectionClientTags = toolkit.createComposite(tagSection);
        layout = new GridLayout(2, false);
        layout.marginWidth = 1; layout.marginHeight = 7;
        sectionClientTags.setLayout(layout);

        tagTxt = createTextFieldWithLabel(sectionClientTags, Messages.MetadataPage_labelTags);
        tagTxt.addSelectionListener(new AddTagButtonListener());

        tagsTreeViewer = new TreeViewer(sectionClientTags, SWT.BORDER | SWT.SINGLE | SWT.HORIZONTAL
                | SWT.VERTICAL | SWT.LEFT_TO_RIGHT | SWT.SMOOTH);
        tagsTreeViewer.setContentProvider(new CreaterepoTreeContentProvider());
        tagsTreeViewer.setLabelProvider(new CreaterepoTreeLabelProvider());
        CreaterepoCategoryModel model = new CreaterepoCategoryModel(project);
        tagsTreeViewer.setInput(model);
        // change the tag text field on change (make editing tag easier)
        tagsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (tagsTree.getSelectionCount() == 1) {
                    TreeItem treeItem = tagsTree.getSelection()[0];
                    if (!(treeItem.getData() instanceof CreaterepoTreeCategory)) {
                        String tag = (String) treeItem.getData();
                        tagTxt.setText(tag);
                    } else {
                        tagTxt.setText(ICreaterepoConstants.EMPTY_STRING);
                    }
                }
            }
        });
        // expand or shrink a category
        tagsTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) tagsTreeViewer.getSelection();
                if (selection.getFirstElement() instanceof CreaterepoTreeCategory) {
                    CreaterepoTreeCategory category = (CreaterepoTreeCategory) selection.getFirstElement();
                    tagsTreeViewer.setExpandedState(category, !tagsTreeViewer.getExpandedState(category));
                }
            }
        });
        tagsTree = tagsTreeViewer.getTree();
        tagsTree.setLayoutData(expandComposite());

        // everything to do with the buttons
        buttonList = toolkit.createComposite(sectionClientTags);
        layout = new GridLayout();
        data = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
        layout.marginWidth = 0; layout.marginHeight = 0;
        buttonList.setLayout(layout);
        buttonList.setLayoutData(data);
        createPushButton(buttonList, Messages.MetadataPage_buttonAddTag,
                toolkit).addSelectionListener(new AddTagButtonListener());
        createPushButton(buttonList, Messages.MetadataPage_buttonEditTag,
                toolkit).addSelectionListener(new EditTagButtonListener());
        createPushButton(buttonList, Messages.MetadataPage_buttonRemoveTag,
                toolkit).addSelectionListener(new RemoveTagButtonListener());
        tagSection.setClient(sectionClientTags);
        //---------- TAGS SECTION END
        refreshTree();
        managedForm.refresh();
    }

    /**
     * Refresh the tree. This includes removing the expand button of a
     * category if there are no tags placed under it.
     */
    private void refreshTree() {
        // expand categories with no tags under them to remove expand button
        for (TreeItem treeItem : tagsTree.getItems()) {
            if (treeItem.getData() instanceof CreaterepoTreeCategory) {
                CreaterepoTreeCategory category = (CreaterepoTreeCategory) treeItem.getData();
                if (category.getTags().isEmpty()) {
                    tagsTreeViewer.expandToLevel(category, 1);
                    tagsTreeViewer.update(category, null);
                }
            }
        }
    }

    /**
     * Make a GridData that expands to fill both horizontally
     * and vertically.
     *
     * @return The created GridData.
     */
    private static GridData expandComposite() {
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        return data;
    }

    /**
     * Create a push style button.
     *
     * @param parent The parent the button will belong to.
     * @param buttonText The text show on the button.
     * @param toolkit The form toolkit used in creating a button.
     * @return The button created.
     */
    private static Button createPushButton(Composite parent, String buttonText, FormToolkit toolkit) {
        Button button = toolkit.createButton(parent, buttonText, SWT.PUSH | SWT.FLAT
                | SWT.CENTER | SWT.LEFT_TO_RIGHT);
        button.setFont(parent.getFont());
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        button.setLayoutData(gd);
        return button;
    }

    /**
     * Create a text field with a label.
     *
     * @param parent The parent of the text field and label.
     * @param labelName The name on the label.
     * @return The newly created text field.
     */
    private Text createTextFieldWithLabel(Composite parent, String labelName) {
        // set up the area in which the label and text will reside
        Composite areaLabelText = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData();
        GridLayout gridlayout = new GridLayout(2, false);
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.CENTER;
        layoutData.horizontalSpan = 2;
        layoutData.grabExcessHorizontalSpace = true;
        areaLabelText.setLayoutData(layoutData);
        areaLabelText.setLayout(gridlayout);
        // create the label
        Label respositoryBaseURLLbl = new Label(areaLabelText, SWT.NONE);
        respositoryBaseURLLbl.setText(labelName);
        layoutData = new GridData();
        layoutData.widthHint = 100;
        layoutData.horizontalAlignment = GridData.BEGINNING;
        layoutData.verticalAlignment = GridData.CENTER;
        respositoryBaseURLLbl.setLayoutData(layoutData);
        // create the text field
        Text textField = new Text(areaLabelText, SWT.SINGLE);
        layoutData = new GridData();
        layoutData.horizontalIndent = 50;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.CENTER;
        // achieve flat look (don't put SWT.BORDER)
        textField.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
        textField.setLayoutData(layoutData);
        toolkit.paintBordersFor(areaLabelText);
        return textField;
    }

    /**
     * Save the project preferences of some value to a key.
     *
     * @param key The preferences key.
     * @param val The value to save.
     */
    private void savePreferences(String key, String val) {
        eclipsePreferences.put(key, val);
        try {
            eclipsePreferences.flush();
            refreshTree();
        } catch (BackingStoreException e) {
            Activator.logError(Messages.MetadataPage_errorSavingPreferences, e);
        }
    }

    /**
     * Prepare the tags to be saved to the preference. This class gets all the tags
     * from a category and transforms it into a semicolon-delimited string.
     *
     * @param category The category to prepare the tag string for.
     * @return A semicolon-delimited string of tags taken from the category.
     */
    private static String preparePreferenceTag(CreaterepoTreeCategory category) {
        String preferenceToSave = ICreaterepoConstants.EMPTY_STRING;
        if (!category.getTags().isEmpty()) {
            for (String tag : category.getTags()){
                preferenceToSave = preferenceToSave.concat(tag+ICreaterepoConstants.DELIMITER);
            }
            // remove the hanging delimiter
            preferenceToSave = preferenceToSave.substring(0, preferenceToSave.length()-1);
        }
        return preferenceToSave;
    }

    /**
     * Method to add the tag from the tag text field to the category in the tree.
     * Used by the "Add" button and the default operation when ENTER is pressed while
     * in the tag text field.
     */
    private void addTag() {
        IStructuredSelection selection = (IStructuredSelection) tagsTreeViewer.getSelection();
        if (selection.getFirstElement() instanceof CreaterepoTreeCategory) {
            CreaterepoTreeCategory category = (CreaterepoTreeCategory) selection.getFirstElement();
            String text = tagTxt.getText().trim();
            if (!text.isEmpty()) {
                category.addTag(text);
                tagsTreeViewer.refresh(category, false);
                tagsTreeViewer.setExpandedState(category, true);
                tagTxt.setText(ICreaterepoConstants.EMPTY_STRING);
                String preferenceToSave = preparePreferenceTag(category);
                savePreferences(category.getName(), preferenceToSave);
            }
        }
    }

    /**
     * Handle the add button execution on the Metadata page.
     */
    private class AddTagButtonListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            addTag();
        }
        
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            addTag();
        }
    }

    /**
     * Handle the edit button execution on the Metadata page.
     */
    private class EditTagButtonListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (tagsTree.getSelectionCount() == 1) {
                TreeItem treeItem = tagsTree.getSelection()[0];
                String newTag = tagTxt.getText().trim();
                if (!(treeItem.getData() instanceof CreaterepoTreeCategory) && !newTag.isEmpty()) {
                    CreaterepoTreeCategory parent = (CreaterepoTreeCategory) treeItem.getParentItem().getData();
                    String oldTag = (String) treeItem.getData();
                    int oldTagIndex = parent.getTags().indexOf(oldTag);
                    if (parent.getTags().indexOf(newTag) == -1) {
                        parent.getTags().set(oldTagIndex, newTag);
                        tagsTreeViewer.refresh(parent, true);
                        tagsTree.setSelection(treeItem);
                        String preferenceToSave = preparePreferenceTag(parent);
                        savePreferences(parent.getName(), preferenceToSave);
                    }
                }
            }
        }
    }

    /**
     * Handle the remove button execution on the Metadata page.
     */
    private class RemoveTagButtonListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (tagsTree.getSelectionCount() == 1) {
                TreeItem treeItem = tagsTree.getSelection()[0];
                if (!(treeItem.getData() instanceof CreaterepoTreeCategory)) {
                    CreaterepoTreeCategory parent = (CreaterepoTreeCategory) treeItem.getParentItem().getData();
                    String tag = (String) treeItem.getData();
                    parent.removeTag(tag);
                    tagsTreeViewer.refresh(parent, true);
                    tagTxt.setText(ICreaterepoConstants.EMPTY_STRING);
                    String preferenceToSave = preparePreferenceTag(parent);
                    savePreferences(parent.getName(), preferenceToSave);
                }
            }
        }
    }

}
