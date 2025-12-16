package com.tlcsdm.eclipse.mavenview;

import org.eclipse.core.resources.IProject;

public class InitialProjectSelectionAllProjectsTest extends AbstractFetchMavenProjectsTest {

	@Override
	protected IProject[] fetchMavenProjects() {
		return InitialProjectSelection.ALL_PROJECTS.fetchMavenProjects();
	}

	@Override
	protected boolean areNestedProjectsFound() {
		return true;
	}

}
