package com.tlcsdm.eclipse.mavenview.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.mavenview.internal.tree.LaunchConfigNode;

public class OpenMavenSettingsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.getFirstElement() instanceof LaunchConfigNode launchNode) {
			if (launchNode.getLaunchConfig() != null) {
				Display.getDefault().asyncExec(() -> {
					Shell shell = window.getShell();
					DebugUITools.openLaunchConfigurationPropertiesDialog(shell, launchNode.getLaunchConfig(),
							IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
				});
			}
		}
		return null;
	}

}
