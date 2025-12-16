package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;
import com.tlcsdm.eclipse.mavenview.Phase;

public class PhaseNode implements Displayable {

	static PhaseNode[] createDisplayed(ProjectNode mavenProject) {
		return Arrays.stream(MavenViewPreferences.getDisplayedPhases()).map(phase -> new PhaseNode(mavenProject, phase))
				.toArray(PhaseNode[]::new);
	}

	private final ProjectNode mavenProject;
	private final Phase phase;

	public PhaseNode(ProjectNode mavenProject, Phase phase) {
		this.mavenProject = Objects.requireNonNull(mavenProject);
		this.phase = Objects.requireNonNull(phase);
	}

	public Phase getPhase() {
		return this.phase;
	}

	public IProject getProject() {
		return this.mavenProject.getProjectResource();
	}

	@Override
	public String getDisplayName() {
		return this.phase.getDisplayName();
	}

	@Override
	public Image getImage() {
		return this.phase.getImage();
	}

	@Override
	public int hashCode() {
		return 3 * Objects.hash(this.mavenProject, this.phase);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final PhaseNode that = (PhaseNode) obj;
		if (!Objects.equals(this.mavenProject, that.mavenProject))
			return false;
		if (this.phase != that.phase)
			return false;
		return true;
	}

}
