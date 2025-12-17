package com.tlcsdm.eclipse.mavenview;

import java.util.Objects;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.tlcsdm.eclipse.mavenview"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		syncCommandStateFromPreference();
	}

	public static void syncCommandStateFromPreference() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean prefValue = store.getBoolean(MavenViewPreferences.SKIP_TESTS);
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("com.tlcsdm.eclipse.mavenview.commands.skipTests");
		State state = command.getState("org.eclipse.ui.commands.toggleState");
		if (state != null) {
			state.setValue(prefValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static Image getImage(String imagePath) {
		final ImageRegistry imageRegistry = JFaceResources.getImageRegistry();

		final String key = PLUGIN_ID + '/' + imagePath;
		Image result = imageRegistry.get(key);
		if (result == null) {
			imageRegistry.put(key, imageDescriptorFromPlugin(PLUGIN_ID, imagePath));
			result = imageRegistry.get(key);
		}
		return Objects.requireNonNull(result, "Image '" + imagePath + "' was not found!");
	}
}
