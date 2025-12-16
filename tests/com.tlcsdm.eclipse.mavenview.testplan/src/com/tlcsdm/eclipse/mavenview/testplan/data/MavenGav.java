package com.tlcsdm.eclipse.mavenview.testplan.data;

import java.util.Objects;
import java.util.UUID;

public class MavenGav {

	String groupId = "de.slothsoft.group";
	String artifactId = UUID.randomUUID().toString();
	String version = "0.0.1-SNAPSHOT";
	String type = "jar";

	public String getArtifactId() {
		return this.artifactId;
	}

	public MavenGav artifactId(String newArtifactId) {
		setArtifactId(newArtifactId);
		return this;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = Objects.requireNonNull(artifactId);
	}

	public String getGroupId() {
		return this.groupId;
	}

	public MavenGav groupId(String newGroupId) {
		setGroupId(newGroupId);
		return this;
	}

	public void setGroupId(String groupId) {
		this.groupId = Objects.requireNonNull(groupId);
	}

	public String getVersion() {
		return this.version;
	}

	public MavenGav version(String newVersion) {
		setVersion(newVersion);
		return this;
	}

	public void setVersion(String version) {
		this.version = Objects.requireNonNull(version);
	}

	public String getType() {
		return this.type;
	}

	public MavenGav type(String newType) {
		setType(newType);
		return this;
	}

	public void setType(String type) {
		this.type = Objects.requireNonNull(type);
	}

}
