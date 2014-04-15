/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.builder;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.internal.rpm.rpmlint.parser.RpmlintItem;
import org.eclipse.linuxtools.internal.rpm.rpmlint.parser.RpmlintParser;

/**
 * Project builder responsible for invoking rpmlint and processing it's
 * response.
 */
public class RpmlintBuilder extends IncrementalProjectBuilder {

    /**
     * Total number of chunks to divede the work in.
     */
    public static final int MAX_WORKS = 100;

    /**
     * ID for this builder.
     */
    public static final String BUILDER_ID = Activator.PLUGIN_ID
            + ".rpmlintBuilder"; //$NON-NLS-1$

    /**
     * ID for rpmlint marker problems.
     */
    public static final String MARKER_ID = Activator.PLUGIN_ID
            + ".rpmlintProblem"; //$NON-NLS-1$

    @Override
    protected IProject[] build(int kind, Map<String, String> args,
            IProgressMonitor monitor) throws CoreException {
        // TODO: handle the monitor in a more clean way.
        monitor.beginTask(Messages.RpmlintBuilder_0, MAX_WORKS);
        monitor.worked(20);
        if (kind == FULL_BUILD) {
            fullBuild(monitor);
        } else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild(monitor);
            } else {
                incrementalBuild(delta, monitor);
            }
        }
        return null;
    }

    private void fullBuild(IProgressMonitor monitor) throws CoreException {
        RpmlintPreVisitor resourceVisitor = new RpmlintPreVisitor();
        getProject().accept(resourceVisitor);
        checkCancel(monitor);
        monitor.worked(50);
        monitor.setTaskName(Messages.RpmlintBuilder_1);
        List<RpmlintItem> rpmlintItems = RpmlintParser
                .parseVisisted(resourceVisitor.getVisitedPaths());
        visitAndMarkRpmlintItems(monitor, rpmlintItems);
    }

    private void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        RpmlintDeltaVisitor deltaVisitor = new RpmlintDeltaVisitor();
        delta.accept(deltaVisitor);
        monitor.worked(50);
        monitor.setTaskName(Messages.RpmlintBuilder_1);
        List<RpmlintItem> rpmlintItems = RpmlintParser
                .parseVisisted(deltaVisitor.getVisitedPaths());
        visitAndMarkRpmlintItems(monitor, rpmlintItems);
    }

    private void visitAndMarkRpmlintItems(IProgressMonitor monitor,
            List<RpmlintItem> rpmlintItems) throws CoreException {
        if (rpmlintItems.size() > 0) {
            checkCancel(monitor);
            monitor.worked(70);
            monitor.setTaskName(Messages.RpmlintBuilder_2);
            getProject().accept(new RpmlintMarkerVisitor(rpmlintItems));
            monitor.worked(MAX_WORKS);
        }
    }

    private static void checkCancel(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
}
