package com.tlcsdm.eclipse.mavenview;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Constant definitions for plug-in preferences.
 */

public final class MavenViewPreferences {

	public static final String INITIAL_PROJECT_SELECTION = "initialProjectSelection";
	public static final String DISPLAYED_PHASES = "displayedPhases";
	public static final String ALWAYS_SELECTED_PROJECTS = "alwaysSelectedProjects";
	public static final String NEVER_SELECTED_PROJECTS = "neverSelectedProjects";

	static final String SEPARATOR = "\n";

	public static void setInitialProjectSelection(InitialProjectSelection value) {
		if (value == null) {
			getPreferences().setToDefault(INITIAL_PROJECT_SELECTION);
		} else {
			getPreferences().setValue(INITIAL_PROJECT_SELECTION, value.name());
		}
	}

	private static IPreferenceStore getPreferences() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static InitialProjectSelection getInitialProjectSelection() {
		return InitialProjectSelection.valueOf(getPreferences().getString(INITIAL_PROJECT_SELECTION));
	}

	public static Phase[] getDisplayedPhases() {
		return Arrays.stream(getPreferences().getString(DISPLAYED_PHASES).split(SEPARATOR)).filter(s -> !s.isEmpty())
				.map(Phase::valueOf).toArray(Phase[]::new);
	}

	public static void setDisplayedPhases(Phase[] displayedPhases) {
		if (displayedPhases == null || displayedPhases.length == 0) {
			getPreferences().setToDefault(DISPLAYED_PHASES);
		} else {
			getPreferences().setValue(DISPLAYED_PHASES, getDisplayedPhasesString(displayedPhases));
		}
	}

	public static String getDisplayedPhasesString(Phase[] displayedPhases) {
		return Arrays.stream(displayedPhases).map(Phase::name).collect(Collectors.joining(SEPARATOR));
	}

	public static IProject[] getAlwaysSelectedProjects() {
		return getProjects(ALWAYS_SELECTED_PROJECTS);
	}

	private static IProject[] getProjects(String preferences) {
		final List<String> projectNames = Arrays.asList(getPreferences().getString(preferences).split(SEPARATOR));
		return Arrays.stream(InitialProjectSelection.fetchAllMavenProjects())
				.filter(p -> projectNames.contains(p.getName())).toArray(IProject[]::new);
	}

	public static void setAlwaysSelectedProjects(IProject... alwaysSelectedProjects) {
		setProjects(ALWAYS_SELECTED_PROJECTS, alwaysSelectedProjects);
	}

	private static void setProjects(String preferences, IProject[] projects) {
		if (projects == null || projects.length == 0) {
			getPreferences().setToDefault(preferences);
		} else {
			getPreferences().setValue(preferences,
					Arrays.stream(projects).map(IProject::getName).collect(Collectors.joining(SEPARATOR)));
		}
	}

	public static IProject[] getNeverSelectedProjects() {
		return getProjects(NEVER_SELECTED_PROJECTS);
	}

	public static void setNeverSelectedProjects(IProject... neverSelectedProjects) {
		setProjects(NEVER_SELECTED_PROJECTS, neverSelectedProjects);
	}

	private MavenViewPreferences() {
		// hide me
	}

}
