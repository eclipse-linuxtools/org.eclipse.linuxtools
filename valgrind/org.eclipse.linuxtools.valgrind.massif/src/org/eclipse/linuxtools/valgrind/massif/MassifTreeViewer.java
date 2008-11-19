package org.eclipse.linuxtools.valgrind.massif;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;

public class MassifTreeViewer extends TreeViewer {

	public MassifTreeViewer(Composite parent) {
		super(parent);
		
		setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				return ((MassifHeapTreeNode) parentElement).getChildren();
			}

			public Object getParent(Object element) {
				return ((MassifHeapTreeNode) element).getParent();
			}

			public boolean hasChildren(Object element) {
				MassifHeapTreeNode[] children = ((MassifHeapTreeNode) element).getChildren();
				return children != null && children.length > 0;
			}

			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public void dispose() {}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {}
			
		});
		
		setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MassifHeapTreeNode) element).getText();
			}
			
			@Override
			public Image getImage(Object element) {
				Image img = null;
				if (((MassifHeapTreeNode) element).getParent() == null) { // only show for root elements
					img = MassifPlugin.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID, "icons/memory_view.gif").createImage(); //$NON-NLS-1$
				}
				return img;
			}
		});
		
		addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				MassifHeapTreeNode element = (MassifHeapTreeNode) ((TreeSelection) event.getSelection()).getFirstElement();
				if (element.hasSourceFile()) {
					// do source lookup
					ISourceLocator sourceLocator = MassifPlugin.getDefault().getLaunch().getSourceLocator();
					if (sourceLocator instanceof ISourceLookupDirector) {
						Object obj = ((ISourceLookupDirector) sourceLocator).getSourceElement(element.getFilename());
						if (obj != null && obj instanceof IFile) {
							try {
								ProfileUIUtils.openEditorAndSelect(((IFile) obj).getLocation().toOSString(), element.getLine());
							} catch (PartInitException e) {
								e.printStackTrace();
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}			
		});
	}



}
