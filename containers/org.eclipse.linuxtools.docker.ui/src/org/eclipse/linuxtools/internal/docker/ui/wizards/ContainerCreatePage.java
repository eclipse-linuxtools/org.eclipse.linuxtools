/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ContainerCreatePage extends WizardPage {

	private final static String NAME = "ContainerCreate.name"; //$NON-NLS-1$
	private final static String TITLE = "ContainerCreate.title"; //$NON-NLS-1$
	private final static String DESC = "ContainerCreate.desc"; //$NON-NLS-1$
	private final static String CREATE_LABEL = "ContainerCreate.label"; //$NON-NLS-1$
	private final static String IMAGE_LABEL = "Image.label"; //$NON-NLS-1$
	private final static String CMD_LABEL = "Cmd.label"; //$NON-NLS-1$
	private final static String USER_LABEL = "User.label"; //$NON-NLS-1$
	private final static String WORKING_DIR_LABEL = "WorkingDir.label"; //$NON-NLS-1$
	private final static String ENV_LABEL = "Env.label"; //$NON-NLS-1$
	private final static String HOSTNAME_LABEL = "Hostname.label"; //$NON-NLS-1$
	private final static String DOMAINNAME_LABEL = "Domainname.label"; //$NON-NLS-1$
	private final static String MEMORY_LABEL = "Memory.label"; //$NON-NLS-1$
	private final static String MEMORY_SWAP_LABEL = "MemorySwap.label"; //$NON-NLS-1$
	private final static String CPU_SHARES_LABEL = "CpuShares.label"; //$NON-NLS-1$
	private final static String VOLUMES_LABEL = "Volumes.label"; //$NON-NLS-1$
	private final static String PORT_SPECS_LABEL = "PortSpecs.label"; //$NON-NLS-1$
	private final static String EXPOSED_PORTS_LABEL = "ExposedPorts.label"; //$NON-NLS-1$
	private final static String ON_BUILD_LABEL = "OnBuild.label"; //$NON-NLS-1$
	private final static String ENTRY_POINT_LABEL = "EntryPoint.label"; //$NON-NLS-1$
	private final static String CPU_SET_LABEL = "CpuSet.label"; //$NON-NLS-1$
	private final static String ATTACH_STDIN_LABEL = "AttachStdin.label"; //$NON-NLS-1$
	private final static String ATTACH_STDOUT_LABEL = "AttachStdout.label"; //$NON-NLS-1$
	private final static String ATTACH_STDERR_LABEL = "AttachStderr.label"; //$NON-NLS-1$
	private final static String TTY_LABEL = "Tty.label"; //$NON-NLS-1$
	private final static String OPEN_STDIN_LABEL = "OpenStdin.label"; //$NON-NLS-1$
	private final static String STDIN_ONCE_LABEL = "StdinOnce.label"; //$NON-NLS-1$
	private final static String NETWORK_DISABLED_LABEL = "NetworkDisabled.label"; //$NON-NLS-1$
	private final static String PRIVILEGED_LABEL = "Privileged.label"; //$NON-NLS-1$
	private final static String PUBLISH_ALL_LABEL = "PublishAll.label"; //$NON-NLS-1$
	private final static String NETWORK_MODE_LABEL = "NetworkMode.label"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private IDockerConnection connection;
	private String image;

	private Text imageText;
	private Text hostnameText;
	private Text domainnameText;
	private Text userText;
	private Text cmdText;
	private Text envText;
	private Text cpuSetText;
	private Text workingDirText;
	private Text portSpecsText;
	private Text exposedPortsText;
	private Text volumesText;
	private Text entryPointText;
	private Text onBuildText;
	private Text networkModeText;

	private Text memoryWidget;
	private Text memorySwapWidget;
	private Text cpuSharesWidget;

	private Button attachStdinButton;
	private Button attachStdoutButton;
	private Button attachStderrButton;
	private Button ttyButton;
	private Button openStdinButton;
	private Button stdinOnceButton;
	private Button networkDisabledButton;
	private Button privilegedButton;
	private Button publishAllPortsButton;

	public ContainerCreatePage(IDockerConnection connection, String image) {
		super(WizardMessages.getString(NAME));
		this.connection = connection;
		this.image = image;
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public String getImageId() {
		return imageText.getText();
	}

	public String getHostName() {
		return hostnameText.getText();
	}

	public String getDomainName() {
		return domainnameText.getText();
	}

	public String getUser() {
		return userText.getText();
	}

	public List<String> getCmd() {
		return getCmdList(cmdText.getText());
	}

	private List<String> getCmdList(String s) {
		ArrayList<String> list = new ArrayList<>();
		int length = s.length();
		boolean insideQuote1 = false; // single-quote
		boolean insideQuote2 = false; // double-quote
		boolean escaped = false;
		StringBuffer buffer = new StringBuffer();
		// Parse the string and break it up into chunks that are
		// separated by white-space or are quoted. Ignore characters
		// that have been escaped, including the escape character.
		for (int i = 0; i < length; ++i) {
			char c = s.charAt(i);
			if (escaped) {
				buffer.append(c);
				escaped = false;
			}
			switch (c) {
			case '\'':
				if (!insideQuote2)
					insideQuote1 = insideQuote1 ^ true;
				else
					buffer.append(c);
				break;
			case '\"':
				if (!insideQuote1)
					insideQuote2 = insideQuote2 ^ true;
				else
					buffer.append(c);
				break;
			case '\\':
				escaped = true;
				break;
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				if (insideQuote1 || insideQuote2)
					buffer.append(c);
				else {
					String item = buffer.toString();
					buffer.setLength(0);
					if (item.length() > 0)
						list.add(item);
				}
				break;
			default:
				buffer.append(c);
				break;
			}
		}
		// add last item of string that will be in the buffer
		String item = buffer.toString();
		if (item.length() > 0)
			list.add(item);
		return list;
	}

	public List<String> getEnv() {
		return getEnvList(envText.getText());
	}

	private List<String> getEnvList(String str) {
		ArrayList<String> envVars = new ArrayList<>();

		Pattern p1 = Pattern.compile("(\\w+[=]\\\".*?\\\"\\s*).*"); //$NON-NLS-1$
		Pattern p2 = Pattern.compile("(\\w+[=]'.*?'\\s*).*"); //$NON-NLS-1$
		Pattern p3 = Pattern.compile("(\\w+[=][^\\s]+).*"); //$NON-NLS-1$
		Pattern p4 = Pattern.compile("(\\w+[=][\\s]*$)"); //$NON-NLS-1$
		boolean finished = false;
		while (!finished) {
			Matcher m1 = p1.matcher(str);
			if (m1.matches()) {
				str = str.replaceFirst("\\w+[=]\\\".*?\\\"", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				String s = m1.group(1).trim();
				envVars.add(s.replaceAll("\\\"", "")); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				Matcher m2 = p2.matcher(str);
				if (m2.matches()) {
					str = str.replaceFirst("\\w+[=]'.*?'", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
					String s = m2.group(1).trim();
					envVars.add(s.replaceAll("'", ""));//$NON-NLS-1$ //$NON-NLS-2$ 
				} else {
					Matcher m3 = p3.matcher(str);
					if (m3.matches()) {
						str = str.replaceFirst("\\w+[=][^\\s]+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
						envVars.add(m3.group(1).trim());
					} else {
						Matcher m4 = p4.matcher(str);
						if (m4.matches()) {
							str = str.replaceFirst("\\w+[=][\\s]*$", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
							envVars.add(m4.group(1).trim());
						} else {
							finished = true;
						}
					}
				}
			}
		}
		return envVars;
	}

	public String getWorkingDir() {
		return workingDirText.getText();
	}

	public Long getMemory() {
		return getDigitalValue(memoryWidget.getText());
	}

	public Long getMemorySwap() {
		return getDigitalValue(memorySwapWidget.getText());
	}

	private Long getDigitalValue(String s) {
		s = s.trim();
		int len = s.length();
		long factor = 1;
		if (len > 0) {
			char c = s.charAt(len - 1);
			boolean foundSpecifier = false;
			switch (c) {
			case 'k':
				factor = 1000;
				foundSpecifier = true;
				break;
			case 'K':
				factor = 1024;
				foundSpecifier = true;
				break;
			case 'm':
				factor = 1000000;
				foundSpecifier = true;
				break;
			case 'M':
				factor = 1024 * 1024;
				foundSpecifier = true;
				break;
			case 'g':
				factor = 1000000000;
				foundSpecifier = true;
				break;
			case 'G':
				factor = 1024 * 1024 * 1024;
				foundSpecifier = true;
				break;
			case 't':
				factor = 1000000000000L;
				foundSpecifier = true;
				break;
			case 'T':
				factor = 1024 * 1024 * 1024 * 1024;
				foundSpecifier = true;
				break;
			}
			if (foundSpecifier) {
				s = s.substring(len - 1);
			}
		}
		return Long.valueOf(Long.parseLong(s) * factor);
	}

	public Long getCpuShares() {
		return Long.valueOf(cpuSharesWidget.getText());
	}

	public String getCpuSet() {
		return cpuSetText.getText();
	}

	public Boolean getAttachStdin() {
		return attachStdinButton.getSelection();
	}

	public Boolean getAttachStdout() {
		return attachStdoutButton.getSelection();
	}

	public Boolean getAttachStderr() {
		return attachStderrButton.getSelection();
	}

	public Boolean getTty() {
		return ttyButton.getSelection();
	}

	public Boolean getOpenStdin() {
		return openStdinButton.getSelection();
	}

	public Boolean getStdinOnce() {
		return stdinOnceButton.getSelection();
	}

	public Boolean getNetworkDisabled() {
		return networkDisabledButton.getSelection();
	}

	public Boolean getPrivileged() {
		return privilegedButton.getSelection();
	}

	public Boolean getPublishAllPorts() {
		return publishAllPortsButton.getSelection();
	}

	public String getNetworkMode() {
		return networkModeText.getText();
	}

	public Set<String> getVolumes() {
		String s = volumesText.getText().trim();
		if (s.length() == 0)
			return null;
		Set<String> volumeSet = new HashSet<>();
		String[] volumes = s.split("\\s+");
		for (String volume : volumes) {
			volume = volume.trim();
			if (volume.length() > 0 && !volume.contains(":")) //$NON-NLS-1$
				volumeSet.add(volume);
		}
		return volumeSet;
	}

	public List<String> getHostVolumes() {
		String s = volumesText.getText().trim();
		if (s.length() == 0)
			return null;
		List<String> hostVolumes = new ArrayList<>();
		String[] volumes = s.split("\\s+");
		for (String volume : volumes) {
			volume = volume.trim();
			if (volume.length() > 0 && volume.contains(":")) //$NON-NLS-1$
				hostVolumes.add(volume);
		}
		return hostVolumes;
	}

	public List<String> getPortSpecs() {
		String s = portSpecsText.getText().trim();
		if (s.length() == 0)
			return null;
		List<String> specList = new ArrayList<>();
		String[] specs = s.split("\\s+");
		for (String spec : specs) {
			spec = spec.trim();
			if (spec.length() > 0)
				specList.add(spec);
		}
		return specList;
	}

	public Set<String> getExposedPorts() {
		String s = exposedPortsText.getText().trim();
		if (s.length() == 0)
			return null;
		Set<String> exposedPortsSet = new HashSet<>();
		String[] exposedPorts = s.split("\\s+");
		for (String exposedPort : exposedPorts) {
			exposedPort = exposedPort.trim();
			if (exposedPort.length() > 0) {
				String[] segments = exposedPort.split(":"); //$NON-NLS-1$
				if (segments.length == 1) { // containerPort
					exposedPortsSet.add(segments[0]);
				} else if (segments.length == 2) { // hostPort:containerPort
					exposedPortsSet.add(segments[1]);
				} else if (segments.length == 3) { // either
													// ip:hostPort:containerPort
													// or ip::containerPort
					exposedPortsSet.add(segments[2]);
				}
			}
		}
		return exposedPortsSet;
	}

	public Map<String, List<IDockerPortBinding>> getPortBindings() {
		String s = exposedPortsText.getText().trim();
		if (s.length() == 0)
			return null;
		Map<String, List<IDockerPortBinding>> portBindingsMap = new HashMap<>();
		String[] exposedPorts = s.split("\\s+");
		for (String exposedPort : exposedPorts) {
			exposedPort = exposedPort.trim();
			if (exposedPort.length() > 0) {
				String[] segments = exposedPort.split(":"); //$NON-NLS-1$
				if (segments.length == 1) { // containerPort
					portBindingsMap.put(segments[0], Arrays
							.asList((IDockerPortBinding) new DockerPortBinding(
									"", ""))); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (segments.length == 2) { // hostPort:containerPort
					portBindingsMap.put(segments[1], Arrays
							.asList((IDockerPortBinding) new DockerPortBinding(
									"", segments[0]))); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (segments.length == 3) { // either
													// ip:hostPort:containerPort
													// or ip::containerPort
					if (segments[1].isEmpty()) {
						portBindingsMap
								.put(segments[2],
										Arrays.asList((IDockerPortBinding) new DockerPortBinding(
												"", segments[0]))); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						portBindingsMap
								.put(segments[2],
										Arrays.asList((IDockerPortBinding) new DockerPortBinding(
												segments[0], segments[1]))); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
		return portBindingsMap;
	}

	public List<String> getOnBuild() {
		String s = onBuildText.getText().trim();
		if (s.length() == 0)
			return null;
		List<String> onBuildList = new ArrayList<>();
		String[] onBuildStrings = s.split("\\s+");
		for (String onBuildString : onBuildStrings) {
			onBuildString = onBuildString.trim();
			if (onBuildString.length() > 0)
				onBuildList.add(onBuildString);
		}
		return onBuildList;
	}

	public List<String> getEntryPoint() {
		String s = entryPointText.getText().trim();
		if (s.length() == 0)
			return null;
		List<String> entryPointList = new ArrayList<>();
		String[] entryPoints = s.split("\\s+");
		for (String entryPoint : entryPoints) {
			entryPoint = entryPoint.trim();
			if (entryPoint.length() > 0)
				entryPointList.add(entryPoint);
		}
		return entryPointList;
	}

	private ModifyListener Listener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			// TODO Auto-generated method stub
			validate();
		}
	};

	private void validate() {
		boolean complete = true;
		boolean error = false;

		if (imageText.getText().length() == 0)
			complete = false;
		if (!error)
			setErrorMessage(null);
		setPageComplete(complete && !error);
	}

	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		final Composite container = new Composite(sc, SWT.NULL);
		sc.setContent(container);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NULL);
		label.setText(WizardMessages.getString(CREATE_LABEL));

		Label imageLabel = new Label(container, SWT.NULL);
		imageLabel.setText(WizardMessages.getString(IMAGE_LABEL));

		imageText = new Text(container, SWT.BORDER | SWT.SINGLE);
		if (image != null) {
			imageText.setText(image);
			imageText.setEditable(false);
		}
		imageText.addModifyListener(Listener);

		Label cmdLabel = new Label(container, SWT.NULL);
		cmdLabel.setText(WizardMessages.getString(CMD_LABEL));

		cmdText = new Text(container, SWT.BORDER | SWT.SINGLE);
		cmdText.addModifyListener(Listener);

		Label userLabel = new Label(container, SWT.NULL);
		userLabel.setText(WizardMessages.getString(USER_LABEL));

		userText = new Text(container, SWT.BORDER | SWT.SINGLE);
		userText.addModifyListener(Listener);

		Label workingDirLabel = new Label(container, SWT.NULL);
		workingDirLabel.setText(WizardMessages.getString(WORKING_DIR_LABEL));

		workingDirText = new Text(container, SWT.BORDER | SWT.SINGLE);
		workingDirText.addModifyListener(Listener);

		Label envLabel = new Label(container, SWT.NULL);
		envLabel.setText(WizardMessages.getString(ENV_LABEL));

		envText = new Text(container, SWT.BORDER | SWT.SINGLE);
		envText.addModifyListener(Listener);

		Label hostnameLabel = new Label(container, SWT.NULL);
		hostnameLabel.setText(WizardMessages.getString(HOSTNAME_LABEL));

		hostnameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		hostnameText.addModifyListener(Listener);

		Label domainnameLabel = new Label(container, SWT.NULL);
		domainnameLabel.setText(WizardMessages.getString(DOMAINNAME_LABEL));

		domainnameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		domainnameText.addModifyListener(Listener);

		Label memoryLabel = new Label(container, SWT.NULL);
		memoryLabel.setText(WizardMessages.getString(MEMORY_LABEL));

		memoryWidget = new Text(container, SWT.BORDER | SWT.SINGLE);
		memoryWidget.setText("0"); //$NON-NLS-1$
		memoryWidget.addModifyListener(Listener);

		Label memorySwapLabel = new Label(container, SWT.NULL);
		memorySwapLabel.setText(WizardMessages.getString(MEMORY_SWAP_LABEL));

		memorySwapWidget = new Text(container, SWT.BORDER | SWT.SINGLE);
		memorySwapWidget.setText("0"); //$NON-NLS-1$
		memorySwapWidget.addModifyListener(Listener);

		Label cpuSharesLabel = new Label(container, SWT.NULL);
		cpuSharesLabel.setText(WizardMessages.getString(CPU_SHARES_LABEL));

		cpuSharesWidget = new Text(container, SWT.BORDER | SWT.SINGLE);
		cpuSharesWidget.setText("0"); //$NON-NLS-1$
		cpuSharesWidget.addModifyListener(Listener);

		Label cpuSetLabel = new Label(container, SWT.NULL);
		cpuSetLabel.setText(WizardMessages.getString(CPU_SET_LABEL));

		cpuSetText = new Text(container, SWT.BORDER | SWT.SINGLE);
		cpuSetText.addModifyListener(Listener);

		Label volumesLabel = new Label(container, SWT.NULL);
		volumesLabel.setText(WizardMessages.getString(VOLUMES_LABEL));

		volumesText = new Text(container, SWT.BORDER | SWT.SINGLE);
		volumesText.addModifyListener(Listener);

		Label portSpecsLabel = new Label(container, SWT.NULL);
		portSpecsLabel.setText(WizardMessages.getString(PORT_SPECS_LABEL));

		portSpecsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		portSpecsText.addModifyListener(Listener);

		Label exposedPortsLabel = new Label(container, SWT.NULL);
		exposedPortsLabel
				.setText(WizardMessages.getString(EXPOSED_PORTS_LABEL));

		exposedPortsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		exposedPortsText.addModifyListener(Listener);

		Label onBuildLabel = new Label(container, SWT.NULL);
		onBuildLabel.setText(WizardMessages.getString(ON_BUILD_LABEL));

		onBuildText = new Text(container, SWT.BORDER | SWT.SINGLE);
		onBuildText.addModifyListener(Listener);

		Label entryPointLabel = new Label(container, SWT.NULL);
		entryPointLabel.setText(WizardMessages.getString(ENTRY_POINT_LABEL));

		entryPointText = new Text(container, SWT.BORDER | SWT.SINGLE);
		entryPointText.addModifyListener(Listener);

		Label networkModeLabel = new Label(container, SWT.NULL);
		networkModeLabel.setText(WizardMessages.getString(NETWORK_MODE_LABEL));

		networkModeText = new Text(container, SWT.BORDER | SWT.SINGLE);
		networkModeText.addModifyListener(Listener);

		attachStdinButton = new Button(container, SWT.CHECK);
		attachStdinButton.setText(WizardMessages.getString(ATTACH_STDIN_LABEL));

		attachStdoutButton = new Button(container, SWT.CHECK);
		attachStdoutButton.setText(WizardMessages
				.getString(ATTACH_STDOUT_LABEL));

		attachStderrButton = new Button(container, SWT.CHECK);
		attachStderrButton.setText(WizardMessages
				.getString(ATTACH_STDERR_LABEL));

		ttyButton = new Button(container, SWT.CHECK);
		ttyButton.setText(WizardMessages.getString(TTY_LABEL));

		openStdinButton = new Button(container, SWT.CHECK);
		openStdinButton.setText(WizardMessages.getString(OPEN_STDIN_LABEL));

		stdinOnceButton = new Button(container, SWT.CHECK);
		stdinOnceButton.setText(WizardMessages.getString(STDIN_ONCE_LABEL));

		networkDisabledButton = new Button(container, SWT.CHECK);
		networkDisabledButton.setText(WizardMessages
				.getString(NETWORK_DISABLED_LABEL));

		privilegedButton = new Button(container, SWT.CHECK);
		privilegedButton.setText(WizardMessages.getString(PRIVILEGED_LABEL));

		publishAllPortsButton = new Button(container, SWT.CHECK);
		publishAllPortsButton.setText(WizardMessages
				.getString(PUBLISH_ALL_LABEL));

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = imageText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0);
		label.setLayoutData(f);

		Control prevControl = label;
		Control longestLabel = workingDirLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		imageLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		imageText.setLayoutData(f);

		prevControl = imageLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		cmdLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		cmdText.setLayoutData(f);

		prevControl = cmdLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		userLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		userText.setLayoutData(f);

		prevControl = userLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		workingDirLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		workingDirText.setLayoutData(f);

		prevControl = workingDirLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		envLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		envText.setLayoutData(f);

		prevControl = envLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		hostnameLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		hostnameText.setLayoutData(f);

		prevControl = hostnameLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		domainnameLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		domainnameText.setLayoutData(f);

		prevControl = domainnameLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		memoryLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		memoryWidget.setLayoutData(f);

		prevControl = memoryLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		memorySwapLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		memorySwapWidget.setLayoutData(f);

		prevControl = memorySwapLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		cpuSharesLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		cpuSharesWidget.setLayoutData(f);

		prevControl = cpuSharesLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		cpuSetLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		cpuSetText.setLayoutData(f);

		prevControl = cpuSetLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		volumesLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		volumesText.setLayoutData(f);

		prevControl = volumesLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		portSpecsLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		portSpecsText.setLayoutData(f);

		prevControl = portSpecsLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		exposedPortsLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		exposedPortsText.setLayoutData(f);

		prevControl = exposedPortsLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		onBuildLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		onBuildText.setLayoutData(f);

		prevControl = onBuildLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		entryPointLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		entryPointText.setLayoutData(f);

		prevControl = entryPointLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		networkModeLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		networkModeText.setLayoutData(f);

		prevControl = networkModeLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		attachStdinButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(attachStdinButton, 10);
		attachStdoutButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(attachStdoutButton, 10);
		attachStderrButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(attachStderrButton, 10);
		ttyButton.setLayoutData(f);

		prevControl = attachStdinButton;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		openStdinButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(openStdinButton, 10);
		stdinOnceButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(stdinOnceButton, 10);
		networkDisabledButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(networkDisabledButton, 0);
		privilegedButton.setLayoutData(f);

		prevControl = openStdinButton;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		publishAllPortsButton.setLayoutData(f);

		container.setSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		setControl(sc);
		setPageComplete(false);
		cmdText.setFocus();
	}
}
