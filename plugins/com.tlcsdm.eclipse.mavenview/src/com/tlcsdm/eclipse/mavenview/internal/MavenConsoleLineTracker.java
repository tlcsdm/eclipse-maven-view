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
