package com.tlcsdm.eclipse.mavenview.internal.common;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests all public method of a {@link FieldEditor} except for:
 *
 * <ul>
 * <li>{@link FieldEditor#isValid()}</li>
 * </ul>
 *
 * @param <E> - the field editor implementation
 * @param <I> - input to be used to test
 */

public abstract class AbstractFieldEditorTest<E extends FieldEditor, I> {

	private Shell shell;

	private E fieldEditor;

	private final String preferenceKey = UUID.randomUUID().toString();
	private final String labelText = UUID.randomUUID().toString();
	private final PreferenceStore preferenceStore = new PreferenceStore();

	private final I input;
	private final String preferenceValue;

	public AbstractFieldEditorTest(I input, String preferenceValue) {
		this.input = Objects.requireNonNull(input);
		this.preferenceValue = Objects.requireNonNull(preferenceValue);
	}

	@Before
	public void setUp() {
		this.shell = new Shell();
		this.shell.setLayout(GridLayoutFactory.fillDefaults().create());

		this.fieldEditor = createFieldEditor(this.preferenceKey, this.shell, this.labelText);
		this.fieldEditor.setPreferenceStore(this.preferenceStore);

		this.shell.open();
	}

	protected abstract E createFieldEditor(String usedPreferenceKey, Composite usedParent, String usedLabelText);

	@After
	public void tearDown() {
		this.shell.close();
		this.shell.dispose();
	}

	@Test
	public void testStore() throws Exception {
		setInput(this.fieldEditor, this.input);

		this.fieldEditor.store();

		Assert.assertEquals(this.preferenceValue, this.preferenceStore.getString(this.preferenceKey));
	}

	protected abstract void setInput(E fieldEditor, I input);

	@Test
	public void testStoreNull() throws Exception {
		setInput(this.fieldEditor, null);

		this.fieldEditor.store();

		Assert.assertEquals("", this.preferenceStore.getString(this.preferenceKey));
	}

	@Test
	public void testLoad() throws Exception {
		this.preferenceStore.setValue(this.preferenceKey, this.preferenceValue);

		this.fieldEditor.load();

		Assert.assertEquals(this.input, getInput(this.fieldEditor));
	}

	protected abstract I getInput(E usedFieldEditor);

	@Test
	public void testLoadNull() throws Exception {
		this.preferenceStore.setToDefault(this.preferenceKey);

		this.fieldEditor.load();

		Assert.assertEquals(null, getInput(this.fieldEditor));
	}

	@Test
	public void testLoadDefault() throws Exception {
		this.preferenceStore.setDefault(this.preferenceKey, this.preferenceValue);

		this.fieldEditor.loadDefault();

		Assert.assertEquals(this.input, getInput(this.fieldEditor));
	}

	@Test
	public void testLoadDefaultNull() throws Exception {
		this.preferenceStore.setDefault(this.preferenceKey, "");

		this.fieldEditor.loadDefault();

		Assert.assertEquals(null, getInput(this.fieldEditor));
	}

	@Test
	public void testGetPreferenceName() throws Exception {
		Assert.assertEquals(this.preferenceKey, this.fieldEditor.getPreferenceName());
	}

	@Test
	public void testGetLabelTextDefault() throws Exception {
		Assert.assertEquals(this.labelText, this.fieldEditor.getLabelText());
	}

	@Test
	public void testSetLabelText() throws Exception {
		final String newLabelText = UUID.randomUUID().toString();
		this.fieldEditor.setLabelText(newLabelText);

		Assert.assertEquals(newLabelText, this.fieldEditor.getLabelText());
	}

	@Test
	public void testGetFieldEditorFontName() throws Exception {
		Assert.assertNotNull(this.fieldEditor.getFieldEditorFontName());
	}

	@Test
	public void testGetNumberOfControls() throws Exception {
		Assert.assertEquals(this.shell.getChildren().length, this.fieldEditor.getNumberOfControls());
	}

	@Test
	public void testDispose() throws Exception {
		final Control[] children = this.shell.getChildren();

		this.fieldEditor.dispose();

		for (final Control child : children) {
			Assert.assertTrue(child.isDisposed());
		}
	}

	@Test
	public void testGetLabelControl() throws Exception {
		Assert.assertSame(this.shell.getChildren()[0], this.fieldEditor.getLabelControl(this.shell));
	}

	@Test
	public void testSetPreferenceStore() throws Exception {
		final PreferenceStore newPreferenceStore = new PreferenceStore();
		newPreferenceStore.setValue(this.preferenceKey, this.preferenceValue);

		this.fieldEditor.setPreferenceStore(newPreferenceStore);
		Assert.assertSame(newPreferenceStore, this.fieldEditor.getPreferenceStore());

		this.fieldEditor.load();
		Assert.assertEquals(this.input, getInput(this.fieldEditor));
	}

	@Test
	public void testSetPreferenceName() throws Exception {
		final String newPreferenceKey = UUID.randomUUID().toString();
		this.preferenceStore.setValue(newPreferenceKey, this.preferenceValue);

		this.fieldEditor.setPreferenceName(newPreferenceKey);

		this.fieldEditor.load();
		Assert.assertEquals(this.input, getInput(this.fieldEditor));
	}

	@Test
	public void testPresentsDefaultValue() throws Exception {
		this.fieldEditor.load();
		Assert.assertFalse(this.fieldEditor.presentsDefaultValue());

		this.fieldEditor.loadDefault();
		Assert.assertTrue(this.fieldEditor.presentsDefaultValue());
	}

	@Test
	public void testSetPropertyChangeListener() throws Exception {
		this.fieldEditor.loadDefault();

		final boolean[] called = {false};
		final IPropertyChangeListener listener = e -> {
			called[0] = true;
		};
		this.fieldEditor.setPropertyChangeListener(listener);

		setInput(this.fieldEditor, this.input);

		Assert.assertTrue(called[0]);
	}

	@Test
	public void testSetFocus() throws Exception {
		this.fieldEditor.setFocus();

		final Control control = this.shell.getChildren()[1];
		Assert.assertTrue("Expected this control to have focus: " + control, control.isFocusControl());
	}

	@Test
	public void testSetEnabledFalse() throws Exception {
		this.fieldEditor.setEnabled(false, this.shell);

		final Control control = this.shell.getChildren()[1];
		Assert.assertFalse("Expected this control to be disabled: " + this.fieldEditor.getLabelControl(this.shell),
				this.fieldEditor.getLabelControl(this.shell).getEnabled());
		Assert.assertFalse("Expected this control to be disabled: " + control, control.getEnabled());
	}

	@Test
	public void testSetEnabledTrue() throws Exception {
		this.fieldEditor.setEnabled(true, this.shell);

		final Control control = this.shell.getChildren()[1];
		Assert.assertTrue("Expected this control to be enabled: " + this.fieldEditor.getLabelControl(this.shell),
				this.fieldEditor.getLabelControl(this.shell).getEnabled());
		Assert.assertTrue("Expected this control to be enabled: " + control, control.getEnabled());
	}

}
