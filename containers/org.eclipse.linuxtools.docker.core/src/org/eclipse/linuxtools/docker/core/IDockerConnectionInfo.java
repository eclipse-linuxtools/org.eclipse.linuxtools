package org.eclipse.linuxtools.docker.core;

import java.util.List;

public interface IDockerConnectionInfo {

	/**
	 * @return the memoryLimit
	 */
	boolean isMemoryLimit();

	/**
	 * @return the containers
	 */
	int getContainers();

	/**
	 * @return the debug
	 */
	boolean isDebug();

	/**
	 * @return the Driver status
	 */
	List<List<String>> getDriverStatus();

	/**
	 * @return the DockerRootDir
	 */
	String getDockerRootDir();
	
	/**
	 * @return the executionDriver
	 */
	String getExecutionDriver();
	
	/**
	 * @return the fileDescriptors
	 */
	int getFileDescriptors();

	/**
	 * @return the goroutines
	 */
	int getGoroutines();

	/**
	 * @return the images
	 */
	int getImages();

	/**
	 * @return the initPath
	 */
	String getInitPath();

	/**
	 * @return the initSha1
	 */
	String getInitSha1();
	
	/**
	 * @return the IndexServerAddress
	 */
	String getIndexServerAddress();
	
	/**
	 * @return the IPv4Forwarding flag
	 */
	boolean isIPv4Forwarding();
	
	/**
	 * @return the kernelVersion
	 */
	String getKernelVersion();

	/**
	 * @return the Labels
	 */
	List<String> getLabels();
	
	/**
	 * @return number of CPUs
	 */
	int getCPUNumber();

	/**
	 * @return total memory
	 */
	long getTotalMemory();
	
	/**
	 * @return VM name
	 */
	String getName();
	
	/**
	 * @return VM ID
	 */
	String getId();
	
	/**
	 * @return the storageDriver
	 */
	String getStorageDriver();

	/**
	 * @return the swapLimit
	 */
	boolean isSwapLimit();

	/**
	 * @return the apiVersion
	 */
	String getApiVersion();

	/**
	 * @return the gitCommit
	 */
	String getGitCommit();

	/**
	 * @return the os
	 */
	String getOs();

	/**
	 * @return the version
	 */
	String getVersion();

}