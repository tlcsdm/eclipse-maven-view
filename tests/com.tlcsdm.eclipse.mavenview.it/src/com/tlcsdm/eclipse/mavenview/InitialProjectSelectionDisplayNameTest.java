package com.tlcsdm.eclipse.mavenview;

import org.junit.Assert;
import org.junit.Test;

public class InitialProjectSelectionDisplayNameTest {

	@Test
	public void testDisplayName() throws Exception {
		for (final InitialProjectSelection value : InitialProjectSelection.values()) {
			final String error = value + " is missing the display name!";
			final String displayName = value.getDisplayName();
			Assert.assertNotNull(error, displayName);
			Assert.assertFalse(error, displayName.startsWith("!"));
			Assert.assertFalse(error, displayName.endsWith("!"));
		}
	}
}
