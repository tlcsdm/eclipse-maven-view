package com.tlcsdm.eclipse.mavenview.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.mavenview.MavenView;
import com.tlcsdm.eclipse.mavenview.internal.tree.Parentable;

public class ExpandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final MavenView mavenView = RefreshHandler.findMavenView(event);
		if (mavenView != null) {
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				for (Object element : structuredSelection.toList()) {
					if (element instanceof Parentable) {
						mavenView.expand(element);
					}
				}
			}
		}
		return null;
	}

}
