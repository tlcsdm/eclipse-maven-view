package com.tlcsdm.eclipse.mavenview.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

	private static final String BUNDLE_NAME = "com.tlcsdm.eclipse.mavenview.internal.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	private Messages() {
		// hidden
	}

}
