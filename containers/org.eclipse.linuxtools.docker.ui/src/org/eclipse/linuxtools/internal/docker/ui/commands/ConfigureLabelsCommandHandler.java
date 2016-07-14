package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ConfigureLabels;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConfigureLabelsCommandHandler extends AbstractHandler {

	private static final String CONTAINER_FILTER_LABELS = "containerFilterLabels"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ConfigureLabels wizard = new ConfigureLabels();
		final boolean configureLabels = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (configureLabels) {
			Map<String, String> labels = wizard.getConfigureLabels();
			StringBuffer buffer = new StringBuffer();
			for (Entry<String, String> entry : labels.entrySet()) {
				buffer.append(entry.getKey());
				buffer.append('='); // $NON-NLS-1$
				buffer.append(entry.getValue());
				buffer.append('\u00a0');
			}
			IEclipsePreferences preferences = InstanceScope.INSTANCE
					.getNode(Activator.PLUGIN_ID);
			preferences.put(CONTAINER_FILTER_LABELS, buffer.toString());
			IDockerConnection connection = CommandUtils
					.getCurrentConnection(null);
			connection.getContainers(true); // force refresh
		}
		return null;
	}

}
