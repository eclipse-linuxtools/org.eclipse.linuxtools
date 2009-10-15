package org.eclipse.linuxtools.callgraph.graphlisteners;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * A Projectionist is the gguy that operates a movie camera.
 * @author chwang
 *
 */
public class Projectionist extends Job {
	private StapGraphKeyListener listener;
	private int frame_time = 2000;
	private boolean pause;
	

	/**
	 * @param name
	 * @param listener    -- the keyListener instantiating this class
	 * @param time -- Amount of time between frames
	 */
	public Projectionist(String name, StapGraphKeyListener listener, int time) {
		super(name);
		this.listener = listener;
		this.frame_time = time;
		pause = false;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {

		long snapshot = System.currentTimeMillis();
		while (true) {
			if (pause)
				return Status.OK_STATUS;
			
			if (System.currentTimeMillis() - snapshot >= frame_time) {
				snapshot = System.currentTimeMillis();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						listener.nextFrame();	
					}
				});
				
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (monitor.isCanceled()) {
				break;
			}
		}
		
		return Status.CANCEL_STATUS;
	}
	
	/**
	 * Projectionist will pause -- reschedule job to continue
	 */
	public void pause() {
		pause = true;
	}

}
