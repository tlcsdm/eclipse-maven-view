package com.tlcsdm.eclipse.mavenview.internal.tree;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.swt.graphics.Image;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenRunner;
import com.tlcsdm.eclipse.mavenview.MavenViewImages;

/**
 * Parent node for all Maven plugins in a project.
 */
public class MavenPluginsNode implements Displayable, Parentable {

	private final ProjectNode mavenProject;
	private final IProject project;

	public MavenPluginsNode(ProjectNode mavenProject) {
		this.mavenProject = Objects.requireNonNull(mavenProject);
		this.project = mavenProject.getProjectResource();
	}

	public ProjectNode getMavenProject() {
		return this.mavenProject;
	}

	@Override
	public String getDisplayName() {
		return Messages.getString("MavenPlugins");
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_PLUGINS);
	}

	@Override
	public Object[] getChildren() {
		return readPlugins(project, mavenProject);
	}

	/**
	 * Check if the project has any build plugins.
	 * @param project the project to check
	 * @return true if the project has plugins, false otherwise
	 */
	public static boolean hasPlugins(IProject project) {
		return readPlugins(project, null).length > 0;
	}

	private static MavenPluginNode[] readPlugins(IProject project, ProjectNode mavenProject) {
		try {
			final IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
			final IFile pomFile = project.getFile(new Path(MavenRunner.POM_FILE_NAME));
			final IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, new NullProgressMonitor());

			if (projectFacade == null) {
				return new MavenPluginNode[0];
			}

			try {
				final Object mavenProjectObj = projectFacade.getMavenProject(new NullProgressMonitor());
				if (mavenProjectObj == null) {
					return new MavenPluginNode[0];
				}

				// Use reflection to call getBuildPlugins()
				final java.lang.reflect.Method getBuildPluginsMethod = mavenProjectObj.getClass().getMethod("getBuildPlugins");
				@SuppressWarnings("unchecked")
				final java.util.List<Object> plugins = (java.util.List<Object>) getBuildPluginsMethod.invoke(mavenProjectObj);

				if (plugins == null || plugins.isEmpty()) {
					return new MavenPluginNode[0];
				}

				final List<MavenPluginNode> result = new ArrayList<>();
				for (Object pluginObj : plugins) {
					// Use reflection to get plugin properties
					final java.lang.reflect.Method getGroupIdMethod = pluginObj.getClass().getMethod("getGroupId");
					final String groupId = (String) getGroupIdMethod.invoke(pluginObj);

					final java.lang.reflect.Method getArtifactIdMethod = pluginObj.getClass().getMethod("getArtifactId");
					final String artifactId = (String) getArtifactIdMethod.invoke(pluginObj);

					final java.lang.reflect.Method getVersionMethod = pluginObj.getClass().getMethod("getVersion");
					final String version = (String) getVersionMethod.invoke(pluginObj);

					if (groupId == null || artifactId == null) {
						continue;
					}

					// Get plugin prefix (short name for goals)
					String prefix = getPluginPrefix(artifactId);

					result.add(new MavenPluginNode(mavenProject, groupId, artifactId, 
							version != null ? version : "", prefix));
				}

				return result.toArray(new MavenPluginNode[0]);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				// Reflection failed, fall back to XML parsing
				Activator.getDefault().getLog().warn(
						"Maven reflection API access failed for project " + project.getName()
								+ ", falling back to XML parsing.",
						e);
				return readPluginsFromXml(pomFile, mavenProject);
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().error("Failed to read Maven plugins for project " + project.getName(), e);
		}
		return new MavenPluginNode[0];
	}

	/**
	 * Extract the plugin prefix from the artifact ID.
	 * Maven plugin naming convention: xxx-maven-plugin or maven-xxx-plugin -> prefix is xxx
	 */
	private static String getPluginPrefix(String artifactId) {
		if (artifactId == null) {
			return "";
		}
		// Common pattern: xxx-maven-plugin
		if (artifactId.endsWith("-maven-plugin")) {
			return artifactId.substring(0, artifactId.length() - "-maven-plugin".length());
		}
		// Official Maven pattern: maven-xxx-plugin
		if (artifactId.startsWith("maven-") && artifactId.endsWith("-plugin")) {
			return artifactId.substring("maven-".length(), artifactId.length() - "-plugin".length());
		}
		// Fallback: use full artifact ID
		return artifactId;
	}

	private static MavenPluginNode[] readPluginsFromXml(IFile pomFile, ProjectNode mavenProject) {
		try {
			if (pomFile == null || !pomFile.exists()) {
				return new MavenPluginNode[0];
			}

			// Parse pom.xml using DOM parser
			final javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(false);

			// Security: Disable external entities to prevent XXE attacks
			try {
				factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
			} catch (javax.xml.parsers.ParserConfigurationException e) {
				// Feature not supported, continue
			}
			try {
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			} catch (javax.xml.parsers.ParserConfigurationException e) {
				// Feature not supported, continue
			}
			try {
				factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			} catch (javax.xml.parsers.ParserConfigurationException e) {
				// Feature not supported, continue
			}
			try {
				factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			} catch (javax.xml.parsers.ParserConfigurationException e) {
				// Feature not supported, continue
			}
			try {
				factory.setExpandEntityReferences(false);
			} catch (IllegalArgumentException e) {
				// Feature not supported, continue
			}

			final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
			final org.w3c.dom.Document document = builder.parse(pomFile.getContents());

			// Get build/plugins/plugin elements
			final org.w3c.dom.NodeList buildNodes = document.getElementsByTagName("build");
			if (buildNodes.getLength() == 0) {
				return new MavenPluginNode[0];
			}

			final List<MavenPluginNode> result = new ArrayList<>();
			for (int b = 0; b < buildNodes.getLength(); b++) {
				final org.w3c.dom.Element buildElement = (org.w3c.dom.Element) buildNodes.item(b);
				
				// Only process direct child <plugins> element
				final org.w3c.dom.NodeList pluginsNodes = buildElement.getElementsByTagName("plugins");
				for (int p = 0; p < pluginsNodes.getLength(); p++) {
					final org.w3c.dom.Element pluginsElement = (org.w3c.dom.Element) pluginsNodes.item(p);
					
					// Skip if this is under pluginManagement
					if (pluginsElement.getParentNode() != buildElement) {
						continue;
					}
					
					final org.w3c.dom.NodeList pluginNodes = pluginsElement.getElementsByTagName("plugin");
					for (int i = 0; i < pluginNodes.getLength(); i++) {
						final org.w3c.dom.Element pluginElement = (org.w3c.dom.Element) pluginNodes.item(i);

						// Get groupId (defaults to org.apache.maven.plugins)
						String groupId = "org.apache.maven.plugins";
						final org.w3c.dom.NodeList groupIdNodes = pluginElement.getElementsByTagName("groupId");
						if (groupIdNodes.getLength() > 0) {
							groupId = groupIdNodes.item(0).getTextContent().trim();
						}

						// Get artifactId
						final org.w3c.dom.NodeList artifactIdNodes = pluginElement.getElementsByTagName("artifactId");
						if (artifactIdNodes.getLength() == 0) {
							continue;
						}
						final String artifactId = artifactIdNodes.item(0).getTextContent().trim();

						// Get version (optional)
						String version = "";
						final org.w3c.dom.NodeList versionNodes = pluginElement.getElementsByTagName("version");
						if (versionNodes.getLength() > 0) {
							version = versionNodes.item(0).getTextContent().trim();
						}

						// Skip plugins with empty artifactId
						if (artifactId.isEmpty()) {
							continue;
						}

						String prefix = getPluginPrefix(artifactId);
						result.add(new MavenPluginNode(mavenProject, groupId, artifactId, version, prefix));
					}
				}
			}

			return result.toArray(new MavenPluginNode[0]);
		} catch (Exception e) {
			Activator.getDefault().getLog().error("Failed to parse POM XML for plugins", e);
		}
		return new MavenPluginNode[0];
	}

	@Override
	public int hashCode() {
		return 13 * Objects.hash(mavenProject);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		MavenPluginsNode other = (MavenPluginsNode) obj;
		return Objects.equals(mavenProject, other.mavenProject);
	}
}
