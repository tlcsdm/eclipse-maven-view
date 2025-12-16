package com.tlcsdm.eclipse.mavenview.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.InitialProjectSelection;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;
import com.tlcsdm.eclipse.mavenview.Phase;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(MavenViewPreferences.INITIAL_PROJECT_SELECTION, InitialProjectSelection.ROOT_PROJECTS.name());
		store.setDefault(MavenViewPreferences.DISPLAYED_PHASES,
				MavenViewPreferences.getDisplayedPhasesString(Phase.values()));
	}

}
