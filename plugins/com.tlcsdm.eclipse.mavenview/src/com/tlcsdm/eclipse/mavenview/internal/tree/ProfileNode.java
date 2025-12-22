package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.Profile;

/**
 * Represents a Maven profile node in the tree view.
 */
public class ProfileNode implements Displayable {

	private final ProjectNode projectNode;
	private final Profile profile;
	private boolean selected;

	public ProfileNode(ProjectNode projectNode, Profile profile, boolean selected) {
		this.projectNode = Objects.requireNonNull(projectNode);
		this.profile = Objects.requireNonNull(profile);
		this.selected = selected;
	}

	public Profile getProfile() {
		return this.profile;
	}

	public IProject getProject() {
		return this.projectNode.getProjectResource();
	}

	public boolean isSelected() {
		return this.selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String getDisplayName() {
		return this.profile.getDisplayName();
	}

	@Override
	public Image getImage() {
		// Return different images based on selection state
		if (this.selected) {
			return Activator.getImage("icons/profile_checked.png");
		} else {
			return Activator.getImage("icons/profile_unchecked.png");
		}
	}

	@Override
	public int hashCode() {
		return 11 * Objects.hash(this.projectNode, this.profile);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final ProfileNode that = (ProfileNode) obj;
		if (!Objects.equals(this.projectNode, that.projectNode))
			return false;
		if (!Objects.equals(this.profile, that.profile))
			return false;
		return true;
	}
}
