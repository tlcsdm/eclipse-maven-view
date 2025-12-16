package com.tlcsdm.eclipse.mavenview;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import com.tlcsdm.eclipse.mavenview.internal.DisplayableLabelProvider;
import com.tlcsdm.eclipse.mavenview.internal.tree.ProjectTreeContentProvider;

public class MavenView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.slothsoft.mavenview.MavenView";

	TreeViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		this.viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		this.viewer.setLabelProvider(new DisplayableLabelProvider());
		this.viewer.setContentProvider(new ProjectTreeContentProvider());
		this.viewer.setInput(ProjectTreeContentProvider.fetchMavenProjects());
		this.viewer.expandAll();

		hookMenuToViewer();

		getSite().setSelectionProvider(this.viewer);
	}

	public void refresh() {
		final Object[] expandedElements = this.viewer.getExpandedElements();
		this.viewer.setInput(ProjectTreeContentProvider.fetchMavenProjects());
		this.viewer.setExpandedElements(expandedElements);
	}

	private void hookMenuToViewer() {
		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(this.viewer.getTree());
		this.viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

	public void collapseAll() {
		this.viewer.collapseAll();
	}

	public void expandAll() {
		this.viewer.expandAll();
	}

}
