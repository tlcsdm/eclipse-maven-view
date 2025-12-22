package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.tlcsdm.eclipse.mavenview.Displayable;

/**
 * Represents a Maven dependency in the tree view.
 */
public class DependencyNode implements Displayable {

	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String scope;

	public DependencyNode(String groupId, String artifactId, String version, String scope) {
		this.groupId = Objects.requireNonNull(groupId);
		this.artifactId = Objects.requireNonNull(artifactId);
		this.version = version != null ? version : "";
		this.scope = scope;
	}

	@Override
	public String getDisplayName() {
		return groupId + ":" + artifactId + ":" + version;
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

	public String getScope() {
		return scope;
	}

	/**
	 * Returns true if this dependency has test scope.
	 */
	public boolean isTestScope() {
		return "test".equalsIgnoreCase(scope);
	}

	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, artifactId, version, scope);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		DependencyNode other = (DependencyNode) obj;
		return Objects.equals(groupId, other.groupId) && Objects.equals(artifactId, other.artifactId)
				&& Objects.equals(version, other.version) && Objects.equals(scope, other.scope);
	}
}
