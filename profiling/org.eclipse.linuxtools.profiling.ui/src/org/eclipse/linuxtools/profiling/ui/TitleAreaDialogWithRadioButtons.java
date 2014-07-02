/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    lufimtse :  Leo Ufimtsev lufimtse@redhat.com
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * <h1> Dialogue with radio options. </h1>
 *
 * <p> 
 * This is useful if you need to ask the user to choose one of 'multiple options' instead of a 'yes/no'. <br>
 * It also looks cleaner than having multiple buttons at the bottom of the screen. 
 * </p>
 *
 * <p>
 * Please see the  <a href="https://wiki.eclipse.org/File:TitleAreaDialogWithRadioButtonsExample.png">Screen shot</a>
 * to see what it looks like. </p>
 *
 * <p> 
 * Please see <a href="https://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ/TitleAreaDialogWithRadioButtons""> wiki page </a>
 * for additional details & example usage code
 * </p>
 *  @since 3.1
 */
public class TitleAreaDialogWithRadioButtons extends TitleAreaDialog {

    private String selectedButton;
    private String title, bodyMsg;
    private int msgType;
    private List<Entry<String, String>> userButtonList; // ButtonID , Button Label
    private List<Button> widgetButtonList;

    /**
     * <h1> Construct dialogue. </h1>
     * <p>
     * Specify paramaters, then use open. 
     * </p>
     * <p> 
     * Please see <a href="https://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ/TitleAreaDialogWithRadioButtons""> wiki page </a>
     * for additional details & example.
     * </p>
     *
     * @param parentShell    Parent Shell
     * @param title          Title of the dialogue
     * @param bodyMsg        Body message of the dialogue
     * @param userButtonList A list of SimpleEntry<String,String> mapping ButtonIDs and their visable text.
     * (see <a href="https://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ/TitleAreaDialogWithRadioButtons#Example_usage"> wiki example </a> for details)
     * @param msgType        'IMessageProvider.INFORMATION '  Can be one of: NONE ERROR INFORMATION WARNING
     */
    public TitleAreaDialogWithRadioButtons(
            Shell parentShell, String title, String bodyMsg,
            List<Entry<String, String>> userButtonList,
            int msgType)  { //for type see: IMessageProvider

        super(parentShell);

        // Set the Buttons that will be used listed.
        this.userButtonList = userButtonList;

        //Set labels.
        this.title = title;
        this.bodyMsg = bodyMsg;

        //set type
        this.msgType = msgType;

        // avoid help button poping up.
        this.setHelpAvailable(false);

        selectedButton = null;
    }

    @Override
    public void create() {

        super.create();

        //The 'Message' of a TitleArea dialogue only spans 1-2 lines. Then text is cut off.
        //It is not very efficient for longer messages.
        //Thus we utilize it as a 'title' and instaed we appeng a label to act as body. (see below).
        setMessage(this.title, this.msgType);
        //setTitle(); //not used.

        //Set the size of the dialogue.
        //We avoid hard-coding size, instead we tell it to figure out the most optimal size.
        //this.getShell().setSize(650, 550); //Hard-Coded = bad.
        this.getShell().setSize(getInitialSize());
    }

    /**
     * <h1> Get selected button.</h1>
     * <p> 
     * Return the buttonID of the button that the user selected if he pressed ok. <br>
     * This is the first element of the  (id | label) tuple.
     * </p>
     *
     * @return ButtonID of selected button
     */
    public String getSelectedButton() {
        return selectedButton;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(1, false);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(layout);

        //Append a label to act as message body
        Label label = new Label(container, 0);
        label.setText(this.bodyMsg);

        //Add Radio buttons to dialogue.
        widgetButtonList = new ArrayList<>();
        int buttonCount = 1;
        for (Entry<String, String> usrbutton : userButtonList) {
             Button tmpButton = new Button(container, SWT.RADIO);
             tmpButton.setText(usrbutton.getValue());

             if (buttonCount == 1) {
                 tmpButton.setSelection(true); //Make first button be auto-selected.
                 buttonCount++;
             }
            widgetButtonList.add(tmpButton);
        }
        return area;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    protected void saveInput() {

        //Figure out which button was selected and set 'selectedButton' to it's key.
        for (int i = 0; i < widgetButtonList.size(); i++) {
            if (widgetButtonList.get(i).getSelection()) {
                selectedButton = userButtonList.get(i).getKey();
            }
        }
    }

    /**
     *  <p> 
     *  Called when the ok button is pressed. <br>
     *  Saves the state of the radio buttons prior to deconstruction. 
     *  </p>
     */
    @Override
    protected void okPressed() {
        saveInput(); // save input.
        super.okPressed(); // close dialogue
    }

}
