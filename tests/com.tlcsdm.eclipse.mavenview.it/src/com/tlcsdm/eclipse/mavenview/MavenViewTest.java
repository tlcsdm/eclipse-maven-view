package com.tlcsdm.eclipse.mavenview;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MavenViewTest {

	private IWorkbenchPage activePage;

	@Before
	public void setUp() {
		this.activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		closeAllViews();
	}

	private void closeAllViews() {
		for (final IViewReference view : this.activePage.getViewReferences()) {
			this.activePage.hideView(view);
		}
	}

	@After
	public void tearDown() {
		closeAllViews();
	}

	@Test
	public void testOpenView() throws Exception {
		openView();
	}

	private MavenView openView() throws PartInitException {
		final IViewPart view = this.activePage.showView(MavenView.ID);

		Assert.assertTrue("Wrong view: " + view, view instanceof MavenView);
		return (MavenView) view;
	}

	@Test
	public void testRefresh() throws Exception {
		MavenView mavenView = openView();

		String projectName = UUID.randomUUID().toString();
		AbstractProjectBasedTest.createMavenProject(projectName);

		mavenView.refresh();

		TreeItem projectTreeItem = Arrays.stream(mavenView.viewer.getTree().getItems())
				.filter(i -> i.getText().equals(projectName)).findAny().orElse(null);
		Assert.assertNotNull("Could no find project in Maven view: " + projectName, projectTreeItem);
	}

	@Test
	public void testCollapseAll() throws Exception {
		String projectName = UUID.randomUUID().toString();
		AbstractProjectBasedTest.createMavenProject(projectName);

		MavenView mavenView = openView();

		TreeItem projectTreeItem = Arrays.stream(mavenView.viewer.getTree().getItems())
				.filter(i -> i.getText().equals(projectName)).findAny().orElse(null);
		Assert.assertNotNull("Could no find project in Maven view: " + projectName, projectTreeItem);

		Assert.assertTrue("Projects should be expanded on default.", projectTreeItem.getExpanded());

		mavenView.collapseAll();

		Assert.assertFalse("Project should be collapsed now.", projectTreeItem.getExpanded());
	}

	@Test
	public void testExpandAll() throws Exception {
		String projectName = UUID.randomUUID().toString();
		AbstractProjectBasedTest.createMavenProject(projectName);

		MavenView mavenView = openView();

		TreeItem projectTreeItem = Arrays.stream(mavenView.viewer.getTree().getItems())
				.filter(i -> i.getText().equals(projectName)).findAny().orElse(null);
		Assert.assertNotNull("Could no find project in Maven view: " + projectName, projectTreeItem);

		projectTreeItem.setExpanded(false);
		Assert.assertFalse("Project should be collapsed.", projectTreeItem.getExpanded());

		mavenView.expandAll();

		Assert.assertTrue("Project should be expanded now.", projectTreeItem.getExpanded());
	}
}
