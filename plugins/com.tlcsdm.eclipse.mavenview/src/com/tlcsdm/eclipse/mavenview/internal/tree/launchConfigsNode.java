package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Objects;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewImages;

public class launchConfigsNode implements Displayable, Parentable {

	private final ProjectNode mavenProject;
	private final ILaunchConfiguration[] launchConfigs;

	public launchConfigsNode(ProjectNode mavenProject, ILaunchConfiguration[] launchConfigs) {
		this.mavenProject = Objects.requireNonNull(mavenProject);
		this.launchConfigs = Objects.requireNonNull(launchConfigs);
	}

	@Override
	public String getDisplayName() {
		return Messages.getString("launchConfigs");
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_SETTINGS);
	}

	@Override
	public Object[] getChildren() {
		final Object[] children = new Object[this.launchConfigs.length];
		for (int i = 0; i < this.launchConfigs.length; i++) {
			children[i] = new LaunchConfigNode(this.launchConfigs[i]);
		}
		return children;
	}

	@Override
	public int hashCode() {
		return 5 * Objects.hash(this.mavenProject) + 7 * Objects.hash((Object[]) this.launchConfigs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final launchConfigsNode that = (launchConfigsNode) obj;
		if (!Objects.equals(this.mavenProject, that.mavenProject)
				&& !Objects.deepEquals(this.launchConfigs, that.launchConfigs))
			return false;
		return true;
	}
}
