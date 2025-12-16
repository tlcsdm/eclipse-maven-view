package com.tlcsdm.eclipse.mavenview.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.mavenview.MavenView;

public class RefreshHandler extends AbstractHandler {

	// TODO: ChangeDisplayedProjectsHandler refreshes, too, so maybe this should be
	// the same code

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final MavenView mavenView = findMavenView(event);
		if (mavenView != null) {
			mavenView.refresh();
		}
		return null;
	}

	static MavenView findMavenView(ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof MavenView)
			return (MavenView) activePart;

		for (final IViewReference viewReference : HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
				.getViewReferences()) {
			if (viewReference.getId().equals(MavenView.ID))
				return (MavenView) viewReference.getView(false);
		}
		throw new IllegalArgumentException("Cannot refresh view " + activePart + "!");
	}

}
