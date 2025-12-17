package com.tlcsdm.eclipse.mavenview.internal.tree;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

public class MavenViewAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == PhaseNode.class && adaptableObject instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) adaptableObject).getFirstElement();
			if (firstElement instanceof PhaseNode) {
				return (T) firstElement;
			}
		}
		if (adapterType == LaunchConfigNode.class && adaptableObject instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) adaptableObject).getFirstElement();
			if (firstElement instanceof LaunchConfigNode) {
				return (T) firstElement;
			}
		}

		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { PhaseNode.class, LaunchConfigNode.class };
	}
}