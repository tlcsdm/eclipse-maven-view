package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;

import com.tlcsdm.eclipse.mavenview.InitialProjectSelection;
import com.tlcsdm.eclipse.mavenview.InitialProjectSelectionRootProjectsTest;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;

public class ProjectTreeContentProviderRootProjectsTest extends InitialProjectSelectionRootProjectsTest {

	@Before
	public void setUp() throws CoreException {
		MavenViewPreferences.setInitialProjectSelection(InitialProjectSelection.ROOT_PROJECTS);
	}

	@Override
	protected IProject[] fetchMavenProjects() {
		return Arrays.stream(ProjectTreeContentProvider.fetchMavenProjects()).map(ProjectNode::getProjectResource)
				.toArray(IProject[]::new);
	}

}
