package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenRunner;

public class ProjectNode implements Displayable, Parentable {

	private static final String WORKING_DIR_PREFIX = "${workspace_loc:/";
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
			final String projectLocation = WORKING_DIR_PREFIX + project.getName() + WORKING_DIR_SUFFIX;
			final List<ILaunchConfiguration> result = new ArrayList<>(launchConfigurations.length);

			for (final ILaunchConfiguration configuration : launchConfigurations) {
				final String workingDirectory = configuration.getAttribute(MavenRunner.ATTR_WORKING_DIRECTORY,
						(String) null);
				if (projectLocation.equals(workingDirectory)) {
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
		if (this.launchConfigs.length > 0) {
			final Object[] children = new Object[this.launchConfigs.length + 1];
			for (int i = 0; i < this.launchConfigs.length; i++) {
				children[i] = new LaunchConfigNode(this.launchConfigs[i]);
			}
			children[children.length - 1] = new PhasesNode(this);
			return children;
		}
		return PhaseNode.createDisplayed(this);
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
