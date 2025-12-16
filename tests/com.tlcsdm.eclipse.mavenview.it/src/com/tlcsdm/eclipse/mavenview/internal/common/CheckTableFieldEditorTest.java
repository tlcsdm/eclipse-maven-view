package com.tlcsdm.eclipse.mavenview.internal.common;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.tlcsdm.eclipse.mavenview.internal.common.CheckTableFieldEditor.PreferenceLabelProvider;

public class CheckTableFieldEditorTest
		extends AbstractFieldEditorTest<CheckTableFieldEditor, Set<CheckTableFieldEditorTest.Example>> {

	static enum Example {
		A,

		B,

		C;
	}

	private CheckTableFieldEditor tableFieldEditor;

	public CheckTableFieldEditorTest() {
		super(new TreeSet<>(Arrays.asList(Example.A, Example.B)), "A\nB");
	}

	@Override
	protected CheckTableFieldEditor createFieldEditor(String preferenceKey, Composite parent, String labelText) {
		this.tableFieldEditor = new CheckTableFieldEditor(preferenceKey, parent, labelText);
		this.tableFieldEditor.setInput(Example.values());
		return this.tableFieldEditor;
	}

	@Override
	protected void setInput(CheckTableFieldEditor fieldEditor, Set<Example> input) {
		for (final TableItem tableItem : fieldEditor.tableViewer.getTable().getItems()) {
			tableItem.setChecked(input != null && input.contains(tableItem.getData()));
		}
		// we'll go around the official API of the field editor so we need to call this
		// method too
		fieldEditor.valueChanged();
	}

	@Override
	protected Set<Example> getInput(CheckTableFieldEditor fieldEditor) {
		final Set<Example> result = new TreeSet<>();
		for (final TableItem tableItem : fieldEditor.tableViewer.getTable().getItems()) {
			if (tableItem.getChecked()) {
				result.add((Example) tableItem.getData());
			}
		}
		return result.isEmpty() ? null : result;
	}

	@Test
	public void testIsValid() throws Exception {
		Assert.assertTrue(this.tableFieldEditor.isValid());
	}

	@Test
	public void testSetLabelProvider() throws Exception {
		class TestLabelProvider extends LabelProvider implements PreferenceLabelProvider {

			@Override
			public String getText(Object element) {
				return super.getText(element).toLowerCase();
			}

			@Override
			public String getPreference(Object element) {
				return String.valueOf(((Example) element).ordinal());
			}

		}

		final TestLabelProvider labelProvider = new TestLabelProvider();
		this.tableFieldEditor.setLabelProvider(labelProvider);
		Assert.assertSame(labelProvider, this.tableFieldEditor.getLabelProvider());

		setInput(this.tableFieldEditor, new TreeSet<>(Arrays.asList(Example.A, Example.C)));

		Assert.assertEquals("a", this.tableFieldEditor.tableViewer.getTable().getItem(0).getText(0));
		Assert.assertEquals("b", this.tableFieldEditor.tableViewer.getTable().getItem(1).getText(0));
		Assert.assertEquals("c", this.tableFieldEditor.tableViewer.getTable().getItem(2).getText(0));

		this.tableFieldEditor.store();
		Assert.assertEquals("0\n2",
				this.tableFieldEditor.getPreferenceStore().getString(this.tableFieldEditor.getPreferenceName()));
	}

	@Test
	public void testLabelProvider() throws Exception {
		class TestLabelProvider extends LabelProvider implements PreferenceLabelProvider {

			@Override
			public String getPreference(Object element) {
				return String.valueOf(((Example) element).ordinal());
			}

		}

		final TestLabelProvider labelProvider = new TestLabelProvider();
		this.tableFieldEditor.labelProvider(labelProvider);
		Assert.assertSame(labelProvider, this.tableFieldEditor.getLabelProvider());

		setInput(this.tableFieldEditor, new TreeSet<>(Arrays.asList(Example.B)));

		Assert.assertEquals("A", this.tableFieldEditor.tableViewer.getTable().getItem(0).getText(0));
		Assert.assertEquals("B", this.tableFieldEditor.tableViewer.getTable().getItem(1).getText(0));
		Assert.assertEquals("C", this.tableFieldEditor.tableViewer.getTable().getItem(2).getText(0));

		this.tableFieldEditor.store();
		Assert.assertEquals("1",
				this.tableFieldEditor.getPreferenceStore().getString(this.tableFieldEditor.getPreferenceName()));
	}

	@Test
	public void testSetInput() throws Exception {
		final Example[] input = new Example[]{Example.A, Example.C};
		this.tableFieldEditor.setInput(input);
		Assert.assertArrayEquals(input, this.tableFieldEditor.getInput());
		Assert.assertEquals(2, this.tableFieldEditor.tableViewer.getTable().getItemCount());
	}

	@Test
	public void testInput() throws Exception {
		final Example[] input = new Example[]{Example.A};
		this.tableFieldEditor.input(input);
		Assert.assertArrayEquals(input, this.tableFieldEditor.getInput());
		Assert.assertEquals(1, this.tableFieldEditor.tableViewer.getTable().getItemCount());
	}

	@Test
	public void testSetHeightHint() throws Exception {
		final int heightHint = SWT.DEFAULT;
		this.tableFieldEditor.setHeightHint(heightHint);
		Assert.assertEquals(heightHint, this.tableFieldEditor.getHeightHint());
		Assert.assertEquals(1, ((GridData) this.tableFieldEditor.tableViewer.getTable().getLayoutData()).heightHint);
	}

	@Test
	public void testHeightHint() throws Exception {
		final int heightHint = SWT.DEFAULT;
		this.tableFieldEditor.heightHint(heightHint);
		Assert.assertEquals(heightHint, this.tableFieldEditor.getHeightHint());
		Assert.assertEquals(1, ((GridData) this.tableFieldEditor.tableViewer.getTable().getLayoutData()).heightHint);
	}

	@Ignore("This test does not work for Travis")
	@Override
	public void testSetFocus() throws Exception {
		// remove
	}
}
