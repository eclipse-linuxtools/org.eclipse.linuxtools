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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
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
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.menus.IMenuService;

/**
 * This page will allow the user to view/edit some of the repo
 * xml metadata (i.e., repo, revision, etc.).
 */
public class MetadataPage extends FormPage {

	private FormToolkit toolkit;
	private ScrolledForm form;

	private Text revisionTxt;
	private Tree tagsTree;
	private Composite buttonList;

	private static final String MENU_URI = "toolbar:formsToolbar"; 	//$NON-NLS-1$
	private static final String HEADER_ICON = "/icons/repository_rep.gif"; //$NON-NLS-1$

	/** Default constructor. */
	public MetadataPage(FormEditor editor) {
		super(editor, Messages.MetadataPage_title, Messages.MetadataPage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		// setting up the form page
		super.createFormContent(managedForm);
		GridLayout layout = new GridLayout();
		GridData data = new GridData();
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
		data = new GridData();
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

		// TODO: create custom tree to handle tags in specific categories (distro, content, repo)
		tagsTree = toolkit.createTree(sectionClientTags, SWT.BORDER | SWT.MULTI | SWT.HORIZONTAL
				| SWT.VERTICAL | SWT.LEFT_TO_RIGHT | SWT.SMOOTH);
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

		managedForm.refresh();
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
	private Button createPushButton(Composite parent, String buttonText, FormToolkit toolkit) {
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
	protected Text createTextFieldWithLabel(Composite parent, String labelName) {
		GridData layoutData = new GridData();
		// create the label
		Label respositoryBaseURLLbl = new Label(parent, SWT.NONE);
		respositoryBaseURLLbl.setText(labelName);
		layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.BEGINNING;
		layoutData.verticalAlignment = GridData.CENTER;
		// create the text field
		Text textField = new Text(parent, SWT.SINGLE);
		layoutData = new GridData();
		layoutData.horizontalIndent = 50;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.CENTER;
		// achieve flat look (don't put SWT.BORDER)
		textField.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.paintBordersFor(parent);
		textField.setLayoutData(layoutData);
		return textField;
	}

	/**
	 * Handle the add button execution on the Metadata page.
	 */
	public class AddTagButtonListener extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) { }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {/* not implemented */}
	}

	/**
	 * Handle the edit button execution on the Metadata page.
	 */
	public class EditTagButtonListener extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) { }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {/* not implemented */}
	}

	/**
	 * Handle the remove button execution on the Metadata page.
	 */
	public class RemoveTagButtonListener extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) { }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {/* not implemented */}
	}

}
