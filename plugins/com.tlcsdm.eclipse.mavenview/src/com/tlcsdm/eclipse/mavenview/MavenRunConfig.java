package com.tlcsdm.eclipse.mavenview;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MavenRunConfig {

	private Phase[] phases = { Phase.CLEAN, Phase.INSTALL };
	private String[] profiles = new String[0];

	public String toGoalString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getPhasesAsString());
		return sb.toString();
	}

	public String getPhasesAsString() {
		return Arrays.stream(this.phases).sorted().map(Phase::getDisplayName).collect(Collectors.joining(" "));
	}

	public Phase[] getPhases() {
		return this.phases;
	}

	public MavenRunConfig phases(Phase... newPhases) {
		setPhases(newPhases);
		return this;
	}

	public void setPhases(Phase... phases) {
		this.phases = Objects.requireNonNull(phases);
	}

	public String[] getProfiles() {
		return this.profiles;
	}

	public MavenRunConfig profiles(String... newProfiles) {
		setProfiles(newProfiles);
		return this;
	}

	public void setProfiles(String... profiles) {
		this.profiles = profiles == null ? new String[0] : profiles.clone();
	}

	public String getProfilesAsString() {
		if (this.profiles == null || this.profiles.length == 0)
			return "";
		return Arrays.stream(this.profiles).filter(p -> p != null && p.length() > 0).collect(Collectors.joining(","));
	}

	public MavenRunConfig copy() {
		return new MavenRunConfig().phases(this.phases.clone()).profiles(this.profiles == null ? new String[0] : this.profiles.clone());
	}

	@Override
	public String toString() {
		return "MavenRunConfig [" + Arrays.toString(this.phases) + ", profiles=" + Arrays.toString(this.profiles) + "]";
	}

}