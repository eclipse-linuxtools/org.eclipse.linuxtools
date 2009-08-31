package org.eclipse.linuxtools.systemtap.localgui.launch;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.core.runtime.CoreException;

public class TranslationUnitVisitor implements ICElementVisitor{
	private ArrayList<String> functions;

	public TranslationUnitVisitor() {
		super();
		functions = new ArrayList<String>(); 
	}
	
	@Override
	public boolean visit(ICElement arg0) throws CoreException {
		if (arg0.getElementType() == ICElement.C_FUNCTION)
			functions.add(arg0.getElementName());
		return true;
	}
	
	
	public ArrayList<String> getFunctions() {
		return functions;
	}
	
	public int getNumberOfFunctions() {
		return functions.size();
	}


}

