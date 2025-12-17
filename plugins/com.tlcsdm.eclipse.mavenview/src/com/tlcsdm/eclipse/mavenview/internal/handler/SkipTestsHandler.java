package com.tlcsdm.eclipse.mavenview.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;

public class SkipTestsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("com.tlcsdm.eclipse.mavenview.commands.skipTests");
		State state = command.getState("org.eclipse.ui.commands.toggleState");
		if (state != null) {
			boolean checked = Boolean.TRUE.equals(state.getValue());
			boolean newValue = !checked;
			state.setValue(newValue);

			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			store.setValue(MavenViewPreferences.SKIP_TESTS, newValue);
		}

		return null;
	}

}
