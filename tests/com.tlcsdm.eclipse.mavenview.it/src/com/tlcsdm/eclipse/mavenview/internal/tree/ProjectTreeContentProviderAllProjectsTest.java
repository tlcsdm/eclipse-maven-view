package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;

import com.tlcsdm.eclipse.mavenview.InitialProjectSelection;
import com.tlcsdm.eclipse.mavenview.InitialProjectSelectionAllProjectsTest;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;

public class ProjectTreeContentProviderAllProjectsTest extends InitialProjectSelectionAllProjectsTest {

	@Before
	public void setUp() throws CoreException {
		MavenViewPreferences.setInitialProjectSelection(InitialProjectSelection.ALL_PROJECTS);
	}

	@Override
	protected IProject[] fetchMavenProjects() {
		return Arrays.stream(ProjectTreeContentProvider.fetchMavenProjects()).map(ProjectNode::getProjectResource)
				.toArray(IProject[]::new);
	}

}
