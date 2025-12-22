package com.tlcsdm.eclipse.mavenview;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.tlcsdm.eclipse.mavenview.internal.DisplayableLabelProvider;
import com.tlcsdm.eclipse.mavenview.internal.ProfileSelectionManager;
import com.tlcsdm.eclipse.mavenview.internal.tree.LaunchConfigNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.MavenPluginGoalNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.PhaseNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.PhasesNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.ProfileNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.ProjectNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.ProjectTreeContentProvider;

public class MavenView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.tlcsdm.eclipse.mavenview.MavenView";

	TreeViewer viewer;
	private IResourceChangeListener resourceChangeListener;

	@Override
	public void createPartControl(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		this.viewer.setAutoExpandLevel(AbstractTreeViewer.NO_EXPAND);
		this.viewer.setLabelProvider(new DisplayableLabelProvider());
		this.viewer.setContentProvider(new ProjectTreeContentProvider());
		this.viewer.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				Object element = event.getElement();
				// Refresh phase nodes to show updated status icons for `test` phase
				// Refresh profile nodes to show updated selection state
				if (element instanceof ProjectNode || element instanceof PhasesNode || element instanceof ProfileNode) {
					Display.getDefault().asyncExec(() -> {
						viewer.refresh(element, true);
					});
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				// do nothing
			}
		});
		ProjectNode[] inputNodes = ProjectTreeContentProvider.fetchMavenProjects();
		this.viewer.setInput(inputNodes);
		if (inputNodes != null && inputNodes.length == 1) {
			this.viewer.expandAll();
		}
		// Add single-click listener for profile selection toggle
		this.viewer.getTree().addListener(SWT.MouseDown, event -> {
			if (event.button == 1) { // Left click
				org.eclipse.swt.graphics.Point point = new org.eclipse.swt.graphics.Point(event.x, event.y);
				org.eclipse.swt.widgets.TreeItem item = viewer.getTree().getItem(point);
				if (item != null) {
					Object data = item.getData();
					if (data instanceof ProfileNode) {
						ProfileNode profileNode = (ProfileNode) data;
						profileNode.setSelected(!profileNode.isSelected());
						ProfileSelectionManager.saveProfileSelection(profileNode.getProject(), profileNode);
						viewer.refresh(profileNode, true);
					}
				}
			}
		});
		this.viewer.addDoubleClickListener(event -> {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object selectedElement = selection.getFirstElement();
			if (selectedElement instanceof PhaseNode || selectedElement instanceof LaunchConfigNode
					|| selectedElement instanceof MavenPluginGoalNode) {
				executeCommand(selectedElement);
			}
		});
		hookMenuToViewer();
		hookResourceListener();
		hookPreferenceListener();

		getSite().setSelectionProvider(this.viewer);
	}

	public void refresh() {
		final Object[] expandedElements = this.viewer.getExpandedElements();
		this.viewer.setInput(ProjectTreeContentProvider.fetchMavenProjects());
		this.viewer.setExpandedElements(expandedElements);
		this.viewer.refresh(true);
	}

	private void executeCommand(Object selectedNode) {
		try {
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
					.getService(IHandlerService.class);
			handlerService.executeCommand("com.tlcsdm.eclipse.mavenview.commands.runMavenPhases", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hookMenuToViewer() {
		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(this.viewer.getTree());
		this.viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuManager, this.viewer);
	}

	private void hookResourceListener() {
		resourceChangeListener = event -> {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				handleResourceChanged(event);
			}
		};

		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener,
				IResourceChangeEvent.POST_CHANGE);
	}

	private void handleResourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(delta -> {
				IResource resource = delta.getResource();

				if (resource instanceof IProject) {
					Display.getDefault().asyncExec(() -> {
						refresh();
					});
				}
				return true;
			});
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void hookPreferenceListener() {
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceChangeListener);
	}

	IPropertyChangeListener preferenceChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (MavenViewPreferences.SKIP_TESTS.equals(event.getProperty())) {
				Display.getDefault().asyncExec(() -> {
					viewer.refresh(true);
				});
			}
		}

	};

	@Override
	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

	public void collapseAll() {
		this.viewer.collapseAll();
	}

	public void expandAll() {
		BusyIndicator.showWhile(this.viewer.getControl().getDisplay(), () -> {
			this.viewer.getTree().setRedraw(false);
			try {
				this.viewer.expandAll();
				this.viewer.refresh();
			} finally {
				this.viewer.getTree().setRedraw(true);
			}
		});
	}

	public void expand(Object element) {
		this.viewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
		this.viewer.refresh(element, true);
	}

	@Override
	public void dispose() {
		if (resourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
		if (preferenceChangeListener != null) {
			Activator.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceChangeListener);
		}
		super.dispose();
	}

}
