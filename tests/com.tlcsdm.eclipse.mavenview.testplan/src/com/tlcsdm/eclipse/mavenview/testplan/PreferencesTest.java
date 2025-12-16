package com.tlcsdm.eclipse.mavenview.testplan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;
import com.tlcsdm.eclipse.mavenview.Phase;
import com.tlcsdm.eclipse.mavenview.testplan.constants.CommonConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.MavenViewConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.PreferencesConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.WorkbenchConstants;
import com.tlcsdm.eclipse.mavenview.testplan.data.MavenGav;
import com.tlcsdm.eclipse.mavenview.testplan.data.ProjectFactory;
import com.tlcsdm.eclipse.mavenview.testplan.data.WorkbenchView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class PreferencesTest extends AbstractMavenViewTest {

	private ProjectFactory projectFactory;

	@Before
	public void setUp() {
		this.projectFactory = new ProjectFactory(this.bot);
		addToTearDown(this.projectFactory::dispose);
	}

	@Before
	@After
	public void closeMavenView() {
		WorkbenchView.MAVEN.close(this.bot);
	}

	@Before
	@After
	public void clearPreferences() {
		final IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setToDefault(MavenViewPreferences.DISPLAYED_PHASES);
		preferences.setToDefault(MavenViewPreferences.INITIAL_PROJECT_SELECTION);
		preferences.setToDefault(MavenViewPreferences.ALWAYS_SELECTED_PROJECTS);
		preferences.setToDefault(MavenViewPreferences.NEVER_SELECTED_PROJECTS);
	}

	@Test
	public void testP01A_InitialProjectSelectionAll() throws Exception {

		final IProject[] projects = this.projectFactory.createMavenProjectWithModulesViaDialog(new MavenGav(),
				UUID.randomUUID().toString());

		final SWTBotShell preferenceShell = openPreferencesDialog();

		final SWTBotCombo combo = preferenceShell.bot().comboBoxWithId(MavenViewPreferences.INITIAL_PROJECT_SELECTION);
		combo.setSelection(PreferencesConstants.INITIAL_PROJECT_SELECTION_ALL);

		preferenceShell.bot().button(PreferencesConstants.BUTTON_APPLY_AND_CLOSE).click();

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		Assert.assertEquals(2, viewTree.getAllItems().length);
		viewTree.getTreeItem(projects[0].getName()).expand();
		viewTree.getTreeItem(projects[1].getName()).expand();
	}

	private SWTBotShell openPreferencesDialog() {
		addToTearDown(this::clearPreferencesDialog);

		this.bot.menu(WorkbenchConstants.MENU_WINDOW).menu(WorkbenchConstants.COMMAND_PREFERENCES).click();
		this.bot.waitUntil(Conditions.shellIsActive(PreferencesConstants.TITLE));

		this.bot.tree().getTreeItem(PreferencesConstants.PREFERENCES_MAVEN).expand();
		this.bot.tree().getTreeItem(PreferencesConstants.PREFERENCES_MAVEN)
				.getNode(PreferencesConstants.PREFERENCES_RUNS_VIEW).select();

		return this.bot.activeShell();
	}

	private void clearPreferencesDialog() {
		final SWTBotShell activeShell = this.bot.activeShell();
		if (PreferencesConstants.TITLE.equals(activeShell.getText())) {
			activeShell.close();
		}
	}

	@Test
	public void testP01B_InitialProjectSelectionRoot() throws Exception {

		final IProject[] projects = this.projectFactory.createMavenProjectWithModulesViaDialog(new MavenGav(),
				UUID.randomUUID().toString());

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		final SWTBotShell preferenceShell = openPreferencesDialog();

		final SWTBotCombo combo = preferenceShell.bot().comboBoxWithId(MavenViewPreferences.INITIAL_PROJECT_SELECTION);
		combo.setSelection(PreferencesConstants.INITIAL_PROJECT_SELECTION_ROOT);

		preferenceShell.bot().button(CommonConstants.BUTTON_APPLY).click();
		preferenceShell.bot().button(CommonConstants.BUTTON_CANCEL).click();

		Assert.assertEquals(1, viewTree.getAllItems().length);
		viewTree.getTreeItem(projects[0].getName()).expand();
	}

	@Test
	public void testP02A_DisplayedPhases1() throws Exception {

		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		final SWTBotShell preferenceShell = openPreferencesDialog();

		final SWTBotTable table = preferenceShell.bot().tableWithId(MavenViewPreferences.DISPLAYED_PHASES);
		checkOnlyTableItems(table, "install");

		preferenceShell.bot().button(PreferencesConstants.BUTTON_APPLY_AND_CLOSE).click();

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		Assert.assertEquals(1, viewTree.getAllItems().length);
		final SWTBotTreeItem projectItem = viewTree.getTreeItem(project.getName());
		projectItem.expand();

		Assert.assertEquals(1, projectItem.getItems().length);
		Assert.assertEquals("install", projectItem.getItems()[0].getText());
	}

	private static void checkOnlyTableItems(SWTBotTable table, String... items) {
		final Set<String> itemsAsSet = new TreeSet<>(Arrays.asList(items));

		table.widget.getDisplay().syncExec(() -> {
			final int rowCount = table.widget.getItemCount();
			for (int i = 0; i < rowCount; i++) {
				final SWTBotTableItem item = table.getTableItem(i);
				if (itemsAsSet.contains(item.getText())) {
					item.check();
				} else {
					item.uncheck();
				}
			}
		});
	}

	@Test
	public void testP02B_DisplayedPhases1() throws Exception {

		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		final SWTBotShell preferenceShell = openPreferencesDialog();

		final SWTBotTable table = preferenceShell.bot().tableWithId(MavenViewPreferences.DISPLAYED_PHASES);
		checkOnlyTableItems(table, "site", "site-deploy");

		preferenceShell.bot().button(CommonConstants.BUTTON_APPLY).click();
		preferenceShell.bot().button(CommonConstants.BUTTON_CANCEL).click();

		Assert.assertEquals(1, viewTree.getAllItems().length);
		final SWTBotTreeItem projectItem = viewTree.getTreeItem(project.getName());
		projectItem.expand();

		Assert.assertEquals(2, projectItem.getItems().length);
		Assert.assertEquals("site", projectItem.getItems()[0].getText());
		Assert.assertEquals("site-deploy", projectItem.getItems()[1].getText());
	}

	@Test
	public void testP09_PreferencesDefault() throws Exception {

		final IProject[] projects = this.projectFactory.createMavenProjectWithModulesViaDialog(new MavenGav(),
				UUID.randomUUID().toString());

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		final SWTBotShell preferenceShell = openPreferencesDialog();

		preferenceShell.bot().button(PreferencesConstants.BUTTON_RESTORE_DEFAULTS).click();
		preferenceShell.bot().button(PreferencesConstants.BUTTON_APPLY_AND_CLOSE).click();

		Assert.assertEquals(1, viewTree.getAllItems().length);
		final SWTBotTreeItem projectItem = viewTree.getTreeItem(projects[0].getName());
		projectItem.expand();

		final Phase[] phases = Phase.values();
		Assert.assertEquals(phases.length, projectItem.getItems().length);

		for (int i = 0; i < phases.length; i++) {
			Assert.assertEquals(phases[i].getDisplayName(), projectItem.getItems()[i].getText());
		}
	}

	@Test
	public void testP10_ChangeDisplayedProjectsNoChange() throws Exception {

		final IProject[] projects = this.projectFactory.createMavenProjectWithModulesViaDialog(new MavenGav(),
				UUID.randomUUID().toString());

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		final List<String> projectNames = new ArrayList<>(projects.length);
		for (final SWTBotTreeItem item : viewTree.getAllItems()) {
			projectNames.add(item.getText());
		}

		final SWTBotShell changeDisplayedProjects = openChangeDisplayedProjectsDialog(view);
		changeDisplayedProjects.bot().button(CommonConstants.BUTTON_OK).click();

		for (final String projectName : projectNames) {
			assertItemExists(viewTree, projectName);
		}
	}

	private static void assertItemExists(final SWTBotTree tree, final String treeItemText) {
		Assert.assertNotNull("Item should be visible!", tree.getTreeItem(treeItemText));
	}

	private static void assertItemDoesNotExist(final SWTBotTree tree, final String treeItemText) {
		for (final SWTBotTreeItem treeItem : tree.getAllItems()) {
			Assert.assertNotEquals("Item should not be visible!", treeItemText, treeItem.getText());
		}
	}

	private SWTBotShell openChangeDisplayedProjectsDialog(SWTBotView mavenView) {
		addToTearDown(this::clearChangeDisplayedProjectsDialog);

		mavenView.toolbarButton(MavenViewConstants.COMMAND_CHANGE_DISPLAYED_PROJECTS).click();
		this.bot.waitUntil(Conditions.shellIsActive(MavenViewConstants.COMMAND_CHANGE_DISPLAYED_PROJECTS));

		return this.bot.activeShell();
	}

	private void clearChangeDisplayedProjectsDialog() {
		final SWTBotShell activeShell = this.bot.activeShell();
		if (MavenViewConstants.COMMAND_CHANGE_DISPLAYED_PROJECTS.equals(activeShell.getText())) {
			activeShell.close();
		}
	}

	@Test
	public void testP11_ChangeDisplayedProjectsNever() throws Exception {

		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());
		final String projectName = project.getName();

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		assertItemExists(viewTree, projectName);

		final SWTBotShell changeDisplayedProjects = openChangeDisplayedProjectsDialog(view);

		final SWTBotTable table = changeDisplayedProjects.bot().table();
		table.getTableItem(projectName).select();
		table.getTableItem(projectName).select();

		changeDisplayedProjects.bot().button(CommonConstants.BUTTON_OK).click();

		assertItemDoesNotExist(viewTree, projectName);
	}

	@Test
	public void testP12_ChangeDisplayedProjectsAlways() throws Exception {

		final IProject[] projects = this.projectFactory.createMavenProjectWithModulesViaDialog(new MavenGav(),
				UUID.randomUUID().toString());
		final String moduleName = projects[1].getName();

		final SWTBotView view = WorkbenchView.MAVEN.openProgrammatically(this.bot);
		final SWTBotTree viewTree = view.bot().tree();

		assertItemDoesNotExist(viewTree, moduleName);

		final SWTBotShell changeDisplayedProjects = openChangeDisplayedProjectsDialog(view);

		final SWTBotTable table = changeDisplayedProjects.bot().table();
		table.getTableItem(moduleName).select();

		changeDisplayedProjects.bot().button(CommonConstants.BUTTON_OK).click();

		assertItemExists(viewTree, moduleName);
	}
}