package com.tlcsdm.eclipse.mavenview;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractFetchMavenProjectsTest extends AbstractProjectBasedTest {

	@Test
	public void testFetchMavenProjectsNoProject() throws Exception {
		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertArrayEquals(new IProject[0], mavenProjects);
	}

	protected abstract IProject[] fetchMavenProjects();

	@Test
	public void testFetchMavenProjectsNoMavenProject() throws Exception {
		createDefaultProject();

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(0, mavenProjects.length);
	}

	@Test
	public void testFetchMavenProjectsMavenProject() throws Exception {
		final IProject project = createDefaultProject();
		setProjectNatures(project, InitialProjectSelection.MAVEN_NATURE);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(1, mavenProjects.length);
		Assert.assertEquals(project, mavenProjects[0]);
	}

	@Test
	public void testFetchMavenProjectsMavenAndStuffProject() throws Exception {
		final IProject project = createDefaultProject();
		setProjectNatures(project, JAVA_NATURE, InitialProjectSelection.MAVEN_NATURE);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(1, mavenProjects.length);
		Assert.assertEquals(project, mavenProjects[0]);
	}

	@Test
	public void testFetchMavenProjectsMultipleMavenProjects() throws Exception {
		final IProject mavenProject1 = createProject("1");
		setProjectNatures(mavenProject1, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject2 = createProject("2");
		setProjectNatures(mavenProject2, InitialProjectSelection.MAVEN_NATURE);

		createDefaultProject();

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(2, mavenProjects.length);
		Assert.assertEquals(mavenProject1, mavenProjects[0]);
		Assert.assertEquals(mavenProject2, mavenProjects[1]);
	}

	@Test
	public void testFetchMavenProjectsClosedMavenProject() throws Exception {
		final IProject project = createDefaultProject();
		setProjectNatures(project, InitialProjectSelection.MAVEN_NATURE);
		project.close(new NullProgressMonitor());

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(0, mavenProjects.length);
	}

	@Test
	public void testFetchMavenProjectsNestedMavenProjects() throws Exception {
		final IProject mavenProject1 = createProject("1");
		setProjectNatures(mavenProject1, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject2 = createProject("2", mavenProject1.getLocation().append("2"));
		setProjectNatures(mavenProject2, InitialProjectSelection.MAVEN_NATURE);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);

		if (areNestedProjectsFound()) {
			Assert.assertEquals(2, mavenProjects.length);
			Assert.assertEquals(mavenProject1, mavenProjects[0]);
			Assert.assertEquals(mavenProject2, mavenProjects[1]);
		} else {
			Assert.assertEquals(1, mavenProjects.length);
			Assert.assertEquals(mavenProject1, mavenProjects[0]);
		}
	}

	protected abstract boolean areNestedProjectsFound();

	@Test
	public void testFetchMavenProjectsNestedMavenProjectsSwitched() throws Exception {
		final IProject mavenProject1 = createProject("2");
		setProjectNatures(mavenProject1, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject2 = createProject("1", mavenProject1.getLocation().append("1"));
		setProjectNatures(mavenProject2, InitialProjectSelection.MAVEN_NATURE);

		final IProject[] mavenProjects = fetchMavenProjects();

		if (areNestedProjectsFound()) {
			Assert.assertEquals(2, mavenProjects.length);
			Assert.assertEquals(mavenProject2, mavenProjects[0]);
			Assert.assertEquals(mavenProject1, mavenProjects[1]);
		} else {
			Assert.assertEquals(1, mavenProjects.length);
			Assert.assertEquals(mavenProject1, mavenProjects[0]);
		}
	}

	@Test
	public void testFetchMavenProjectsMultiNestedMavenProjects() throws Exception {
		final IProject mavenProject1 = createProject("1");
		setProjectNatures(mavenProject1, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject2 = createProject("2", mavenProject1.getLocation().append("2"));
		setProjectNatures(mavenProject2, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject3 = createProject("3", mavenProject1.getLocation().append("3"));
		setProjectNatures(mavenProject3, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject4 = createProject("4", mavenProject3.getLocation().append("4"));
		setProjectNatures(mavenProject4, InitialProjectSelection.MAVEN_NATURE);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);

		if (areNestedProjectsFound()) {
			Assert.assertEquals(4, mavenProjects.length);
			Assert.assertEquals(mavenProject1, mavenProjects[0]);
			Assert.assertEquals(mavenProject2, mavenProjects[1]);
			Assert.assertEquals(mavenProject3, mavenProjects[2]);
			Assert.assertEquals(mavenProject4, mavenProjects[3]);
		} else {
			Assert.assertEquals(1, mavenProjects.length);
			Assert.assertEquals(mavenProject1, mavenProjects[0]);
		}
	}

	@Test
	public void testFetchMavenProjectsMultiNestedMavenProjectsSwitched() throws Exception {
		final IProject mavenProject1 = createProject("4");
		setProjectNatures(mavenProject1, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject2 = createProject("3", mavenProject1.getLocation().append("3"));
		setProjectNatures(mavenProject2, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject3 = createProject("2", mavenProject1.getLocation().append("2"));
		setProjectNatures(mavenProject3, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject4 = createProject("1", mavenProject3.getLocation().append("1"));
		setProjectNatures(mavenProject4, InitialProjectSelection.MAVEN_NATURE);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);

		if (areNestedProjectsFound()) {
			Assert.assertEquals(4, mavenProjects.length);
			Assert.assertEquals(mavenProject4, mavenProjects[0]);
			Assert.assertEquals(mavenProject3, mavenProjects[1]);
			Assert.assertEquals(mavenProject2, mavenProjects[2]);
			Assert.assertEquals(mavenProject1, mavenProjects[3]);
		} else {
			Assert.assertEquals(1, mavenProjects.length);
			Assert.assertEquals(mavenProject1, mavenProjects[0]);
		}
	}

	@Test
	public void testNeverShown() throws Exception {
		final IProject project = createDefaultProject();
		setProjectNatures(project, InitialProjectSelection.MAVEN_NATURE);
		MavenViewPreferences.setNeverSelectedProjects(project);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(0, mavenProjects.length);
	}

	@Test
	public void testAlwaysShown() throws Exception {
		final IProject mavenProject1 = createProject("1");
		setProjectNatures(mavenProject1, InitialProjectSelection.MAVEN_NATURE);

		final IProject mavenProject2 = createProject("2", mavenProject1.getLocation().append("2"));
		setProjectNatures(mavenProject2, InitialProjectSelection.MAVEN_NATURE);
		MavenViewPreferences.setAlwaysSelectedProjects(mavenProject2);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);

		Assert.assertEquals(2, mavenProjects.length);
		Assert.assertEquals(mavenProject1, mavenProjects[0]);
		Assert.assertEquals(mavenProject2, mavenProjects[1]);
	}

	@Test
	public void testAlwaysShownButNotTwice() throws Exception {
		final IProject project = createDefaultProject();
		setProjectNatures(project, InitialProjectSelection.MAVEN_NATURE);
		MavenViewPreferences.setAlwaysSelectedProjects(project);

		final IProject[] mavenProjects = fetchMavenProjects();

		Assert.assertNotNull(mavenProjects);
		Assert.assertEquals(1, mavenProjects.length);
		Assert.assertEquals(project, mavenProjects[0]);
	}

}
