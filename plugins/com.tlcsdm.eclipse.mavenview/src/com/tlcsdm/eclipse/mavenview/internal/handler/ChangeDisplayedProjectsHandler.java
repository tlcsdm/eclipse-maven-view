package com.tlcsdm.eclipse.mavenview.internal.handler;

import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.mavenview.InitialProjectSelection;
import com.tlcsdm.eclipse.mavenview.MavenView;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;
import com.tlcsdm.eclipse.mavenview.internal.Messages;
import com.tlcsdm.eclipse.mavenview.internal.common.SelectElementsFromViewerDialog;

public class ChangeDisplayedProjectsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final SelectElementsFromViewerDialog<IProject> dialog = new SelectElementsFromViewerDialog<>(
				HandlerUtil.getActiveShell(event));
		dialog.setElements(InitialProjectSelection.fetchAllMavenProjects());
		dialog.setMessage(Messages.getString("ChangeDisplayedProjectsMessage"));
		dialog.setTitle(Messages.getString("ChangeDisplayedProjectsTitle"));
		dialog.setToStringFunction(IProject::getName);
		dialog.selectElements(MavenViewPreferences.getAlwaysSelectedProjects(), Boolean.TRUE);
		dialog.selectElements(MavenViewPreferences.getNeverSelectedProjects(), Boolean.FALSE);

		if (dialog.open() == Window.OK) {
			MavenViewPreferences.setAlwaysSelectedProjects(Arrays.stream(dialog.getSelectedElements(Boolean.TRUE))
					.map(o -> (IProject) o).toArray(IProject[]::new));
			MavenViewPreferences.setNeverSelectedProjects(Arrays.stream(dialog.getSelectedElements(Boolean.FALSE))
					.map(o -> (IProject) o).toArray(IProject[]::new));

			IWorkbenchPage page = HandlerUtil.getActiveSite(event).getPage();

			for (IViewReference ref : page.getViewReferences()) {
				IViewPart view = ref.getView(false);
				if (view instanceof MavenView) {
					((MavenView) view).refresh();
				}
			}
		}
		return null;
	}

}
