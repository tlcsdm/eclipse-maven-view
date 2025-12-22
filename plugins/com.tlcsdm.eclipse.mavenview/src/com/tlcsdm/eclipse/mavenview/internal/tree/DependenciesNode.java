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
 * Parent node for all Maven dependencies in a project.
 */
public class DependenciesNode implements Displayable, Parentable {

	private final ProjectNode mavenProject;
	private final IProject project;

	public DependenciesNode(ProjectNode mavenProject) {
		this.mavenProject = Objects.requireNonNull(mavenProject);
		this.project = mavenProject.getProjectResource();
	}

	@Override
	public String getDisplayName() {
		return Messages.getString("Dependencies");
	}

	@Override
	public Image getImage() {
		return Activator.getImage(MavenViewImages.OBJ_DEPENDENCIES);
	}

	@Override
	public Object[] getChildren() {
		return readDependencies(project);
	}

	/**
	 * Check if the project has any dependencies.
	 * @param project the project to check
	 * @return true if the project has dependencies, false otherwise
	 */
	public static boolean hasDependencies(IProject project) {
		return readDependencies(project).length > 0;
	}

	private static DependencyNode[] readDependencies(IProject project) {
		try {
			final IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
			final IFile pomFile = project.getFile(new Path(MavenRunner.POM_FILE_NAME));
			final IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, new NullProgressMonitor());

			if (projectFacade == null) {
				return new DependencyNode[0];
			}

			try {
				final Object mavenProject = projectFacade.getMavenProject(new NullProgressMonitor());
				if (mavenProject == null) {
					return new DependencyNode[0];
				}

				// Use reflection to call getModel()
				final java.lang.reflect.Method getModelMethod = mavenProject.getClass().getMethod("getModel");
				final Object model = getModelMethod.invoke(mavenProject);
				if (model == null) {
					return new DependencyNode[0];
				}

				// Use reflection to call getDependencies()
				final java.lang.reflect.Method getDependenciesMethod = model.getClass().getMethod("getDependencies");
				@SuppressWarnings("unchecked")
				final java.util.List<Object> dependencies = (java.util.List<Object>) getDependenciesMethod.invoke(model);

				if (dependencies == null || dependencies.isEmpty()) {
					return new DependencyNode[0];
				}

				final List<DependencyNode> result = new ArrayList<>();
				for (Object dependencyObj : dependencies) {
					// Use reflection to get dependency properties
					final java.lang.reflect.Method getGroupIdMethod = dependencyObj.getClass().getMethod("getGroupId");
					final String groupId = (String) getGroupIdMethod.invoke(dependencyObj);

					final java.lang.reflect.Method getArtifactIdMethod = dependencyObj.getClass().getMethod("getArtifactId");
					final String artifactId = (String) getArtifactIdMethod.invoke(dependencyObj);

					final java.lang.reflect.Method getVersionMethod = dependencyObj.getClass().getMethod("getVersion");
					final String version = (String) getVersionMethod.invoke(dependencyObj);

					final java.lang.reflect.Method getScopeMethod = dependencyObj.getClass().getMethod("getScope");
					final String scope = (String) getScopeMethod.invoke(dependencyObj);

					if (groupId == null || artifactId == null) {
						continue;
					}

					result.add(new DependencyNode(groupId, artifactId, version != null ? version : "", scope));
				}

				return result.toArray(new DependencyNode[0]);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				// Reflection failed, fall back to XML parsing
				Activator.getDefault().getLog().warn(
						"Maven reflection API access failed for project " + project.getName()
								+ ", falling back to XML parsing.",
						e);
				return readDependenciesFromXml(pomFile);
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().error("Failed to read Maven dependencies for project " + project.getName(), e);
		}
		return new DependencyNode[0];
	}

	private static DependencyNode[] readDependenciesFromXml(IFile pomFile) {
		try {
			if (pomFile == null || !pomFile.exists()) {
				return new DependencyNode[0];
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

			// Get all dependency elements
			final org.w3c.dom.NodeList dependencyNodes = document.getElementsByTagName("dependency");
			if (dependencyNodes.getLength() == 0) {
				return new DependencyNode[0];
			}

			final List<DependencyNode> result = new ArrayList<>();
			for (int i = 0; i < dependencyNodes.getLength(); i++) {
				final org.w3c.dom.Element dependencyElement = (org.w3c.dom.Element) dependencyNodes.item(i);

				// Get groupId
				final org.w3c.dom.NodeList groupIdNodes = dependencyElement.getElementsByTagName("groupId");
				if (groupIdNodes.getLength() == 0) {
					continue;
				}
				final String groupId = groupIdNodes.item(0).getTextContent().trim();

				// Get artifactId
				final org.w3c.dom.NodeList artifactIdNodes = dependencyElement.getElementsByTagName("artifactId");
				if (artifactIdNodes.getLength() == 0) {
					continue;
				}
				final String artifactId = artifactIdNodes.item(0).getTextContent().trim();

				// Get version (optional)
				String version = "";
				final org.w3c.dom.NodeList versionNodes = dependencyElement.getElementsByTagName("version");
				if (versionNodes.getLength() > 0) {
					version = versionNodes.item(0).getTextContent().trim();
				}

				// Get scope (optional)
				String scope = null;
				final org.w3c.dom.NodeList scopeNodes = dependencyElement.getElementsByTagName("scope");
				if (scopeNodes.getLength() > 0) {
					scope = scopeNodes.item(0).getTextContent().trim();
				}

				// Skip dependencies with empty groupId or artifactId
				if (groupId.isEmpty() || artifactId.isEmpty()) {
					continue;
				}

				result.add(new DependencyNode(groupId, artifactId, version, scope));
			}

			return result.toArray(new DependencyNode[0]);
		} catch (Exception e) {
			Activator.getDefault().getLog().error("Failed to parse POM XML for dependencies", e);
		}
		return new DependencyNode[0];
	}

	@Override
	public int hashCode() {
		return 11 * Objects.hash(mavenProject);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		DependenciesNode other = (DependenciesNode) obj;
		return Objects.equals(mavenProject, other.mavenProject);
	}
}
