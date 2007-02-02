/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.project;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
// gone! import org.eclipse.cdt.core.model.ICFile;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;


/**
 * @author keiths
 */
public class ProjectProfileExecutable extends ProfileElement
{
	protected IBinary _binary;

	/**
	 * Constructor for ProfileExecutable.
	 * @param parent
	 * @param binary
	 */
	public ProjectProfileExecutable(IProfileElement parent, IBinary binary)
	{
		super(parent, IProfileElement.OBJECT);
		_binary = binary;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 */
	public IProfileElement[] getChildren()
	{
		// Children of Executable are: libraries, object files
		ArrayList objects = new ArrayList();
		ArrayList libs = new ArrayList();
		
		// how to get objfiles?
		ICElement[] elements = new ICElement[0];
		try {
			elements = _binary.getChildren();
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < elements.length; i++)
		{
			/* FIXME: ICFile and C_FILE are gone!
			if (elements[i].getElementType() == ICElement.C_FILE)
			{
				ICFile cfile = (ICFile) elements[i];
				if (cfile.isArchive())
				{
					int j = 0; // ??
				}
				else if (cfile.isBinary())
				{
					int j = 0; //
				}
				else if (cfile.isTranslationUnit())
				{
					// This is an object representing the source file of an object file
					objects.add(new ProjectProfileObjectFile(this, cfile));					
				}
			}
			else*/ if (elements[i].getElementType() == ICElement.C_FUNCTION)
			{
				// These probably came from a library which was statically linked with the executable
				libs.add(new ProjectProfileFunction(this, (IFunction) elements[i]));
			}
		}
		
		// TODO: add sort option
		objects.addAll(libs);

		// Add required shared libraries
		/* FIXME: getFile gone!
		IBinaryParser parser = CModelManager.getBinaryParser(_binary.getFile().getProject());
		IArchiveContainer archive = _binary.getCProject().getArchiveContainer();
		IArchive[] archives = archive.getArchives();
		*/
	
		IProfileElement[] children = new IProfileElement[objects.size()];
		objects.toArray(children);
		return children;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		return (getChildren().length > 0);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelImage()
	 */
	public Image getLabelImage()
	{
		if (_binary.hasDebug())
			return CPluginImages.get(CPluginImages.IMG_OBJS_CEXEC_DEBUG);
			
		return CPluginImages.get(CPluginImages.IMG_OBJS_CEXEC);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return _binary.getElementName() + " - [" + _binary.getCPU() //$NON-NLS-1$
						+ (_binary.isLittleEndian() ? "le" : "be") + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public int getSampleCount() { return 0; }
}
