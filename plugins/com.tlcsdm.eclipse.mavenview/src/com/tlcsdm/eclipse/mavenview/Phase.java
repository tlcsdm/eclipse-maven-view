package com.tlcsdm.eclipse.mavenview;

import org.eclipse.swt.graphics.Image;

public enum Phase implements Displayable {
	CLEAN,

	VALIDATE,

	COMPILE,

	TEST,

	PACKAGE,

	VERIFY,

	INSTALL,

	DEPLOY,

	SITE,

	SITE_DEPLOY,

	;

	@Override
	public String getDisplayName() {
		return name().toLowerCase().replace("_", "-");
	}

	@Override
	public Image getImage() {
		switch (this) {
		case CLEAN:
			return Activator.getImage(MavenViewImages.OBJ_PHASE_CLEAN);

		case SITE:
		case SITE_DEPLOY:
			return Activator.getImage(MavenViewImages.OBJ_PHASE_SITE);

		default:
			return Activator.getImage(MavenViewImages.OBJ_PHASE);
		}

	}

}
