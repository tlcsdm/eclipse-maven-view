package com.tlcsdm.eclipse.mavenview;

import java.util.Objects;

import org.eclipse.swt.graphics.Image;

/**
 * Represents a Maven profile with its name and active status.
 */
public class Profile implements Displayable {

	private final String id;
	private final boolean activeByDefault;

	public Profile(String id, boolean activeByDefault) {
		this.id = Objects.requireNonNull(id);
		this.activeByDefault = activeByDefault;
	}

	public String getId() {
		return this.id;
	}

	public boolean isActiveByDefault() {
		return this.activeByDefault;
	}

	@Override
	public String getDisplayName() {
		return this.id;
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_PROFILE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final Profile that = (Profile) obj;
		return Objects.equals(this.id, that.id);
	}

	@Override
	public String toString() {
		return "Profile [id=" + this.id + ", activeByDefault=" + this.activeByDefault + "]";
	}
}
