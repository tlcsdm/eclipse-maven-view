package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.Objects;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewImages;

public class LaunchConfigNode implements Displayable {

	private final ILaunchConfiguration launchConfig;

	public LaunchConfigNode(ILaunchConfiguration launchConfig) {
		this.launchConfig = Objects.requireNonNull(launchConfig);
	}

	public ILaunchConfiguration getLaunchConfig() {
		return this.launchConfig;
	}

	@Override
	public String getDisplayName() {
		return this.launchConfig.getName();
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_MAVEN);
	}

}
