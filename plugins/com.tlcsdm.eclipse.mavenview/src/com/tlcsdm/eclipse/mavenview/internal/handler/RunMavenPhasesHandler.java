package com.tlcsdm.eclipse.mavenview.internal.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.mavenview.MavenRunConfig;
import com.tlcsdm.eclipse.mavenview.MavenRunner;
import com.tlcsdm.eclipse.mavenview.MavenRunnerException;
import com.tlcsdm.eclipse.mavenview.Phase;
import com.tlcsdm.eclipse.mavenview.internal.Messages;
import com.tlcsdm.eclipse.mavenview.internal.ProfileSelectionManager;
import com.tlcsdm.eclipse.mavenview.internal.tree.LaunchConfigNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.PhaseNode;

public class RunMavenPhasesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);

		final PhaseNode[] selectedPhaseNodes = selection instanceof IStructuredSelection
				? Arrays.stream(((IStructuredSelection) selection).toArray()).filter(s -> s instanceof PhaseNode)
						.map(s -> (PhaseNode) s).toArray(PhaseNode[]::new)
				: new PhaseNode[0];
		final LaunchConfigNode[] selectedLaunchConfigNodes = selection instanceof IStructuredSelection
				? Arrays.stream(((IStructuredSelection) selection).toArray()).filter(s -> s instanceof LaunchConfigNode)
						.map(s -> (LaunchConfigNode) s).toArray(LaunchConfigNode[]::new)
				: new LaunchConfigNode[0];

		final Shell activeShell = HandlerUtil.getActiveShell(event);
		if (selectedPhaseNodes.length == 0 && selectedLaunchConfigNodes.length == 0) {
			MessageDialog.openError(activeShell, Messages.getString("SelectPhasesTitle"),
					Messages.getString("SelectPhases"));
		} else {
			runMavenPhases(activeShell, selectedPhaseNodes);
			runLaunchConfigs(selectedLaunchConfigNodes);
		}
		return null;
	}

	private static void runMavenPhases(Shell shell, PhaseNode[] phaseNodes) {
		final Map<IProject, List<PhaseNode>> projects = Arrays.stream(phaseNodes)
				.collect(Collectors.groupingBy(PhaseNode::getProject));

		final MavenRunConfig config = new MavenRunConfig();

		for (final Entry<IProject, List<PhaseNode>> project : projects.entrySet()) {
			try {
				final MavenRunConfig projectConfig = config.copy();
				projectConfig.setPhases(project.getValue().stream().map(PhaseNode::getPhase).toArray(Phase[]::new));
				
				// Apply selected profiles from ProfileSelectionManager
				final String[] selectedProfiles = ProfileSelectionManager.getSelectedProfiles(project.getKey());
				if (selectedProfiles != null && selectedProfiles.length > 0) {
					projectConfig.setProfiles(selectedProfiles);
				}

				final MavenRunner runner = new MavenRunner();
				runner.runForProject(project.getKey(), projectConfig);
			} catch (final MavenRunnerException e) {
				MessageDialog.openError(shell, Messages.getString("ErrorWhileRunningMaven"), e.getLocalizedMessage());
			}
		}
	}

	private static void runLaunchConfigs(LaunchConfigNode[] launchConfigNodes) {
		for (final LaunchConfigNode launchConfigNode : launchConfigNodes) {
			DebugUITools.launch(launchConfigNode.getLaunchConfig(), "run");
		}
	}
}
