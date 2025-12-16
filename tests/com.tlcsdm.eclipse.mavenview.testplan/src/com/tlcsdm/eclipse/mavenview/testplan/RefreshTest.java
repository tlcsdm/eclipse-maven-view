package com.tlcsdm.eclipse.mavenview.testplan;

import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
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
public class RefreshTest extends AbstractMavenViewTest {

	private ProjectFactory projectFactory;
	private SWTBotView mavenView;

	@Before
	public void setUp() {
		this.projectFactory = new ProjectFactory(this.bot);
		addToTearDown(this.projectFactory::dispose);

		this.mavenView = WorkbenchView.MAVEN.openProgrammatically(this.bot);
	}

	@After
	public void closeMavenView() {
		this.mavenView.close();
	}

	@Test
	public void testR01_RefreshView() throws Exception {
		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_REFRESH);

		final SWTBotTree mavenProjectTree = this.mavenView.bot().tree();
		Assert.assertEquals(1, mavenProjectTree.getAllItems().length);
		Assert.assertNotNull(mavenProjectTree.getTreeItem(project.getName()));
	}

	@Test
	public void testR02_RefreshViewForLaunchConfig() throws Exception {
		final IProject project = this.projectFactory.createMavenProjectViaDialog(new MavenGav());
		final String mavenLaunchConfigName = UUID.randomUUID().toString();
		this.projectFactory.createMavenLaunchConfig(project, mavenLaunchConfigName,
				new MavenRunConfig().phases(Phase.CLEAN, Phase.INSTALL));

		clickToolbarButton(this.mavenView, MavenViewConstants.COMMAND_REFRESH);

		final SWTBotTree mavenProjectTree = this.mavenView.bot().tree();
		Assert.assertEquals(1, mavenProjectTree.getAllItems().length);

		final SWTBotTreeItem projectItem = mavenProjectTree.getTreeItem(project.getName());
		Assert.assertNotNull(projectItem);
		Assert.assertEquals(2, projectItem.getNodes().size());
		Assert.assertNotNull(projectItem.getNode(mavenLaunchConfigName));
	}

}