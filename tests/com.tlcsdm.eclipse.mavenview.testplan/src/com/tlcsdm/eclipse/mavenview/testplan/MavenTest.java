package com.tlcsdm.eclipse.mavenview.testplan;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotWorkbenchPart;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tlcsdm.eclipse.mavenview.MavenRunConfig;
import com.tlcsdm.eclipse.mavenview.Phase;
import com.tlcsdm.eclipse.mavenview.testplan.constants.MavenViewConstants;
import com.tlcsdm.eclipse.mavenview.testplan.data.MavenGav;
import com.tlcsdm.eclipse.mavenview.testplan.data.ProjectFactory;
import com.tlcsdm.eclipse.mavenview.testplan.data.WorkbenchView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class MavenTest extends AbstractMavenViewTest {

	private static final String SEPARATOR = "(?s)------------------------------------------------------------------------";

	private ProjectFactory projectFactory;

	private SWTBotView consoleView;
	private SWTBotView mavenView;

	@Before
	public void setUp() {
		this.projectFactory = new ProjectFactory(this.bot);
		addToTearDown(this.projectFactory::dispose);

		this.consoleView = WorkbenchView.CONSOLE.openProgrammatically(this.bot);
		terminateAllLaunches();

		this.mavenView = WorkbenchView.MAVEN.openProgrammatically(this.bot);
	}

	@After
	public void tearDown() {
		this.mavenView.close();
		terminateAllLaunches();
	}

	private void terminateAllLaunches() {
		this.consoleView.show();

		boolean wasLaunchRemoved = false;
		do {
			wasLaunchRemoved = false;

			final SWTBotToolbarButton terminateButton = enabledToolbarButton(this.consoleView,
					MavenViewConstants.COMMAND_TERMINATE);
			if (terminateButton != null) {
				terminateButton.click();
			}

			final SWTBotToolbarButton removeAllLaunchesButton = enabledToolbarButton(this.consoleView,
					MavenViewConstants.COMMAND_REMOVE_ALL_LAUNCHES);
			if (removeAllLaunchesButton != null) {
				removeAllLaunchesButton.click();
				wasLaunchRemoved = true;
			}

		} while (wasLaunchRemoved); // try to see if there is another launch
	}

	private static SWTBotToolbarButton enabledToolbarButton(SWTBotWorkbenchPart<?> part, String tooltip)
			throws WidgetNotFoundException {
		final List<SWTBotToolbarButton> l = part.getToolbarButtons();

		for (int i = 0; i < l.size(); i++) {
			final SWTBotToolbarButton item = l.get(i);
			if (item.getToolTipText().equals(tooltip))
				return item.isEnabled() ? item : null;
		}

		return null;
	}

	@Test
	public void testM01_RunMavenBuild() throws Exception {
		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_REFRESH);

		final SWTBotTree mavenProjectTree = this.mavenView.bot().tree();

		mavenProjectTree.getTreeItem(project.getName()).expand();
		mavenProjectTree.getTreeItem(project.getName()).getNode(Phase.CLEAN.getDisplayName()).select();

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_RUN_MAVEN_BUILD);

		this.consoleView.show();
		final String consoleText = waitForSeparators(this.consoleView.bot().styledText(), 2);

		Assert.assertTrue("Missing working directory for project " + project.getName() + ": " + consoleText,
				consoleText.matches("(?s)(.*)Working Directory: (.*)" + project.getName() + "(.*)"));
		Assert.assertTrue("Missing phases: " + consoleText, consoleText.contains("Phases: clean"));
	}

	private static String waitForSeparators(SWTBotStyledText styledText, int number) {
		final long startTime = System.currentTimeMillis();

		do {
			final String text = styledText.getText();

			if (text.split(SEPARATOR).length > number)
				return text;

			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				// just ignore
			}
		} while (System.currentTimeMillis() <= startTime + SWTBotPreferences.TIMEOUT);

		return styledText.getText();
	}

	@Test
	public void testM02_RunMavenBuildWithMultiplePhases() throws Exception {
		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_REFRESH);

		final SWTBotTree mavenProjectTree = this.mavenView.bot().tree();

		mavenProjectTree.getTreeItem(project.getName()).expand();

		final SWTBotTreeItem installItem = mavenProjectTree.getTreeItem(project.getName())
				.getNode(Phase.INSTALL.getDisplayName());
		final SWTBotTreeItem testItem = mavenProjectTree.getTreeItem(project.getName())
				.getNode(Phase.TEST.getDisplayName());
		mavenProjectTree.select(installItem, testItem);

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_RUN_MAVEN_BUILD);

		this.consoleView.show();
		final String consoleText = waitForSeparators(this.consoleView.bot().styledText(), 2);

		Assert.assertTrue("Missing phases: " + consoleText, consoleText.contains("Phases: test install"));
	}

	@Test
	public void testM03_RunMavenBuildWithMultipleProjects() throws Exception {
		final IProject project1 = this.projectFactory.createMavenProjectViaDialog(new MavenGav());
		final IProject project2 = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_REFRESH);

		final SWTBotTree mavenProjectTree = this.mavenView.bot().tree();

		mavenProjectTree.getTreeItem(project1.getName()).expand();
		mavenProjectTree.getTreeItem(project2.getName()).expand();

		final SWTBotTreeItem compileItem = mavenProjectTree.getTreeItem(project1.getName())
				.getNode(Phase.COMPILE.getDisplayName());
		final SWTBotTreeItem verifyItem = mavenProjectTree.getTreeItem(project2.getName())
				.getNode(Phase.VERIFY.getDisplayName());
		mavenProjectTree.select(compileItem, verifyItem);

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_RUN_MAVEN_BUILD);

		this.consoleView.show();
		String consoleText = waitForSeparators(this.consoleView.bot().styledText(), 2);

		final String phasePattern = "(?s)(.*)Phases: (verify|compile)(.*)";
		Assert.assertTrue("Missing phases: " + consoleText, consoleText.matches(phasePattern));

		final SWTBotToolbarDropDownButton button = this.consoleView
				.toolbarDropDownButton(MavenViewConstants.COMMAND_DISPLAY_SELECTED_CONSOLE);

		button.menuItem(new BaseMatcher<MenuItem>() {

			@Override
			public boolean matches(Object item) {
				return !((MenuItem) item).getSelection();
			}

			@Override
			public void describeTo(Description description) {
				// nothing to do?
			}
		}).click();

		consoleText = waitForSeparators(this.consoleView.bot().styledText(), 2);
		Assert.assertTrue("Missing phases: " + consoleText, consoleText.matches(phasePattern));
	}

	@Test
	public void testM04_RunMavenBuildForMavenLaunchConfig() throws Exception {
		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		final String mavenLaunchConfigName = UUID.randomUUID().toString();
		this.projectFactory.createMavenLaunchConfig(project, mavenLaunchConfigName,
				new MavenRunConfig().phases(Phase.CLEAN));

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_REFRESH);

		final SWTBotTree mavenProjectTree = this.mavenView.bot().tree();

		mavenProjectTree.getTreeItem(project.getName()).expand();
		mavenProjectTree.getTreeItem(project.getName()).getNode(mavenLaunchConfigName).select();

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_RUN_MAVEN_BUILD);

		this.consoleView.show();
		final String consoleText = waitForSeparators(this.consoleView.bot().styledText(), 1);

		Assert.assertTrue("Missing Maven's first line: " + consoleText,
				consoleText.startsWith("[INFO] Scanning for projects..."));
	}

}