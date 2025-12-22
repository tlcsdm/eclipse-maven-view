package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewImages;

/**
 * Represents a single Maven plugin goal that can be executed.
 * Displayed as "prefix:goal" (e.g., "clean:clean", "compiler:compile").
 */
public class MavenPluginGoalNode implements Displayable {

	// Common goals for well-known Maven plugins
	private static final Map<String, String[]> KNOWN_PLUGIN_GOALS = new HashMap<>();

	static {
		// Maven core plugins
		KNOWN_PLUGIN_GOALS.put("maven-clean-plugin", new String[] { "clean", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-compiler-plugin", new String[] { "compile", "testCompile", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-deploy-plugin", new String[] { "deploy", "deploy-file", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-install-plugin", new String[] { "install", "install-file", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-resources-plugin", new String[] { "resources", "testResources", "copy-resources", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-site-plugin", new String[] { "site", "deploy", "run", "stage", "stage-deploy", "attach-descriptor", "jar", "effective-site", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-surefire-plugin", new String[] { "test", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-failsafe-plugin", new String[] { "integration-test", "verify", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-verifier-plugin", new String[] { "verify", "help" });

		// Maven packaging plugins
		KNOWN_PLUGIN_GOALS.put("maven-jar-plugin", new String[] { "jar", "test-jar", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-war-plugin", new String[] { "war", "exploded", "inplace", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-ear-plugin", new String[] { "ear", "generate-application-xml", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-ejb-plugin", new String[] { "ejb", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-rar-plugin", new String[] { "rar", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-source-plugin", new String[] { "jar", "test-jar", "jar-no-fork", "test-jar-no-fork", "aggregate", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-javadoc-plugin", new String[] { "javadoc", "test-javadoc", "jar", "test-jar", "aggregate", "aggregate-jar", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-shade-plugin", new String[] { "shade", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-assembly-plugin", new String[] { "single", "assembly", "help" });

		// Maven reporting plugins
		KNOWN_PLUGIN_GOALS.put("maven-changelog-plugin", new String[] { "changelog", "dev-activity", "file-activity", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-checkstyle-plugin", new String[] { "checkstyle", "checkstyle-aggregate", "check", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-pmd-plugin", new String[] { "pmd", "cpd", "check", "cpd-check", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-project-info-reports-plugin", new String[] { "index", "dependencies", "dependency-info", "dependency-management", "dependency-convergence", "plugin-management", "plugins", "scm", "summary", "team", "help" });

		// Maven tools plugins
		KNOWN_PLUGIN_GOALS.put("maven-antrun-plugin", new String[] { "run", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-dependency-plugin", new String[] { "analyze", "analyze-dep-mgt", "analyze-duplicate", "analyze-only", "analyze-report", "build-classpath", "copy", "copy-dependencies", "get", "go-offline", "list", "list-repositories", "properties", "purge-local-repository", "resolve", "resolve-plugins", "sources", "tree", "unpack", "unpack-dependencies", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-enforcer-plugin", new String[] { "enforce", "display-info", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-gpg-plugin", new String[] { "sign", "sign-and-deploy-file", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-help-plugin", new String[] { "active-profiles", "all-profiles", "describe", "effective-pom", "effective-settings", "evaluate", "expressions", "system", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-invoker-plugin", new String[] { "install", "integration-test", "verify", "run", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-release-plugin", new String[] { "clean", "prepare", "rollback", "perform", "stage", "branch", "update-versions", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-scm-plugin", new String[] { "add", "checkin", "checkout", "diff", "export", "status", "tag", "update", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-plugin-plugin", new String[] { "descriptor", "helpmojo", "report", "addPluginArtifactMetadata", "help" });

		// Tycho plugins (for Eclipse plugin development)
		KNOWN_PLUGIN_GOALS.put("tycho-maven-plugin", new String[] { "help" });
		KNOWN_PLUGIN_GOALS.put("target-platform-configuration", new String[] { "target-platform", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-packaging-plugin", new String[] { "package-plugin", "package-feature", "update-consumer-pom", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-p2-plugin", new String[] { "update-local-index", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-p2-director-plugin", new String[] { "materialize-products", "archive-products", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-p2-publisher-plugin", new String[] { "publish-products", "publish-categories", "publish-osgi-ee", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-p2-repository-plugin", new String[] { "assemble-repository", "archive-repository", "verify-repository", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-surefire-plugin", new String[] { "test", "plugin-test", "help" });
		KNOWN_PLUGIN_GOALS.put("tycho-versions-plugin", new String[] { "set-version", "update-pom", "bump-versions", "help" });

		// Spring Boot plugins
		KNOWN_PLUGIN_GOALS.put("spring-boot-maven-plugin", new String[] { "run", "repackage", "start", "stop", "build-info", "build-image", "help" });

		// Other common plugins
		KNOWN_PLUGIN_GOALS.put("exec-maven-plugin", new String[] { "exec", "java", "help" });
		KNOWN_PLUGIN_GOALS.put("versions-maven-plugin", new String[] { "set", "display-dependency-updates", "display-plugin-updates", "display-property-updates", "use-latest-releases", "use-latest-snapshots", "use-latest-versions", "use-next-releases", "use-next-snapshots", "use-next-versions", "commit", "revert", "help" });
		KNOWN_PLUGIN_GOALS.put("flatten-maven-plugin", new String[] { "flatten", "clean", "help" });
		KNOWN_PLUGIN_GOALS.put("git-changelog-maven-plugin", new String[] { "git-changelog", "help" });
		KNOWN_PLUGIN_GOALS.put("git-commit-id-maven-plugin", new String[] { "revision", "validateRevision", "help" });
		KNOWN_PLUGIN_GOALS.put("cyclonedx-maven-plugin", new String[] { "makeAggregateBom", "makeBom", "help" });
		KNOWN_PLUGIN_GOALS.put("maven-eclipse-plugin", new String[] { "eclipse", "clean", "configure-workspace", "help" });
	}

	private final MavenPluginNode pluginNode;
	private final String goal;

	public MavenPluginGoalNode(MavenPluginNode pluginNode, String goal) {
		this.pluginNode = Objects.requireNonNull(pluginNode);
		this.goal = Objects.requireNonNull(goal);
	}

	/**
	 * Create goal nodes for a plugin.
	 */
	public static MavenPluginGoalNode[] createGoals(MavenPluginNode pluginNode) {
		String artifactId = pluginNode.getArtifactId();
		String[] goals = KNOWN_PLUGIN_GOALS.get(artifactId);

		if (goals == null) {
			// For unknown plugins, provide common goals
			goals = new String[] { "help" };
		}

		List<MavenPluginGoalNode> result = new ArrayList<>();
		for (String goal : goals) {
			result.add(new MavenPluginGoalNode(pluginNode, goal));
		}
		return result.toArray(new MavenPluginGoalNode[0]);
	}

	@Override
	public String getDisplayName() {
		// Display format: prefix:goal (e.g., "clean:clean", "compiler:compile")
		return pluginNode.getPrefix() + ":" + goal;
	}

	/**
	 * Get the full Maven goal command to execute.
	 * Format: groupId:artifactId:version:goal or prefix:goal
	 */
	public String getGoalCommand() {
		// Use the short form: prefix:goal
		return pluginNode.getPrefix() + ":" + goal;
	}

	public String getGoal() {
		return goal;
	}

	public MavenPluginNode getPluginNode() {
		return pluginNode;
	}

	public IProject getProject() {
		return pluginNode.getProject();
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_MAVEN);
	}

	@Override
	public int hashCode() {
		return 19 * Objects.hash(pluginNode, goal);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		MavenPluginGoalNode other = (MavenPluginGoalNode) obj;
		return Objects.equals(pluginNode, other.pluginNode) && Objects.equals(goal, other.goal);
	}
}
