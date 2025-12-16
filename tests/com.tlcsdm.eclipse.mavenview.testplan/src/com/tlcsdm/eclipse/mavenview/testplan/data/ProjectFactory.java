package com.tlcsdm.eclipse.mavenview.testplan.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import com.tlcsdm.eclipse.mavenview.MavenRunConfig;
import com.tlcsdm.eclipse.mavenview.testplan.constants.CommonConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.NewProjectConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.WorkbenchConstants;

public class ProjectFactory {

	private final SWTWorkbenchBot bot;

	private final List<Runnable> tearDowns = new ArrayList<>();

	public ProjectFactory(SWTWorkbenchBot bot) {
		this.bot = Objects.requireNonNull(bot);
	}

	public final void dispose() {
		this.tearDowns.forEach(Runnable::run);
		this.tearDowns.clear();
	}

	public IProject createMavenProjectViaDialog(MavenGav gav) {
		this.tearDowns.add(this::clearNewProjectDialog); // just in case

		try {
			return doCreateMavenProjectViaDialog(gav);
		} catch (final WidgetNotFoundException e) {
			// try again if necessary
			return doCreateMavenProjectViaDialog(gav);
		}

	}

	private IProject doCreateMavenProjectViaDialog(MavenGav gav) {

		this.bot.menu(WorkbenchConstants.MENU_FILE).menu(WorkbenchConstants.SUB_MENU_NEW)
				.menu(WorkbenchConstants.COMMAND_OTHER).click();
		this.bot.waitUntil(Conditions.shellIsActive(NewProjectConstants.DIALOG_TITLE));

		try {
			this.bot.tree().getTreeItem(NewProjectConstants.GROUP_MAVEN).expand();
			this.bot.tree().getTreeItem(NewProjectConstants.GROUP_MAVEN).getNode(NewProjectConstants.PROJECT_MAVEN)
					.select();
		} catch (final WidgetNotFoundException e) {
			System.out.println("ProjectFactory.createMavenProjectViaDialog(New Dialog Contents)");
			printTreeItems(this.bot.tree().getAllItems(), 0);
			throw e;
		}

		this.bot.button(CommonConstants.BUTTON_NEXT).click();

		this.bot.checkBox(NewProjectConstants.MAVEN_SIMPLE_PROJECT).select();

		this.bot.button(CommonConstants.BUTTON_NEXT).click();

		this.bot.comboBoxWithLabelInGroup(NewProjectConstants.MAVEN_GROUP_ID, NewProjectConstants.MAVEN_GROUP_ARTIFACT)
				.setText(gav.groupId);
		this.bot.comboBoxWithLabelInGroup(NewProjectConstants.MAVEN_ARTIFACT_ID,
				NewProjectConstants.MAVEN_GROUP_ARTIFACT).setText(gav.artifactId);
		this.bot.comboBoxWithLabelInGroup(NewProjectConstants.MAVEN_VERSION, NewProjectConstants.MAVEN_GROUP_ARTIFACT)
				.setText(gav.version);
		this.bot.comboBoxWithLabelInGroup(NewProjectConstants.MAVEN_TYPE, NewProjectConstants.MAVEN_GROUP_ARTIFACT)
				.setText(gav.type);

		this.bot.button(CommonConstants.BUTTON_FINISH).click();

		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(gav.artifactId);
		addProjectDeletionToTearDown(project);

		clearDiscoverM2ConnectorsDialogIfNecessary();

		return project;
	}

	private void printTreeItems(SWTBotTreeItem[] swtBotTreeItems, int indent) {
		for (final SWTBotTreeItem swtBotTreeItem : swtBotTreeItems) {
			System.out.println(String.join("", Collections.nCopies(indent, "  ")) + swtBotTreeItem.getText());
			printTreeItems(swtBotTreeItem.getItems(), indent + 1);
		}
	}

	private void addProjectDeletionToTearDown(final IProject project) {
		this.tearDowns.add(() -> {
			try {
				project.delete(false, false, null);
			} catch (final CoreException e) {
				System.err.println(e.getMessage()); // ignore
			}
		});
	}

	private void clearNewProjectDialog() {
		final SWTBotShell activeShell = this.bot.activeShell();
		if (NewProjectConstants.MAVEN_DISCOVER_M2E_CONNECTORS.equals(activeShell.getText())) {
			activeShell.close();
		}
	}

	private void clearDiscoverM2ConnectorsDialogIfNecessary() {
		final long originalTimeout = SWTBotPreferences.TIMEOUT;
		SWTBotPreferences.TIMEOUT = 2000;
		try {
			this.bot.waitUntil(Conditions.shellIsActive(NewProjectConstants.MAVEN_DISCOVER_M2E_CONNECTORS));
			clearDiscoverM2ConnectorsDialog();
		} catch (final TimeoutException ignoredException) {
			// ignore
		} finally {
			SWTBotPreferences.TIMEOUT = originalTimeout;
		}
	}

	private void clearDiscoverM2ConnectorsDialog() {
		final SWTBotShell activeShell = this.bot.activeShell();
		if (NewProjectConstants.MAVEN_DISCOVER_M2E_CONNECTORS.equals(activeShell.getText())) {
			activeShell.close();
		}
	}

	public IProject[] createMavenProjectWithModulesViaDialog(MavenGav gav, String... modules) {
		final List<IProject> result = new ArrayList<>();

		final IProject parentProject = createMavenProjectViaDialog(gav.type("pom"));
		result.add(parentProject);

		final SWTBotView projectExplorer = WorkbenchView.PROJECT_EXPLORER.openViaDialog(this.bot);

		for (final String module : modules) {
			projectExplorer.bot().tree().getTreeItem(parentProject.getName()).select();

			this.bot.menu(WorkbenchConstants.MENU_FILE).menu(WorkbenchConstants.SUB_MENU_NEW)
					.menu(WorkbenchConstants.COMMAND_OTHER).click();
			this.bot.waitUntil(Conditions.shellIsActive(NewProjectConstants.DIALOG_TITLE));

			this.bot.tree().getTreeItem(NewProjectConstants.GROUP_MAVEN).expand();
			this.bot.tree().getTreeItem(NewProjectConstants.GROUP_MAVEN)
					.getNode(NewProjectConstants.PROJECT_MAVEN_MODULE).select();

			this.bot.button(CommonConstants.BUTTON_NEXT).click();

			this.bot.checkBox(NewProjectConstants.MAVEN_SIMPLE_PROJECT).select();
			this.bot.comboBoxWithLabel(NewProjectConstants.MAVEN_MODULE_NAME).setText(module);

			this.bot.button(CommonConstants.BUTTON_FINISH).click();

			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final IProject project = root.getProject(module);
			addProjectDeletionToTearDown(project);
			result.add(project);

			clearDiscoverM2ConnectorsDialogIfNecessary();
		}

		return result.toArray(new IProject[result.size()]);
	}

	public void createMavenLaunchConfig(IProject project, String mavenLaunchConfigName, MavenRunConfig config) {
		final SWTBotView projectExplorer = WorkbenchView.PROJECT_EXPLORER.openViaDialog(this.bot);

		final SWTBotTree projectTree = projectExplorer.bot().tree();
		projectTree.getTreeItem(project.getName()).expand();
		projectTree.getTreeItem(project.getName()).getNode("pom.xml").select();

		projectTree.contextMenu().menu("Run As", "2 Maven build...").click();

		this.bot.waitUntil(Conditions.shellIsActive("Edit Configuration"));
		final SWTBotShell editConfigurationShell = this.bot.activeShell();
		try {
			this.bot.textWithLabel("Name:").setText(mavenLaunchConfigName);
			this.bot.textWithLabel("Goals:").setText(config.getPhasesAsString());

			this.bot.button(CommonConstants.BUTTON_APPLY).click();
			this.bot.button(CommonConstants.BUTTON_CLOSE).click();
		} finally {
			if (editConfigurationShell.isActive()) {
				editConfigurationShell.close();
			}
		}
	}
}
