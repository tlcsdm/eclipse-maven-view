package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenRunner;
import com.tlcsdm.eclipse.mavenview.Profile;
import com.tlcsdm.eclipse.mavenview.ProfileSelectionManager;

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
			return result.toArray(new ILaunchConfiguration[result.size()]);
		} catch (final CoreException e) {
			// we can ignore that
			System.err.println(e.getMessage());
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
		// Build children dynamically: optionally ProfilesNode, PhasesNode and launch
		// configs
		final List<Object> children = new ArrayList<>();

		// add profiles node if project has profiles
		Profile[] availableProfiles = readAvailableProfiles(this.project);
		String[] selectedProfiles = ProfileSelectionManager.getSelectedProfiles(this.project);
		// If no user selection, use profiles active by default
		if (selectedProfiles.length == 0 && availableProfiles.length > 0) {
			selectedProfiles = getDefaultSelectedProfiles(availableProfiles);
			// Initialize ProfileSelectionManager with default selections
			ProfileSelectionManager.initializeDefaultProfiles(this.project, selectedProfiles);
		}
		if (availableProfiles != null && availableProfiles.length > 0) {
			children.add(new ProfilesNode(this, availableProfiles, selectedProfiles));
		}

		// phases
		children.add(new PhasesNode(this));

		// launch configs
		if (this.launchConfigs.length > 0) {
			children.add(new launchConfigsNode(this, this.launchConfigs));
		}
		if (this.launchConfigs.length == 0) {
			return PhaseNode.createDisplayed(this);
		}

		return children.toArray(new Object[children.size()]);
	}

	private static Profile[] readAvailableProfiles(IProject project) {
		try {
			final IFile pomFile = project.getFile(new Path(MavenRunner.POM_FILE_NAME));
			if (pomFile == null || !pomFile.exists()) {
				return new Profile[0];
			}
			
			// Parse pom.xml using DOM parser to avoid restricted Maven API
			final javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
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
				
				// Check if profile is active by default
				boolean activeByDefault = false;
				final org.w3c.dom.NodeList activationNodes = profileElement.getElementsByTagName("activation");
				if (activationNodes.getLength() > 0) {
					final org.w3c.dom.Element activationElement = (org.w3c.dom.Element) activationNodes.item(0);
					final org.w3c.dom.NodeList activeByDefaultNodes = activationElement.getElementsByTagName("activeByDefault");
					if (activeByDefaultNodes.getLength() > 0) {
						final String activeByDefaultValue = activeByDefaultNodes.item(0).getTextContent().trim();
						activeByDefault = "true".equalsIgnoreCase(activeByDefaultValue);
					}
				}
				
				result.add(new Profile(profileId, activeByDefault));
			}
			
			return result.toArray(new Profile[result.size()]);
		} catch (Exception e) {
			e.printStackTrace();
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
		return result.toArray(new String[result.size()]);
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