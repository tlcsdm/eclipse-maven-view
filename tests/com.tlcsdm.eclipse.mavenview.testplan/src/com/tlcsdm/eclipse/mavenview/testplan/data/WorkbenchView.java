package com.tlcsdm.eclipse.mavenview.testplan.data;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;

import com.tlcsdm.eclipse.mavenview.MavenView;
import com.tlcsdm.eclipse.mavenview.testplan.constants.CommonConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.MavenViewConstants;
import com.tlcsdm.eclipse.mavenview.testplan.constants.WorkbenchConstants;

public enum WorkbenchView {
	MAVEN(MavenViewConstants.VIEW_GROUP, MavenViewConstants.VIEW_TITLE, MavenView.ID),

	CONSOLE(WorkbenchConstants.GROUP_GENERAL, WorkbenchConstants.VIEW_CONSOLE, WorkbenchConstants.VIEW_CONSOLE_ID),

	PROJECT_EXPLORER(WorkbenchConstants.GROUP_GENERAL, WorkbenchConstants.VIEW_PROJECT_EXPLORER,
			WorkbenchConstants.VIEW_PROJECT_EXPLORER_ID),

	;

	private String viewGroup;
	private String viewTitle;
	private String id;

	private WorkbenchView(String group, String title, String id) {
		this.viewGroup = group;
		this.viewTitle = title;
		this.id = id;
	}

	public SWTBotView openProgrammatically(SWTWorkbenchBot bot) {
		return openViewProgrammatically(bot, this.id);
	}

	static SWTBotView openViewProgrammatically(SWTWorkbenchBot bot, String viewId) {
		final IViewReference[] view = { null };
		Display.getDefault().syncExec(() -> {
			try {
				final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				activePage.showView(viewId);
				view[0] = activePage.findViewReference(viewId);
			} catch (final PartInitException e) {
				Assert.fail(e.getMessage());
			}
		});
		Assert.assertNotNull("Could not open view " + viewId);

		final SWTBotView result = new SWTBotView(view[0], bot);
		Assert.assertTrue("View should be active!", result.isActive());
		return result;
	}

	public SWTBotView openViaDialog(SWTWorkbenchBot bot) {
		final SWTBotView view = findView(bot);
		if (view != null) {
			view.show();
			Assert.assertTrue("View should be active!", view.isActive());
			return view;
		}
		return openViewViaDialog(bot, this.viewGroup, this.viewTitle);
	}

	private SWTBotView findView(SWTWorkbenchBot bot) {
		for (final SWTBotView view : bot.views()) {
			if (view.getTitle().equals(this.viewTitle) || view.getReference().getId().equals(this.id))
				return view;
		}
		return null;
	}

	static SWTBotView openViewViaDialog(SWTWorkbenchBot bot, String viewGroup, String viewTitle) {
		bot.menu(WorkbenchConstants.MENU_WINDOW).menu(WorkbenchConstants.SUB_MENU_SHOW_VIEW)
				.menu(WorkbenchConstants.COMMAND_OTHER).click();

		bot.waitUntil(Conditions.shellIsActive(WorkbenchConstants.SHOW_VIEW_TITLE));

		bot.text().setText(MavenViewConstants.VIEW_TITLE);

		// dunno why we have to expand, but it's necessary else the title is not found
		bot.tree().getTreeItem(viewGroup).expand();
		bot.tree().getTreeItem(viewGroup).getNode(viewTitle).select();

		bot.button(CommonConstants.BUTTON_OPEN).click();

		return bot.viewByTitle(viewTitle);
	}

	public void close(SWTWorkbenchBot bot) {
		final SWTBotView view = findView(bot);
		if (view != null) {
			view.close();
		}
	}
}
