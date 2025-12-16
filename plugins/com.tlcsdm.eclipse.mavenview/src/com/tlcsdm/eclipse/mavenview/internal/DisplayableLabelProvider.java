package com.tlcsdm.eclipse.mavenview.internal;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Displayable;

public class DisplayableLabelProvider extends LabelProvider {

	@Override
	public String getText(Object obj) {
		return obj instanceof Displayable ? ((Displayable) obj).getDisplayName() : null;
	}

	@Override
	public Image getImage(Object obj) {
		return obj instanceof Displayable ? ((Displayable) obj).getImage() : null;
	}
}
