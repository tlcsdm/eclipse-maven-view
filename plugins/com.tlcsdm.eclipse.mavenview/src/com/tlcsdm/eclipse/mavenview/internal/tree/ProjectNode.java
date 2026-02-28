package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenRunner;
import com.tlcsdm.eclipse.mavenview.internal.ProfileSelectionManager;
import com.tlcsdm.eclipse.mavenview.internal.common.SecureXmlParser;

public class ProjectNode implements Displayable, Parentable {

	private static final String WORKING_DIR_PREFIX = "${workspace_loc:/";
	private static final String WORKING_PROJECT_PREFIX = "${project_loc:";
	private static final String WORKING_DIR_SUFFIX = "}";

	private final IProject project;

	private final ILaunchConfiguration[] launchConfigs;

	public ProjectNode(IProject project) {
		this.project = Objects.requireNonNull(project);
		this.launchConfigs = readLaunchConfigs(project);
	}

	private static ILaunchConfiguration[] readLaunchConfigs(IProject project) {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType launchConfigurationType = Objects
				.requireNonNull(launchManager.getLaunchConfigurationType(MavenRunner.LAUNCH_CONFIGURATION_TYPE_ID));

		try {
			final ILaunchConfiguration[] launchConfigurations = launchManager
					.getLaunchConfigurations(launchConfigurationType);
			final String wkLocation = WORKING_DIR_PREFIX + project.getName() + WORKING_DIR_SUFFIX;
			final String projectLocation = WORKING_PROJECT_PREFIX + project.getName() + WORKING_DIR_SUFFIX;
			final List<ILaunchConfiguration> result = new ArrayList<>(launchConfigurations.length);

			for (final ILaunchConfiguration configuration : launchConfigurations) {
				final String workingDirectory = configuration.getAttribute(MavenRunner.ATTR_WORKING_DIRECTORY,
						(String) null);
				if (wkLocation.equals(workingDirectory) || projectLocation.equals(workingDirectory)) {
					result.add(configuration);
				}
			}
			return result.toArray(new ILaunchConfiguration[0]);
		} catch (final CoreException e) {
			// we can ignore that
			Activator.getDefault().getLog().warn("Failed to read launch configurations for project " + project.getName(), e);
			return new ILaunchConfiguration[0];
		}
	}

	@Override
	public String getDisplayName() {
		return this.project.getName();
	}

	public IProject getProjectResource() {
		return this.project;
	}

	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
	}

	@Override
	public Object[] getChildren() {
		// Build children dynamically in order: Profiles, Phases, Maven Plugins, Run Configurations, Dependencies
		// Only show nodes that have children (except Phases which is always shown)
		final List<Object> children = new ArrayList<>();

		// Check if project has profiles
		Profile[] availableProfiles = readAvailableProfiles(this.project);
		String[] selectedProfiles = ProfileSelectionManager.getSelectedProfiles(this.project);
		// If no user selection, use profiles active by default
		if (selectedProfiles.length == 0 && availableProfiles.length > 0) {
			selectedProfiles = getDefaultSelectedProfiles(availableProfiles);
			// Initialize ProfileSelectionManager with default selections
			ProfileSelectionManager.initializeDefaultProfiles(this.project, selectedProfiles);
		}
		boolean hasProfiles = availableProfiles != null && availableProfiles.length > 0;
		boolean hasPlugins = MavenPluginsNode.hasPlugins(this.project);
		boolean hasLaunchConfigs = this.launchConfigs.length > 0;
		boolean hasDependencies = DependenciesNode.hasDependencies(this.project);

		// If no profiles, plugins, launch configs, or dependencies, show phases directly (flat structure)
		if (!hasProfiles && !hasPlugins && !hasLaunchConfigs && !hasDependencies) {
			return PhaseNode.createDisplayed(this);
		}

		// Add nodes in order: Profiles, Phases, Maven Plugins, Run Configurations, Dependencies
		// 1. Profiles (only if has profiles)
		if (hasProfiles) {
			children.add(new ProfilesNode(this, availableProfiles, selectedProfiles));
		}

		// 2. Phases (always shown as a container node when at least one other node type exists)
		children.add(new PhasesNode(this));

		// 3. Maven Plugins (only if has plugins)
		if (hasPlugins) {
			children.add(new MavenPluginsNode(this));
		}

		// 4. Run Configurations (only if has launch configs)
		if (hasLaunchConfigs) {
			children.add(new LaunchConfigsNode(this, this.launchConfigs));
		}

		// 5. Dependencies (only if has dependencies)
		if (hasDependencies) {
			children.add(new DependenciesNode(this));
		}

		return children.toArray(new Object[0]);
	}

	private static Profile[] readAvailableProfiles(IProject project) {
		try {
			// Use M2E to get the Maven project facade which includes effective model
			final IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
			final IFile pomFile = project.getFile(new Path(MavenRunner.POM_FILE_NAME));
			final IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, new NullProgressMonitor());

			if (projectFacade == null) {
				return new Profile[0];
			}

			// Use reflection to access MavenProject.getModel().getProfiles() without direct
			// API access
			// This approach allows accessing Maven model data without adding Maven
			// dependencies
			// that might conflict with M2E's embedded Maven version, avoiding access
			// restriction errors
			try {
				final Object mavenProject = projectFacade.getMavenProject(new NullProgressMonitor());
				if (mavenProject == null) {
					return new Profile[0];
				}

				// Use reflection to call getModel()
				final java.lang.reflect.Method getModelMethod = mavenProject.getClass().getMethod("getModel");
				final Object model = getModelMethod.invoke(mavenProject);
				if (model == null) {
					return new Profile[0];
				}

				// Use reflection to call getProfiles()
				final java.lang.reflect.Method getProfilesMethod = model.getClass().getMethod("getProfiles");
				@SuppressWarnings("unchecked")
				final java.util.List<Object> profiles = (java.util.List<Object>) getProfilesMethod.invoke(model);

				if (profiles == null || profiles.isEmpty()) {
					return new Profile[0];
				}

				final List<Profile> result = new ArrayList<>();
				for (Object profileObj : profiles) {
					// Use reflection to get profile ID
					final java.lang.reflect.Method getIdMethod = profileObj.getClass().getMethod("getId");
					final String profileId = (String) getIdMethod.invoke(profileObj);

					if (profileId == null || profileId.trim().isEmpty()) {
						continue;
					}

					// Use reflection to check if active by default
					boolean activeByDefault = false;
					final java.lang.reflect.Method getActivationMethod = profileObj.getClass()
							.getMethod("getActivation");
					final Object activation = getActivationMethod.invoke(profileObj);
					if (activation != null) {
						final java.lang.reflect.Method isActiveByDefaultMethod = activation.getClass()
								.getMethod("isActiveByDefault");
						activeByDefault = Boolean.TRUE.equals(isActiveByDefaultMethod.invoke(activation));
					}

					result.add(new Profile(profileId, activeByDefault));
				}

				return result.toArray(new Profile[0]);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				// Reflection failed, fall back to XML parsing of local pom.xml
				Activator
						.getDefault().getLog().warn(
								"Maven reflection API access failed for project " + project.getName()
										+ ", falling back to XML parsing. This may not include parent POM profiles.",
								e);
				return readProfilesFromXml(pomFile);
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().error("Failed to read Maven profiles for project " + project.getName(), e);
		}
		return new Profile[0];
	}

	private static Profile[] readProfilesFromXml(IFile pomFile) {
		try {
			if (pomFile == null || !pomFile.exists()) {
				return new Profile[0];
			}

			final javax.xml.parsers.DocumentBuilder builder = SecureXmlParser.createSecureDocumentBuilder();
			final org.w3c.dom.Document document = builder.parse(pomFile.getContents());

			// Get all profile elements
			final org.w3c.dom.NodeList profileNodes = document.getElementsByTagName("profile");
			if (profileNodes.getLength() == 0) {
				return new Profile[0];
			}

			final List<Profile> result = new ArrayList<>();
			for (int i = 0; i < profileNodes.getLength(); i++) {
				final org.w3c.dom.Element profileElement = (org.w3c.dom.Element) profileNodes.item(i);

				// Get profile id
				final org.w3c.dom.NodeList idNodes = profileElement.getElementsByTagName("id");
				if (idNodes.getLength() == 0) {
					continue;
				}
				final String profileId = idNodes.item(0).getTextContent().trim();

				// Skip profiles with empty IDs
				if (profileId.isEmpty()) {
					continue;
				}

				// Check if profile is active by default
				boolean activeByDefault = false;
				final org.w3c.dom.NodeList activationNodes = profileElement.getElementsByTagName("activation");
				if (activationNodes.getLength() > 0) {
					final org.w3c.dom.Element activationElement = (org.w3c.dom.Element) activationNodes.item(0);
					final org.w3c.dom.NodeList activeByDefaultNodes = activationElement
							.getElementsByTagName("activeByDefault");
					if (activeByDefaultNodes.getLength() > 0) {
						final String activeByDefaultValue = activeByDefaultNodes.item(0).getTextContent().trim();
						activeByDefault = "true".equalsIgnoreCase(activeByDefaultValue);
					}
				}

				result.add(new Profile(profileId, activeByDefault));
			}

			return result.toArray(new Profile[0]);
		} catch (Exception e) {
			Activator.getDefault().getLog().error("Failed to parse POM XML for profiles", e);
		}
		return new Profile[0];
	}

	private static String[] getDefaultSelectedProfiles(Profile[] availableProfiles) {
		final List<String> result = new ArrayList<>();
		for (Profile profile : availableProfiles) {
			if (profile.isActiveByDefault()) {
				result.add(profile.getId());
			}
		}
		return result.toArray(new String[0]);
	}

	@Override
	public int hashCode() {
		return 7 * Objects.hash(this.project);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final ProjectNode that = (ProjectNode) obj;
		if (!Objects.equals(this.project.getName(), that.project.getName()))
			return false;
		return true;
	}
}