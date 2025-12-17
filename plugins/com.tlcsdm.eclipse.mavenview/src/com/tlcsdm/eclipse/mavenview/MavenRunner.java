package com.tlcsdm.eclipse.mavenview;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.IProjectConfiguration;

/**
 * This class is able to run a Maven process pretty similarly to how M2E does
 * it. Heavily inspired by <code>org.eclipse.m2e.actions.ExecutePomAction</code>
 * (which is not public API).
 */

public class MavenRunner {

	// Constants from org.eclipse.m2e.actions.MavenLaunchConstants
	public final static String LAUNCH_CONFIGURATION_TYPE_ID = "org.eclipse.m2e.Maven2LaunchConfigurationType"; //$NON-NLS-1$
	private final static String ATTR_GOALS = "M2_GOALS"; //$NON-NLS-1$
	private final static String ATTR_PROFILES = "M2_PROFILES"; //$NON-NLS-1$
	private final static String ATTR_SKIP_TESTS = "M2_SKIP_TESTS";
	public final static String ATTR_WORKING_DIRECTORY = IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY;

	// Constants from org.eclipse.m2e.core.internalIMavenConstants
	public final static String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

	// Misc constants
	public final static String ATTR_CONFIG = "com.tlcsdm.eclipse.mavenview.config"; //$NON-NLS-1$

	private final String mode = "run";

	private final ILaunchManager launchManager;
	private final ILaunchConfigurationType launchConfigurationType;

	public MavenRunner() {
		this.launchManager = DebugPlugin.getDefault().getLaunchManager();
		this.launchConfigurationType = Objects.requireNonNull(
				this.launchManager.getLaunchConfigurationType(MavenRunner.LAUNCH_CONFIGURATION_TYPE_ID));
	}

	public void runForProject(IProject project, MavenRunConfig config) throws MavenRunnerException {
		Objects.requireNonNull(project, "Define the project to run from!");
		Objects.requireNonNull(config, "Define the config to run!");

		final IContainer baseDir = project;

		try {
			final ILaunchConfiguration launchConfiguration = findOrCreateLaunchConfiguration(baseDir, config);
			if (launchConfiguration != null) {
				DebugUITools.launch(launchConfiguration, this.mode);
			}
		} catch (final CoreException e) {
			throw new MavenRunnerException(MessageFormat.format(Messages.getString("CannotExecuteOnProjectPattern"),
					project.getName(), config.toGoalString()), e);
		}
	}

	private ILaunchConfiguration findOrCreateLaunchConfiguration(IContainer baseDir, MavenRunConfig config)
			throws CoreException {
		final List<ILaunchConfiguration> existingConfigs = findExistingLaunchConfigurations(config);

		if (existingConfigs.isEmpty())
			return createLaunchConfiguration(baseDir, config);

		return existingConfigs.get(0);
	}

	private List<ILaunchConfiguration> findExistingLaunchConfigurations(MavenRunConfig config) throws CoreException {
		final List<ILaunchConfiguration> result = new ArrayList<>();

		final ILaunchConfiguration[] launchConfigurations = this.launchManager
				.getLaunchConfigurations(this.launchConfigurationType);
		for (final ILaunchConfiguration configuration : launchConfigurations) {
			if (config.equals(configuration.getAttributes().get(MavenRunner.ATTR_CONFIG))) {
				result.add(configuration);
			}
		}
		return result;
	}

	private ILaunchConfiguration createLaunchConfiguration(IContainer basedir, MavenRunConfig config)
			throws CoreException {

		final String goals = config.toGoalString();
		final String rawConfigName = MessageFormat.format(Messages.getString("ExecutingInPathPattern"), goals,
				basedir.getLocation().toString());
		final String safeConfigName = this.launchManager.generateLaunchConfigurationName(rawConfigName);

		final ILaunchConfigurationWorkingCopy workingCopy = this.launchConfigurationType.newInstance(null,
				safeConfigName);
		workingCopy.setAttribute(ATTR_WORKING_DIRECTORY, basedir.getLocation().toOSString());
		workingCopy.setAttribute(MavenRunner.ATTR_GOALS, goals);
		workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
		workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_SCOPE, "${project}"); //$NON-NLS-1$
		workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_RECURSIVE, true);
		boolean skipTests = Activator.getDefault().getPreferenceStore().getBoolean(MavenViewPreferences.SKIP_TESTS);
		workingCopy.setAttribute(MavenRunner.ATTR_SKIP_TESTS, skipTests);
		workingCopy.setAttribute(ATTR_CONFIG, config);

		setProjectConfiguration(workingCopy, basedir);

		final IPath path = getJreContainerPath(basedir);
		if (path != null) {
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
					path.toPortableString());
		}

		return workingCopy;
	}

	private static void setProjectConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IContainer basedir) {
		final IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		final IFile pomFile = basedir.getFile(new Path(POM_FILE_NAME));
		final IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, new NullProgressMonitor());
		if (projectFacade != null) {
			final IProjectConfiguration configuration = projectFacade.getConfiguration();
			final String selectedProfiles = configuration.getSelectedProfiles();
			if (selectedProfiles != null && selectedProfiles.length() > 0) {
				workingCopy.setAttribute(ATTR_PROFILES, selectedProfiles);
			}
		}
	}

	private static IPath getJreContainerPath(IContainer basedir) throws CoreException {
		final IProject project = basedir.getProject();
		if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
			final IJavaProject javaProject = JavaCore.create(project);
			final IClasspathEntry[] entries = javaProject.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				final IClasspathEntry entry = entries[i];
				if (JavaRuntime.JRE_CONTAINER.equals(entry.getPath().segment(0)))
					return entry.getPath();
			}
		}
		return null;
	}
}
