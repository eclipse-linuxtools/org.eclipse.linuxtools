package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.core.runtime.NullProgressMonitor;

public class ProgressMonitor extends NullProgressMonitor {

    private boolean done = false;

    public boolean isDone() {
        return done;
    }

    @Override
    public void done() {
        super.done();
        done = true;
    }

}
