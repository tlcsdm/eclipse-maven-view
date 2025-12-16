package com.tlcsdm.eclipse.mavenview;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class AbstractProjectBasedTest {

	protected static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";
	protected static final String MAVEN_NATURE = InitialProjectSelection.MAVEN_NATURE;

	@Rule
	public TestName testName = new TestName();

	@Before
	public final void setUpProjects() throws CoreException {
		deleteAllProjects();
		MavenViewPreferences.setAlwaysSelectedProjects();
		MavenViewPreferences.setNeverSelectedProjects();
	}

	@After
	public final void tearDownProjects() throws CoreException {
		deleteAllProjects();
	}

	protected static void deleteAllProjects() throws CoreException {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (final IProject project : workspaceRoot.getProjects()) {
			project.delete(true, new NullProgressMonitor());
		}
	}

	protected IProject createDefaultMavenProject() throws CoreException {
		return createMavenProject(this.testName.getMethodName());
	}

	protected static IProject createMavenProject(String projectName) throws CoreException {
		final IProject result = createProject(projectName);
		setProjectNatures(result, InitialProjectSelection.MAVEN_NATURE);
		return result;
	}

	protected IProject createDefaultProject() throws CoreException {
		return createProject(this.testName.getMethodName());
	}

	protected static IProject createProject(String projectName) throws CoreException {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(projectName);
		project.create(null);
		project.open(null);
		return project;
	}

	@SuppressWarnings("deprecation")
	protected static IProject createProject(String projectName, IPath location) throws CoreException {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(projectName);
		JavaCapabilityConfigurationPage.createProject(project, location, null);
		return project;
	}

	protected static void setProjectNatures(final IProject project, String... natures) throws CoreException {
		final IProjectDescription description = project.getDescription();
		description.setNatureIds(natures);
		project.setDescription(description, null);
	}

}
