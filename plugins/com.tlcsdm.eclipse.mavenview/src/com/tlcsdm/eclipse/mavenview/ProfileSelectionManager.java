package com.tlcsdm.eclipse.mavenview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.tlcsdm.eclipse.mavenview.internal.tree.ProfileNode;

/**
 * Manages the persistence of profile selection states per project.
 */
public class ProfileSelectionManager {

	private static final String PROFILES_KEY_PREFIX = "selectedProfiles.";
	
	// In-memory cache of profile selections per project
	private static final Map<String, List<String>> profileSelections = new HashMap<>();

	/**
	 * Saves the selection state of a profile for a project.
	 */
	public static void saveProfileSelection(IProject project, ProfileNode profileNode) {
		String projectName = project.getName();
		List<String> selectedProfiles = profileSelections.get(projectName);
		
		if (selectedProfiles == null) {
			selectedProfiles = new ArrayList<>();
			profileSelections.put(projectName, selectedProfiles);
		}
		
		String profileId = profileNode.getProfile().getId();
		if (profileNode.isSelected()) {
			if (!selectedProfiles.contains(profileId)) {
				selectedProfiles.add(profileId);
			}
		} else {
			selectedProfiles.remove(profileId);
		}
		
		// Persist to preferences
		persistProfileSelections(projectName, selectedProfiles);
	}

	/**
	 * Gets the selected profiles for a project.
	 */
	public static String[] getSelectedProfiles(IProject project) {
		String projectName = project.getName();
		List<String> selectedProfiles = profileSelections.get(projectName);
		
		if (selectedProfiles == null) {
			// Load from preferences
			selectedProfiles = loadProfileSelections(projectName);
			profileSelections.put(projectName, selectedProfiles);
		}
		
		return selectedProfiles.toArray(new String[selectedProfiles.size()]);
	}

	private static void persistProfileSelections(String projectName, List<String> selectedProfiles) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String key = PROFILES_KEY_PREFIX + projectName;
		
		if (selectedProfiles == null || selectedProfiles.isEmpty()) {
			prefs.remove(key);
		} else {
			String value = String.join(",", selectedProfiles);
			prefs.put(key, value);
		}
		
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private static List<String> loadProfileSelections(String projectName) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String key = PROFILES_KEY_PREFIX + projectName;
		String value = prefs.get(key, "");
		
		List<String> result = new ArrayList<>();
		if (value != null && !value.isEmpty()) {
			String[] profiles = value.split(",");
			for (String profile : profiles) {
				if (!profile.trim().isEmpty()) {
					result.add(profile.trim());
				}
			}
		}
		
		return result;
	}
}
