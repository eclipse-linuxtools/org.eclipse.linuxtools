/***********************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 ***********************************************************************/

package org.eclipse.linuxtools.dataviewers.charts.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.swtchart.Chart;
import org.eclipse.birt.chart.model.Serializer;
import org.eclipse.birt.chart.model.impl.SerializerImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.dataviewers.charts.Activator;
import org.eclipse.linuxtools.dataviewers.charts.UIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

public class SaveXMLAction extends Action
{

	private Composite cmp;
	private Chart cm;

	public SaveXMLAction( Composite parent )
	{
		super( );
		cmp = parent;
		setImageDescriptor( UIHelper.getImageDescriptor( "icons/eexport.gif" ) ); 
		setDisabledImageDescriptor( UIHelper.getImageDescriptor( "icons/dexport.gif" ) );
		setToolTipText( "Save XML Source" ); 
		setDescription( "Save XML Source to the designated directory" );
	}

	/**
	 * When the action is invoked, pop up a File Dialog to designate the
	 * directory.
	 */
	public void run( )
	{
		if ( cm != null )
		{
			final FileDialog saveDialog = new FileDialog( cmp.getShell( ),
					SWT.SAVE );
			saveDialog.setFilterExtensions( new String[]{
				"*.chart"} ); //$NON-NLS-1$
			try
			{
				saveDialog.open( );
				String name = saveDialog.getFileName( );
				if ( name != null && name != "" ) //$NON-NLS-1$
				{
					Serializer serializer = null;
					final File file = new File( saveDialog.getFilterPath( ),
							name );
					if ( file.exists( ) )
					{
						MessageBox box = new MessageBox( cmp.getShell( ),
								SWT.ICON_WARNING | SWT.YES | SWT.NO );
						box.setText( "Save XML Source"); //$NON-NLS-1$
						box.setMessage( "The XML source already exists in the directory. \nDo you want to replace it?" ); 
						if ( box.open( ) != SWT.YES )
						{
							return;
						}
					}

					serializer = SerializerImpl.instance( );
//					try
//					{
//						serializer.write( cm, new FileOutputStream( file ) );
						IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
						if (c != null){
							try {
									c.refreshLocal(1, new NullProgressMonitor());
								} catch (CoreException e) {
									IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
									Activator.getDefault().getLog().log(status);
								}
						}
						
//					}
					/*catch ( IOException ioe )
					{
						ioe.printStackTrace( );
					}*/
				}
			}
			catch ( Throwable e )
			{
				e.printStackTrace( );
			}
		}
	}
	
	public void setChart(Chart chart) {
		try {
			if (chart != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
			cm = chart;
		} catch (Throwable _)
		{
			Status s = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					Status.ERROR,
					"Error when creating \"save as image\" action...",
					_);
			Activator.getDefault().getLog().log(s);
		}
	}
}
