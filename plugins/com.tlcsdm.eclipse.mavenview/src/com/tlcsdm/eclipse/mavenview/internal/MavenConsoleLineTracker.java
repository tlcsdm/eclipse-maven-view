package com.tlcsdm.eclipse.mavenview.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.tlcsdm.eclipse.mavenview.MavenRunConfig;
import com.tlcsdm.eclipse.mavenview.MavenRunner;

/**
 * A console line tracker that enhances Maven-related Java launch consoles by
 * injecting Maven execution context information at console initialization time.
 * <p>
 * This tracker is registered via the
 * {@code org.eclipse.debug.ui.consoleLineTrackers} extension point and is bound
 * to Java processes ({@code processType="java"}). When a Java process is
 * started through a Maven launch configuration, this tracker retrieves the
 * associated {@link ILaunchConfiguration} and extracts the
 * {@link MavenRunConfig} stored in its attributes.
 * </p>
 *
 * <p>
 * If a {@link MavenRunConfig} is present, a formatted header is inserted at the
 * very beginning of the console document. The header typically includes:
 * </p>
 * <ul>
 * <li>The Maven working directory</li>
 * <li>The executed Maven phases or goals</li>
 * </ul>
 *
 * <p>
 * The injected output follows Maven-style log formatting (e.g. {@code [INFO]}
 * prefixes) to remain visually consistent with standard Maven console output.
 * </p>
 *
 * <p>
 * This implementation does <strong>not</strong> process individual console
 * lines. The {@link #lineAppended(IRegion)} method is intentionally left
 * unused, making this tracker a lightweight, initialization-only console
 * enhancement.
 * </p>
 *
 * <p>
 * Any exceptions encountered during initialization (for example, missing launch
 * attributes or document modification errors) are deliberately ignored to
 * ensure that console rendering and Maven execution are never disrupted.
 * </p>
 *
 * @see org.eclipse.debug.ui.console.IConsoleLineTracker
 * @see org.eclipse.debug.core.ILaunchConfiguration
 * @see org.eclipse.jface.text.IDocument
 */
public class MavenConsoleLineTracker implements IConsoleLineTracker {

	private static final String CONSOLE_PREFIX = "[INFO] ";
	private static final String CONSOLE_SEPARATOR = "------------------------------------------------------------------------";
	private static final String CONSOLE_LS = System.lineSeparator();

	@Override
	public void init(IConsole console) {
		try {
			final ILaunchConfiguration launchConfig = console.getProcess().getLaunch().getLaunchConfiguration();
			final MavenRunConfig mavenConfig = launchConfig == null ? null
					: (MavenRunConfig) launchConfig.getAttributes().get(MavenRunner.ATTR_CONFIG);

			if (mavenConfig != null) {
				final IDocument document = console.getDocument();
				document.replace(0, 0, createConfigInfo(launchConfig, mavenConfig));
			}
		} catch (final CoreException | BadLocationException e) {
			// we can ignore that
		}
	}

	private static String createConfigInfo(ILaunchConfiguration launchConfig, MavenRunConfig mavenConfig) {
		final StringBuilder result = new StringBuilder();

		result.append(CONSOLE_PREFIX + CONSOLE_SEPARATOR);
		result.append(CONSOLE_LS);

		try {
			appendConfigInfo(result, Messages.getString("WorkingDirectory"),
					launchConfig.getAttribute(MavenRunner.ATTR_WORKING_DIRECTORY, (String) null));
		} catch (final CoreException e) {
			// we can ignore that
			appendConfigInfo(result, Messages.getString("WorkingDirectory"), e.getMessage());
		}
		appendConfigInfo(result, Messages.getString("Phases"), mavenConfig.getPhasesAsString());

		result.append(CONSOLE_PREFIX + CONSOLE_SEPARATOR);
		result.append(CONSOLE_LS);

		return result.toString();
	}

	private static void appendConfigInfo(StringBuilder result, String key, String value) {
		result.append(CONSOLE_PREFIX).append(key).append(": ").append(value).append(CONSOLE_LS);
	}

	@Override
	public void lineAppended(IRegion line) {
		// not used
	}

	@Override
	public void dispose() {
		// not used
	}
}
