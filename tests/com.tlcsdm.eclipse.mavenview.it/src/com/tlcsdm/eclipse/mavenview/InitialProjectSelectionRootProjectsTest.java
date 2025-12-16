package com.tlcsdm.eclipse.mavenview;

import org.eclipse.core.resources.IProject;

public class InitialProjectSelectionRootProjectsTest extends AbstractFetchMavenProjectsTest {

	@Override
	protected IProject[] fetchMavenProjects() {
		return InitialProjectSelection.ROOT_PROJECTS.fetchMavenProjects();
	}

	@Override
	protected boolean areNestedProjectsFound() {
		return false;
	}
}
