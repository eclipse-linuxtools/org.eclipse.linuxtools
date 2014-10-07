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
package org.eclipse.linuxtools.internal.rpm.createrepo.wizard;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This page allows the user to initialize the .repo file
 * for the repository with the mandatory options: id, name, and
 * base url.
 */
public class CreaterepoNewWizardPageTwo extends WizardPage {

    private Text repositoryIDTxt;
    private Text repositoryNameTxt;
    private Text repositoryBaseURLTxt;

    /**
     * Constructor for CreaterepoWizardPage. Will set the page name, title, and
     * description.
     *
     * @param pageName The wizard page's name.
     */
    public CreaterepoNewWizardPageTwo(String pageName) {
        super(pageName);
        setTitle(Messages.CreaterepoNewWizardPageTwo_wizardPageTitle);
        setDescription(Messages.CreaterepoNewWizardPageTwo_wizardPageDescription);
        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        container.setLayout(layout);
        container.setLayoutData(layoutData);

        // composite to hold the required information
        Composite information = new Composite(container, SWT.NONE);
        layout = new GridLayout(2, false);
        layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.CENTER;
        information.setLayoutData(layoutData);
        information.setLayout(layout);

        // listen on modifying the Text widgets
        ModifyListener modifyListner = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete(isValid());
            }
        };
        repositoryIDTxt = createTextFieldWithLabel(information, Messages.CreaterepoNewWizardPageTwo_labelID,
                Messages.CreaterepoNewWizardPageTwo_tooltipID);
        repositoryIDTxt.addModifyListener(modifyListner);
        repositoryNameTxt = createTextFieldWithLabel(information, Messages.CreaterepoNewWizardPageTwo_labelName,
                Messages.CreaterepoNewWizardPageTwo_tooltipName);
        repositoryNameTxt.addModifyListener(modifyListner);
        repositoryBaseURLTxt = createTextFieldWithLabel(information, Messages.CreaterepoNewWizardPageTwo_labelURL,
                Messages.CreaterepoNewWizardPageTwo_tooltipURL);
        repositoryBaseURLTxt.addModifyListener(modifyListner);
        setControl(container);
    }

    /**
     * Create a text field with a label.
     *
     * @param parent The parent of the text field and label.
     * @param labelName The name on the label.
     * @return The newly created text field.
     */
    protected Text createTextFieldWithLabel(Composite parent, String labelName, String tooltip) {
        // create the label
        Label respositoryBaseURLLbl = new Label(parent, SWT.NONE);
        respositoryBaseURLLbl.setText(labelName);
        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = GridData.BEGINNING;
        layoutData.verticalAlignment = GridData.CENTER;
        respositoryBaseURLLbl.setToolTipText(tooltip);
        // create the text field
        Text textField = new Text(parent, SWT.BORDER | SWT.SINGLE);
        layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.CENTER;
        textField.setLayoutData(layoutData);
        textField.setToolTipText(tooltip);
        return textField;
    }

    /**
     * Check to see if all the text fields are valid. Otherwise, set error
     * messages appropriately.
     *
     * @return True if all the text fields are valid, false otherwise.
     */
    protected boolean isValid() {
        if (!validateID()) {
            setErrorMessage(Messages.CreaterepoNewWizardPageTwo_errorID);
        }
        if (repositoryNameTxt.getText().trim().isEmpty()) {
            setErrorMessage(Messages.CreaterepoNewWizardPageTwo_errorName);
        }
        if (!validateURL()) {
            setErrorMessage(Messages.CreaterepoNewWizardPageTwo_errorURL);
        }
        if (validateID() && validateURL() && !repositoryNameTxt.getText().trim().isEmpty()) {
            setErrorMessage(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the ID is valid. It is valid if it is a single string and
     * not empty.
     *
     * @return True if the ID is valid, false otherwise.
     */
    private boolean validateID() {
        // check if repository ID is a single string
        String tmpRepoID = repositoryIDTxt.getText().trim();
        Pattern singleStringPattern = Pattern.compile("\\b(\\S+)\\b", //$NON-NLS-1$
                Pattern.CASE_INSENSITIVE);
        Matcher singleStringMatcher = singleStringPattern.matcher(tmpRepoID);
        if (singleStringMatcher.matches()) {
            return true;
        }
        return false;
    }

    /**
     * Check if the URL is valid. It is valid if it contains a valid protocol and link.
     *
     * @return True if the URL is valid, false otherwise.
     */
    private boolean validateURL() {
        // check if baseURL is a valid URL
        String tmpRepoURL = repositoryBaseURLTxt.getText().trim();
        // TODO: possibly validate if pointing to something that exists (not really necessary, but nice)
        try {
            new URL(tmpRepoURL);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public String getRepositoryID() {
        if (repositoryIDTxt == null) {
            return ICreaterepoConstants.EMPTY_STRING;
        }
        return repositoryIDTxt.getText().trim();
    }

    public String getRepositoryName() {
        if (repositoryNameTxt == null) {
            return ICreaterepoConstants.EMPTY_STRING;
        }
        return repositoryNameTxt.getText().trim();
    }

    public String getRepositoryURL() {
        if (repositoryBaseURLTxt == null) {
            return ICreaterepoConstants.EMPTY_STRING;
        }
        return repositoryBaseURLTxt.getText().trim();
    }

}
