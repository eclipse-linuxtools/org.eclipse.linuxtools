package org.eclipse.linuxtools.profiling.launch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.TextConsole;

/**
 * Helper class for launching command line tools. Contains methods for creating a process and
 * one method for fetching from the console. 
 * 
 * 
 * @author chwang
 *
 */
public abstract class ProfileLaunchConfigurationDelegate extends AbstractCLaunchDelegate{

	/**
	 * Deletes and recreates the file at outputPath
	 * 
	 * @param outputPath
	 * @return false if there is an IOException
	 */
	protected boolean testOutput(String outputPath) {
		try {
			//Make sure the output file exists
			File tempFile = new File(outputPath);
			tempFile.delete();
			tempFile.createNewFile();
		} catch (IOException e1) {
			return false;
		}
		return true;
	}
	
	/**
	 * This method will create a process in the command line with I/O directed to the Eclipse console.
	 * It returns a reference to the process. Note that the process runs independently of Eclipse's threads,
	 * so you will have to poll the process to determine when it has terminated. To grab output from the 
	 * process, either attach a <code>org.eclipse.debug.core.model.IStreamMonitor</code> to one of the monitors
	 * in <code>process.getStreamsProxy()</code>, or use the static get methods in 
	 * <code>ProfileLaunchConfigurationDelegate</code>.
	 * 
	 * <br>
	 * Will call generateCommand(config) to create the command line.
	 * 
	 * 
	 * @param config -- Use the configuration passed as a parameter to the launch method.
	 * @param cmd -- Command string, as it would appear on the command line.
	 * @param launch -- use the launch passed as a parameter to the launch method.
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */

	protected IProcess createProcess(ILaunchConfiguration config, ILaunch launch) throws CoreException, IOException {
		File workDir = getWorkingDirectory(config);
		if (workDir == null) {
			workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		//Put command into a shell script
		String cmd = generateCommand(config);
		File script = File.createTempFile("org.eclipse.linuxtools.profiling.launch" + System.currentTimeMillis(), ".sh");
		String data = "#!/bin/sh\nexec " + cmd; //$NON-NLS-1$
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(script);
			out.write(data.getBytes());
		} finally {
			if (out != null) {
				out.close();
			}
		}
		
		String[] commandArray = prepareCommand("sh " + script.getAbsolutePath());
		Process subProcess = execute(commandArray, getEnvironment(config),
				workDir, true);
		
		IProcess process = createNewProcess(launch, subProcess,commandArray[0]);
		// set the command line used
		process.setAttribute(IProcess.ATTR_CMDLINE,cmd);
		
		return process;
	}
	
	/**
	 * Use to generate the command. 
	 * @param config
	 * @return The command string, as it would appear on command-line
	 */
	public abstract String generateCommand(ILaunchConfiguration config);
	

	/** 
	 * Prepare cmd for execution - we need a command array of strings,
 	 * no string can contain a space character. The resulting command
 	 * array will be passed in to the process.
	 */
	protected String[] prepareCommand(String cmd) {
		String tmp[] = cmd.split(" "); //$NON-NLS-1$
		ArrayList<String> cmdLine = new ArrayList<String>();
		for (String str : tmp) {
			cmdLine.add(str);
		}
		return (String[]) cmdLine.toArray(new String[cmdLine.size()]);
	}
	
	
	/**
	 * Executes a command array using pty
	 * 
	 * @param commandArray -- Split a command string on the ' ' character
	 * @param env -- Use <code>getEnvironment(ILaunchConfiguration)</code> in the AbstractCLaunchDelegate.
	 * @param wd -- Working directory
	 * @param usePty -- A value of 'true' usually suffices
	 * @return A properly formed process, or null
	 * @throws IOException -- If the process cannot be created
	 */
	public Process execute(String[] commandArray, String[] env, File wd,
			boolean usePty) throws IOException {
		Process process = null;
		try {
			if (wd == null) {
				process = ProcessFactory.getFactory().exec(commandArray, env);
			} else {
				if (PTY.isSupported() && usePty) {
					process = ProcessFactory.getFactory().exec(commandArray,
							env, wd, new PTY());
				} else {
					process = ProcessFactory.getFactory().exec(commandArray,
							env, wd);
				}
			}
		} catch (IOException e) {
			if (process != null) {
				process.destroy();
			}
			return null;
		}
		return process;
	}
	


	/**
	 * Spawn a new IProcess using the Debug Plugin.
	 * 
	 * @param launch
	 * @param systemProcess
	 * @param programName
	 * @return
	 */
	protected IProcess createNewProcess(ILaunch launch, Process systemProcess,
			String programName) {
		return DebugPlugin.newProcess(launch, systemProcess,
				renderProcessLabel(programName));
	}
	
	

	/**
	 * 
	 * @param search : A String that can be found in the console
	 * @return The TextConsole having 'name' somewhere within it's name
	 */
	public static TextConsole getConsole(String search) {
		for (int i = 0; i < ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles().length; i++) {
			if (ConsolePlugin.getDefault().getConsoleManager().
					getConsoles()[i].getName().contains(search)) {
				return (TextConsole)ConsolePlugin.getDefault().getConsoleManager().getConsoles()[i];
			}
		}
		return null;
	}

	
	/**
	 * Returns the contents of a console as a String
	 * 
	 * @param search : Console name
	 * @return The text contained within that console
	 */
	public static String getMainConsoleText(String search){
		TextConsole proc = (TextConsole) getConsole(search);
		return ((IDocument)proc.getDocument()).get();
	}
	
	/**
	 * Return the document attached to containing the given string. For best results,
	 * use <code>ILaunchConfiguration.getName()</code>.
	 * @param search
	 * @return
	 */
	public static IDocument getConsoleDocument(String search) {
		return ((TextConsole)getConsole(search)).getDocument();
	}
	
}
