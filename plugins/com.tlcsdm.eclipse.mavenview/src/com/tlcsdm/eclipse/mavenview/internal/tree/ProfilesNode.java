package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewImages;
import com.tlcsdm.eclipse.mavenview.Profile;

/**
 * Represents the "Profiles" node in the tree view that contains all profile children.
 */
public class ProfilesNode implements Displayable, Parentable {

	private final ProjectNode projectNode;
	private final Profile[] profiles;
	private final String[] selectedProfiles;

	public ProfilesNode(ProjectNode projectNode, Profile[] profiles, String[] selectedProfiles) {
		this.projectNode = Objects.requireNonNull(projectNode);
		this.profiles = Objects.requireNonNull(profiles);
		this.selectedProfiles = selectedProfiles != null ? selectedProfiles : new String[0];
	}

	@Override
	public String getDisplayName() {
		return Messages.getString("Profiles");
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_PROFILE);
	}

	@Override
	public Object[] getChildren() {
		final ProfileNode[] children = new ProfileNode[this.profiles.length];
		for (int i = 0; i < this.profiles.length; i++) {
			boolean selected = isProfileSelected(this.profiles[i].getId());
			children[i] = new ProfileNode(this.projectNode, this.profiles[i], selected);
		}
		return children;
	}

	private boolean isProfileSelected(String profileId) {
		for (String selectedProfile : this.selectedProfiles) {
			if (selectedProfile.equals(profileId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 13 * Objects.hash(this.projectNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final ProfilesNode that = (ProfilesNode) obj;
		if (!Objects.equals(this.projectNode, that.projectNode))
			return false;
		return true;
	}
}
