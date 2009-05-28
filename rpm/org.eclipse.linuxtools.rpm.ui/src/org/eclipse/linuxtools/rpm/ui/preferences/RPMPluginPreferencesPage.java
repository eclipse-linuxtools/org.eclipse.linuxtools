/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

package org.eclipse.linuxtools.rpm.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



/**
 * This class implements a sample preference page that is
 * added to the preference dialog based on the registration.
 */
public class RPMPluginPreferencesPage extends PreferencePage
	implements IWorkbenchPreferencePage, SelectionListener, ModifyListener {
    
	private Text emailField;
	private Text nameField;
	
	private Text rpmField;
	private Text rpmbuildField;
	private Text diffField;
    
   private Composite createComposite(Composite parent, int numColumns) {
	   Composite composite = new Composite(parent, SWT.NULL);

	   //GridLayout
	   GridLayout layout = new GridLayout();
	   layout.numColumns = numColumns;
	   composite.setLayout(layout);

	   //GridData
	   GridData data = new GridData();
	   data.verticalAlignment = GridData.FILL;
	   data.horizontalAlignment = GridData.FILL;
	   composite.setLayoutData(data);
	   return composite;
   }
   
   private Label createLabel(Composite parent, String text) {
	   Label label = new Label(parent, SWT.LEFT);
	   label.setText(text);
	   GridData data = new GridData();
	   data.horizontalSpan = 1;
	   data.horizontalAlignment = GridData.FILL;
	   label.setLayoutData(data);
	   return label;
   }
  
   private Button createBrowseButton(Composite parent, Text field, String command) {
	   Button button = new Button(parent, SWT.PUSH);
	   button.setText("Browse..."); //$NON-NLS-1$
	   button.addSelectionListener(new BrowseSelectionListener(getShell(), 
	   								"Select '" + command + //$NON-NLS-1$
									"' Command", field)); //$NON-NLS-1$
	   GridData data = new GridData();
	   data.horizontalAlignment = GridData.FILL;
	   button.setLayoutData(data);
	   return button;
   }
 
   private Text createTextField(Composite parent) {
	   Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
	   text.addModifyListener(this);
	   GridData data = new GridData();
	   data.horizontalAlignment = GridData.FILL;
	   data.grabExcessHorizontalSpace = true;
	   data.verticalAlignment = GridData.CENTER;
	   data.grabExcessVerticalSpace = false;
	   text.setLayoutData(data);
	   return text;
   }
   
   protected void createSpacer(Composite composite, int columnSpan) {
   	   Label label = new Label(composite, SWT.NONE);
   	   GridData gd = new GridData();
   	   gd.horizontalSpan = columnSpan;
   	   label.setLayoutData(gd);
   }
   
   public void init(IWorkbench workbench){
   }
   
   private void initializeDefaults()
   {
		IPreferenceStore store = RPMCorePlugin.getDefault().getPreferenceStore();
		
		emailField.setText(store.getDefaultString(IRPMConstants.AUTHOR_EMAIL));
		nameField.setText(store.getDefaultString(IRPMConstants.AUTHOR_NAME));
		rpmField.setText(store.getDefaultString(IRPMConstants.RPM_CMD));
		rpmbuildField.setText(store.getDefaultString(IRPMConstants.RPMBUILD_CMD));
		diffField.setText(store.getDefaultString(IRPMConstants.DIFF_CMD));
		
   }
   
   private void initializeValues() {
	   IPreferenceStore store = RPMCorePlugin.getDefault().getPreferenceStore();
	   
	   emailField.setText(store.getString(IRPMConstants.AUTHOR_EMAIL));
	   nameField.setText(store.getString(IRPMConstants.AUTHOR_NAME));
	   rpmField.setText(store.getString(IRPMConstants.RPM_CMD));
	   rpmbuildField.setText(store.getString(IRPMConstants.RPMBUILD_CMD));
	   diffField.setText(store.getString(IRPMConstants.DIFF_CMD));
   }
	
   private void storeValues() {
		IPreferenceStore store = RPMCorePlugin.getDefault().getPreferenceStore();
	   	
		store.setValue(IRPMConstants.AUTHOR_NAME, nameField.getText());
		store.setValue(IRPMConstants.AUTHOR_EMAIL, emailField.getText());
		store.setValue(IRPMConstants.RPM_CMD, rpmField.getText());
		store.setValue(IRPMConstants.RPMBUILD_CMD, rpmbuildField.getText());
		store.setValue(IRPMConstants.DIFF_CMD, diffField.getText());
   	}
    
	public void modifyText(ModifyEvent event) {
		//Do nothing on a modification in this example
	}
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
		
	}
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	public boolean performOk() {
		storeValues();
		RPMCorePlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	protected Control createContents(Composite parent)
	{
		//mainComposite << parent
		Composite mainComposite = createComposite(parent, 1);
		mainComposite.setLayout(new GridLayout());
		
		Group userPrefs = new Group(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		userPrefs.setLayout(layout);
		userPrefs.setText("RPM Preferences"); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		userPrefs.setLayoutData(gd);
				
		createLabel(userPrefs, "Author Name: ");	 //$NON-NLS-1$
		nameField = createTextField(userPrefs);
		
		createLabel(userPrefs, "Author Email: ");	 //$NON-NLS-1$
		emailField = createTextField(userPrefs);
		
		createSpacer(mainComposite, 2);
		
		Group shellPrefs = new Group(mainComposite, SWT.NONE);
		shellPrefs.setText("Shell Commands"); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 3;
		shellPrefs.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		shellPrefs.setLayoutData(gd);
		
		String spacer = ": "; //$NON-NLS-1$
		String title = "rpm"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);	 
		rpmField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, rpmField, title);
		
		title = "rpmbuild"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		rpmbuildField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, rpmbuildField, title);
		
		title = "diff"; //$NON-NLS-1$
		createLabel(shellPrefs, title + spacer);
		diffField = createTextField(shellPrefs);
		createBrowseButton(shellPrefs, diffField, title);

		initializeValues();

		return new Composite(parent, SWT.NULL);
	}
	

   public void widgetDefaultSelected(SelectionEvent event) {
	   //Handle a default selection. Do nothing in this example
   }
   /** (non-Javadoc)
	* Method declared on SelectionListener
	*/
   public void widgetSelected(SelectionEvent event) {
	   //Do nothing on selection in this example;
   }
   
   private class BrowseSelectionListener implements SelectionListener {
   		private Text text;
   		private String title;
   		private Shell parent;
   		
   		public BrowseSelectionListener(Shell disp, String title, Text text) {
   			this.text = text;
   			this.title = title;
   			this.parent = disp;
   		}
   		
   		public void widgetDefaultSelected(SelectionEvent event) {
   			// no action
   		}
   	  
   		public void widgetSelected(SelectionEvent event) {
   			
			FileDialog fd = new FileDialog(parent, SWT.OPEN | SWT.APPLICATION_MODAL);
   			fd.setText(title); 
   			fd.setFileName(text.getText());
   			String result;
   			if( (result = fd.open()) != null )
   				text.setText(result);
   		}
   	}
 }
