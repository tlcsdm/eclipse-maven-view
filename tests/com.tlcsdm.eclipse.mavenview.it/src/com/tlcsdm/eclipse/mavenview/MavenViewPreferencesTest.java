package com.tlcsdm.eclipse.mavenview;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MavenViewPreferencesTest {

	private static final InitialProjectSelection INITIAL_PROJECT_SELECTION_DEFAULT = InitialProjectSelection.ROOT_PROJECTS;
	private static final InitialProjectSelection INITIAL_PROJECT_SELECTION_OTHER = InitialProjectSelection.ALL_PROJECTS;

	private static final Phase[] DISPLAYED_PHASES_DEFAULT = Phase.values();
	private static final Phase[] DISPLAYED_PHASES_OTHER = new Phase[] { Phase.CLEAN, Phase.INSTALL };

	@Rule
	public TestName testName = new TestName();

	private IPreferenceStore preferences;

	@Before
	public void setUp() throws Exception {
		this.preferences = Activator.getDefault().getPreferenceStore();
		this.preferences.setToDefault(MavenViewPreferences.INITIAL_PROJECT_SELECTION);

		AbstractFetchMavenProjectsTest.deleteAllProjects();
	}

	@After
	public void tearDown() throws CoreException {
		AbstractFetchMavenProjectsTest.deleteAllProjects();
	}

	@Test
	public void testInitialProjectSelectionDefault() throws Exception {
		Assert.assertEquals(INITIAL_PROJECT_SELECTION_DEFAULT, MavenViewPreferences.getInitialProjectSelection());
	}

	@Test
	public void testGetInitialProjectSelection() throws Exception {
		this.preferences.setValue(MavenViewPreferences.INITIAL_PROJECT_SELECTION,
				INITIAL_PROJECT_SELECTION_OTHER.name());

		Assert.assertEquals(INITIAL_PROJECT_SELECTION_OTHER, MavenViewPreferences.getInitialProjectSelection());
	}

	@Test
	public void testGetInitialProjectSelectionNull() throws Exception {
		this.preferences.setToDefault(MavenViewPreferences.INITIAL_PROJECT_SELECTION);

		Assert.assertEquals(INITIAL_PROJECT_SELECTION_DEFAULT, MavenViewPreferences.getInitialProjectSelection());
	}

	@Test
	public void testSetInitialProjectSelection() throws Exception {
		MavenViewPreferences.setInitialProjectSelection(INITIAL_PROJECT_SELECTION_OTHER);

		Assert.assertEquals(INITIAL_PROJECT_SELECTION_OTHER.name(),
				this.preferences.getString(MavenViewPreferences.INITIAL_PROJECT_SELECTION));
	}

	@Test
	public void testSetInitialProjectSelectionNull() throws Exception {
		MavenViewPreferences.setInitialProjectSelection(null);

		Assert.assertEquals(INITIAL_PROJECT_SELECTION_DEFAULT.name(),
				this.preferences.getString(MavenViewPreferences.INITIAL_PROJECT_SELECTION));
	}

	@Test
	public void testDisplayedPhasesDefault() throws Exception {
		Assert.assertArrayEquals(DISPLAYED_PHASES_DEFAULT, MavenViewPreferences.getDisplayedPhases());
	}

	@Test
	public void testGetDisplayedPhases() throws Exception {
		this.preferences.setValue(MavenViewPreferences.DISPLAYED_PHASES,
				MavenViewPreferences.getDisplayedPhasesString(DISPLAYED_PHASES_OTHER));

		Assert.assertArrayEquals(DISPLAYED_PHASES_OTHER, MavenViewPreferences.getDisplayedPhases());
	}

	@Test
	public void testGetDisplayedPhasesNull() throws Exception {
		this.preferences.setToDefault(MavenViewPreferences.DISPLAYED_PHASES);

		Assert.assertArrayEquals(DISPLAYED_PHASES_DEFAULT, MavenViewPreferences.getDisplayedPhases());
	}

	@Test
	public void testSetDisplayedPhases() throws Exception {
		MavenViewPreferences.setDisplayedPhases(DISPLAYED_PHASES_OTHER);

		Assert.assertEquals(MavenViewPreferences.getDisplayedPhasesString(DISPLAYED_PHASES_OTHER),
				this.preferences.getString(MavenViewPreferences.DISPLAYED_PHASES));
	}

	@Test
	public void testSetDisplayedPhasesNull() throws Exception {
		MavenViewPreferences.setDisplayedPhases(null);

		Assert.assertEquals(MavenViewPreferences.getDisplayedPhasesString(DISPLAYED_PHASES_DEFAULT),
				this.preferences.getString(MavenViewPreferences.DISPLAYED_PHASES));
	}

	@Test
	public void testGetDisplayedPhasesString() throws Exception {
		Assert.assertEquals("CLEAN\nINSTALL", MavenViewPreferences.getDisplayedPhasesString(DISPLAYED_PHASES_OTHER));
	}

	// alwaysSelectedProjects

	@Test
	public void testGetAlwaysSelectedProject() throws Exception {
		final String projectName = this.testName.getMethodName();
		final IProject project = AbstractProjectBasedTest.createMavenProject(projectName);

		this.preferences.setValue(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS, projectName);

		Assert.assertArrayEquals(new IProject[] { project }, MavenViewPreferences.getAlwaysSelectedProjects());
	}

	@Test
	public void testGetAlwaysSelectedProjects() throws Exception {
		final String projectName1 = this.testName.getMethodName() + "-1";
		final IProject project1 = AbstractProjectBasedTest.createMavenProject(projectName1);

		final String projectName2 = this.testName.getMethodName() + "-2";
		final IProject project2 = AbstractProjectBasedTest.createMavenProject(projectName2);

		this.preferences.setValue(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS,
				projectName1 + MavenViewPreferences.SEPARATOR + projectName2);

		Assert.assertArrayEquals(new IProject[] { project1, project2 },
				MavenViewPreferences.getAlwaysSelectedProjects());
	}

	@Test
	public void testGetAlwaysSelectedProjectsNull() throws Exception {
		this.preferences.setToDefault(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS);

		Assert.assertArrayEquals(new IProject[0], MavenViewPreferences.getAlwaysSelectedProjects());
	}

	@Test
	public void testSetAlwaysSelectedProject() throws Exception {
		final String projectName = this.testName.getMethodName();
		final IProject project = AbstractProjectBasedTest.createMavenProject(projectName);

		MavenViewPreferences.setAlwaysSelectedProjects(new IProject[] { project });

		Assert.assertEquals(projectName, this.preferences.getString(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS));
	}

	@Test
	public void testSetAlwaysSelectedProjects() throws Exception {
		final String projectName1 = this.testName.getMethodName() + "-1";
		final IProject project1 = AbstractProjectBasedTest.createMavenProject(projectName1);

		final String projectName2 = this.testName.getMethodName() + "-2";
		final IProject project2 = AbstractProjectBasedTest.createMavenProject(projectName2);

		MavenViewPreferences.setAlwaysSelectedProjects(project1, project2);

		Assert.assertEquals(projectName1 + MavenViewPreferences.SEPARATOR + projectName2,
				this.preferences.getString(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS));
	}

	@Test
	public void testSetAlwaysSelectedProjectsNull() throws Exception {
		MavenViewPreferences.setAlwaysSelectedProjects((IProject[]) null);

		Assert.assertEquals("", this.preferences.getString(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS));
	}

	@Test
	public void testSetAlwaysSelectedProjectsEmpty() throws Exception {
		MavenViewPreferences.setAlwaysSelectedProjects(new IProject[0]);

		Assert.assertEquals("", this.preferences.getString(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS));
	}

	// neverSelectedProjects

	@Test
	public void testGetNeverSelectedProject() throws Exception {
		final String projectName = this.testName.getMethodName();
		final IProject project = AbstractProjectBasedTest.createMavenProject(projectName);

		this.preferences.setValue(MavenViewPreferences.NEVER_SELECTED_PROJECTS, projectName);

		Assert.assertArrayEquals(new IProject[] { project }, MavenViewPreferences.getNeverSelectedProjects());
	}

	@Test
	public void testGetNeverSelectedProjects() throws Exception {
		final String projectName1 = this.testName.getMethodName() + "-1";
		final IProject project1 = AbstractProjectBasedTest.createMavenProject(projectName1);

		final String projectName2 = this.testName.getMethodName() + "-2";
		final IProject project2 = AbstractProjectBasedTest.createMavenProject(projectName2);

		this.preferences.setValue(MavenViewPreferences.NEVER_SELECTED_PROJECTS,
				projectName1 + MavenViewPreferences.SEPARATOR + projectName2);

		Assert.assertArrayEquals(new IProject[] { project1, project2 },
				MavenViewPreferences.getNeverSelectedProjects());
	}

	@Test
	public void testGetNeverSelectedProjectsNull() throws Exception {
		this.preferences.setToDefault(MavenViewPreferences.NEVER_SELECTED_PROJECTS);

		Assert.assertArrayEquals(new IProject[0], MavenViewPreferences.getNeverSelectedProjects());
	}

	@Test
	public void testSetNeverSelectedProject() throws Exception {
		final String projectName = this.testName.getMethodName();
		final IProject project = AbstractProjectBasedTest.createMavenProject(projectName);

		MavenViewPreferences.setNeverSelectedProjects(new IProject[] { project });

		Assert.assertEquals(projectName, this.preferences.getString(MavenViewPreferences.NEVER_SELECTED_PROJECTS));
	}

	@Test
	public void testSetNeverSelectedProjects() throws Exception {
		final String projectName1 = this.testName.getMethodName() + "-1";
		final IProject project1 = AbstractProjectBasedTest.createMavenProject(projectName1);

		final String projectName2 = this.testName.getMethodName() + "-2";
		final IProject project2 = AbstractProjectBasedTest.createMavenProject(projectName2);

		MavenViewPreferences.setNeverSelectedProjects(project1, project2);

		Assert.assertEquals(projectName1 + MavenViewPreferences.SEPARATOR + projectName2,
				this.preferences.getString(MavenViewPreferences.NEVER_SELECTED_PROJECTS));
	}

	@Test
	public void testSetNeverSelectedProjectsNull() throws Exception {
		MavenViewPreferences.setNeverSelectedProjects((IProject[]) null);

		Assert.assertEquals("", this.preferences.getString(MavenViewPreferences.NEVER_SELECTED_PROJECTS));
	}

	@Test
	public void testSetNeverSelectedProjectsEmpty() throws Exception {
		MavenViewPreferences.setNeverSelectedProjects(new IProject[0]);

		Assert.assertEquals("", this.preferences.getString(MavenViewPreferences.NEVER_SELECTED_PROJECTS));
	}
}
