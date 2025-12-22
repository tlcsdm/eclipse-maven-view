package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewImages;

/**
 * Represents a single Maven plugin in the tree.
 * Shows the plugin name and coordinates (groupId:artifactId:version) as a styled label.
 */
public class MavenPluginNode implements Displayable, Parentable {

	private final ProjectNode mavenProject;
	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String prefix;

	public MavenPluginNode(ProjectNode mavenProject, String groupId, String artifactId, String version, String prefix) {
		this.mavenProject = mavenProject;
		this.groupId = Objects.requireNonNull(groupId);
		this.artifactId = Objects.requireNonNull(artifactId);
		this.version = version != null ? version : "";
		this.prefix = prefix != null ? prefix : artifactId;
	}

	@Override
	public String getDisplayName() {
		return this.prefix;
	}

	/**
	 * Get the coordinates string to be displayed in gray: (groupId:artifactId:version)
	 */
	public String getCoordinates() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(groupId).append(":").append(artifactId);
		if (version != null && !version.isEmpty()) {
			sb.append(":").append(version);
		}
		sb.append(")");
		return sb.toString();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getPrefix() {
		return prefix;
	}

	public IProject getProject() {
		if (mavenProject != null) {
			return mavenProject.getProjectResource();
		}
		return null;
	}

	public ProjectNode getMavenProject() {
		return mavenProject;
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_PLUGIN);
	}

	@Override
	public Object[] getChildren() {
		// Return common Maven plugin goals based on the plugin
		return MavenPluginGoalNode.createGoals(this);
	}

	@Override
	public int hashCode() {
		return 17 * Objects.hash(mavenProject, groupId, artifactId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		MavenPluginNode other = (MavenPluginNode) obj;
		return Objects.equals(mavenProject, other.mavenProject)
				&& Objects.equals(groupId, other.groupId)
				&& Objects.equals(artifactId, other.artifactId);
	}
}
