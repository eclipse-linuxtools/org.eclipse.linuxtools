package org.eclipse.linuxtools.systemtap.structures.process;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * @since 2.2
 */
public class SystemTapRuntimeProcessFactory implements IProcessFactory {

	static public final String PROCESS_FACTORY_ID = "org.eclipse.linuxtools.systemtap.ui.ide.SystemTapRuntimeProcessFactory"; //$NON-NLS-1$

	static public class SystemTapRuntimeProcess extends RuntimeProcess {

		private Process originalProcess = null;

		public SystemTapRuntimeProcess(ILaunch launch, Process process,
				String name, Map<String, String> attributes) {
			super(launch, process, name, attributes);
			originalProcess = process;
		}

		public boolean matchesProcess(Process process) {
			return originalProcess.equals(process);
		}

		/**
		 * SystemTap scripts use a ScriptConsole instance as their output stream,
		 * so don't use the default stream.
		 */
		@Override
		protected IStreamsProxy createStreamsProxy() {
			return null;
		}

	}

	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label,
			Map<String, String> attributes) {

		return new SystemTapRuntimeProcess(launch, process, label, attributes);
	}

}
