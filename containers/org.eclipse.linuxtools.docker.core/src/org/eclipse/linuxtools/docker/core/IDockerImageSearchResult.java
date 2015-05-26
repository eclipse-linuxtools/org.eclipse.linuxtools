package org.eclipse.linuxtools.docker.core;

public interface IDockerImageSearchResult {

	/**
	 * @return the image description
	 */
	String getDescription();

	/**
	 * @return the official image flag
	 */
	boolean isOfficial();

	/**
	 * @return the automated build flag
	 */
	boolean isAutomated();

	/**
	 * @return the image name
	 */
	String getName();

	/**
	 * @return the star count
	 */
	int getStarCount();

}