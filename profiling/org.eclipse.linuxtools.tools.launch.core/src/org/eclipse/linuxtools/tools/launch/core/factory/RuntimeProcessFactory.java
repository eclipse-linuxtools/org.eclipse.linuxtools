/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tools.launch.core.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

/*
 * Create process using Runtime.getRuntime().exec and prepends the
 * 'Linux tools path' project property to the environment PATH.
 * Use this factory instead of Runtime.getRuntime().exec if the command you
 * are running may be in the linux tools path selected in the project property
 * page.
 */
public class RuntimeProcessFactory extends LinuxtoolsProcessFactory {
	private static RuntimeProcessFactory instance = null;
	private static final String WHICH_CMD = "which"; //$NON-NLS-1$
	
	private IRemoteFileProxy proxy;
	

	private String[] tokenizeCommand(String command) {
		StringTokenizer tokenizer = new StringTokenizer(command);
		String[] cmdarray = new String[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreElements(); i++)
			cmdarray[i] = tokenizer.nextToken();

		return cmdarray;
	}

	private String[] fillPathCommand(String[] cmdarray, IProject project) throws IOException {
		cmdarray[0] = whichCommand(cmdarray[0], project);
		return cmdarray;
	}
	
	private String[] fillPathSudoCommand(String[] cmdarray, IProject project) throws IOException {
		cmdarray[1] = whichCommand(cmdarray[1], project);
		return cmdarray;
	}

	public String whichCommand(String command, IProject project) throws IOException {
		
		String[] envp = updateEnvironment(null, project);
		if (project != null) {
			try {
				proxy = RemoteProxyManager.getInstance().getFileProxy(project);
				URI whichUri = URI.create(WHICH_CMD);
				IPath whichPath = new Path(proxy.toPath(whichUri));
				IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(project);
				envp = updateEnvironment(envp, project);
				Process pProxy = launcher.execute(whichPath, new String[]{command}, envp, null, new NullProgressMonitor());
				if (pProxy != null){
					BufferedReader error = new BufferedReader(new InputStreamReader(pProxy.getErrorStream()));
					if(error.readLine() != null){
						throw new IOException(error.readLine());
					}
					BufferedReader reader = new BufferedReader(new InputStreamReader(pProxy.getInputStream()));
					String readLine = reader.readLine();
					command = readLine;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return command;
	}

	public static RuntimeProcessFactory getFactory() {
		if (instance == null)
			instance = new RuntimeProcessFactory();
		return instance;
	}

	public Process exec(String cmd, IProject project) throws IOException {
		return exec(cmd, null, null, project);
	}

	public Process exec(String[] cmdarray, IProject project) throws IOException {
		return exec(cmdarray, null, project);
	}

	public Process exec(String[] cmdarray, String[] envp, IProject project) throws IOException {
		return exec(cmdarray, envp, null, project);
	}

	public Process exec(String cmd, String[] envp, IProject project) throws IOException {
		return exec(cmd, envp, null, project);
	}

	public Process exec(String cmd, String[] envp, IFileStore dir, IProject project)
		throws IOException {
		return exec(tokenizeCommand(cmd), envp, dir, project);
	}

	public Process exec(String cmdarray[], String[] envp, IFileStore dir, IProject project)
		throws IOException {
			
		String command = cmdarray[0];
		URI uri = URI.create(command);

		Process p = null;
		try {
			cmdarray = fillPathCommand(cmdarray, project);
			
			IPath path = new Path(proxy.toPath(uri));
			IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(project);
			envp = updateEnvironment(envp, project);
			
			IPath changeToDir;
			if (dir == null){
				changeToDir = null;
			} else{ 
				changeToDir = new Path(proxy.toPath(dir.toURI()));
			}
			
			List<String> cmdlist = new ArrayList<String>(Arrays.asList(cmdarray));
			cmdlist.remove(0);
			cmdlist.toArray(cmdarray);
			cmdarray = cmdlist.toArray(new String[0]);

			p = launcher.execute(path, cmdarray, envp, changeToDir , new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return p;
	}
	
	public Process sudoExec(String cmd, IProject project) throws IOException {
		return sudoExec(cmd, null, null, project);
	}
	
	public Process sudoExec(String cmd, String[] envp, IProject project) throws IOException {
		return exec(cmd, envp, null, project);
	}
	
	public Process sudoExec(String cmd, String[] envp, IFileStore dir, IProject project)
			throws IOException {
			return sudoExec(tokenizeCommand(cmd), envp, dir, project);
		}
	
	public Process sudoExec(String[] cmdarray, IProject project) throws IOException {
		return sudoExec(cmdarray, null, project);
	}
	
	public Process sudoExec(String[] cmdarray, String[] envp, IProject project) throws IOException {
		return sudoExec(cmdarray, envp, null, project);
	}
	
	public Process sudoExec(String[] cmdarray, String[] envp, IFileStore dir, IProject project) throws IOException {
		URI uri = URI.create("sudo");
		
		List<String> cmdList = Arrays.asList(cmdarray);
		ArrayList<String> cmdArrayList = new ArrayList<String>(cmdList);
		cmdArrayList.add(0, "-n");
		
		String[] cmdArraySudo = new String[cmdArrayList.size()];
		cmdArrayList.toArray(cmdArraySudo);
		
		Process p = null;
		try {
			cmdArraySudo = fillPathSudoCommand(cmdArraySudo, project);
			
			IPath path = new Path(proxy.toPath(uri));
			IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(project);
			envp = updateEnvironment(envp, project);
			
			IPath changeToDir;
			if (dir == null){
				changeToDir = null;
			} else{ 
				changeToDir = new Path(proxy.toPath(dir.toURI()));
			}
			
			List<String> cmdlist = new ArrayList<String>(Arrays.asList(cmdArraySudo));
			cmdlist.remove(0);
			cmdlist.toArray(cmdArraySudo);
			cmdArraySudo = cmdlist.toArray(new String[0]);

			p = launcher.execute(path, cmdArraySudo, envp, changeToDir , new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return p;

	}
}
