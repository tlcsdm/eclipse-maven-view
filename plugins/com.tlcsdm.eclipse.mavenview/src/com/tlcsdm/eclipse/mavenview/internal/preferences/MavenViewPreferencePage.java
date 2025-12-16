package com.tlcsdm.eclipse.mavenview.internal.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.InitialProjectSelection;
import com.tlcsdm.eclipse.mavenview.MavenView;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;
import com.tlcsdm.eclipse.mavenview.Phase;
import com.tlcsdm.eclipse.mavenview.internal.DisplayableLabelProvider;
import com.tlcsdm.eclipse.mavenview.internal.Messages;
import com.tlcsdm.eclipse.mavenview.internal.common.CheckTableFieldEditor;
import com.tlcsdm.eclipse.mavenview.internal.common.CheckTableFieldEditor.PreferenceLabelProvider;

public class MavenViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static class PhaseLabelProvider extends DisplayableLabelProvider implements PreferenceLabelProvider {

		@Override
		public String getPreference(Object element) {
			return ((Phase) element).name();
		}
	}

	private IWorkbench workbench;

	public MavenViewPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench newWorkbench) {
		this.workbench = newWorkbench;
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	public void createFieldEditors() {
		final Composite fieldEditorParent = getFieldEditorParent();

		addField(new ComboFieldEditor(MavenViewPreferences.INITIAL_PROJECT_SELECTION,
				Messages.getString("InitialProjectSelection") + ':',
				createEntryNamesAndValues(InitialProjectSelection.values()), fieldEditorParent));
		fieldEditorParent.getChildren()[1].setData(CheckTableFieldEditor.DATA_ID,
				MavenViewPreferences.INITIAL_PROJECT_SELECTION);

		addField(new CheckTableFieldEditor(MavenViewPreferences.DISPLAYED_PHASES, getFieldEditorParent(),
				Messages.getString("DisplayedPhases") + ':').labelProvider(new PhaseLabelProvider())
				.input(Phase.values()));
	}

	static <E extends Enum<E> & Displayable> String[][] createEntryNamesAndValues(E[] enumValues) {
		final String[][] result = new String[enumValues.length][2];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = enumValues[i].getDisplayName();
			result[i][1] = enumValues[i].name();
		}
		return result;
	}

	@Override
	public boolean performOk() {
		final boolean result = super.performOk();
		if (result) {
			refreshView();
		}
		return result;
	}

	private void refreshView() {
		IWorkbenchPage page = this.workbench.getActiveWorkbenchWindow().getActivePage();

		for (IViewReference ref : page.getViewReferences()) {
			IViewPart view = ref.getView(false);
			if (view instanceof MavenView) {
				((MavenView) view).refresh();
			}
		}
	}

	@Override
	protected void performApply() {
		super.performApply();
		refreshView();
	}

}