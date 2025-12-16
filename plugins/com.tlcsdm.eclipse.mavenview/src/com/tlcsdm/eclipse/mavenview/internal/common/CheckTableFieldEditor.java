package com.tlcsdm.eclipse.mavenview.internal.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class CheckTableFieldEditor extends FieldEditor {

	public static final String DATA_ID = "id";
	private static final String SEPARATOR = "\n";

	public interface PreferenceLabelProvider extends ILabelProvider {

		String getPreference(Object element);
	}

	public static class DefaultPreferenceLabelProvider extends LabelProvider implements PreferenceLabelProvider {

		@Override
		public String getPreference(Object element) {
			return getText(element);
		}
	}

	TableViewer tableViewer;
	private ISelection oldSelection;

	private PreferenceLabelProvider labelProvider = new DefaultPreferenceLabelProvider();
	private Object[] input = new Object[0];
	private int heightHint = SWT.DEFAULT;

	public CheckTableFieldEditor(String name, Composite parent, String labelText) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	@Override
	protected void createControl(Composite parent) {
		getLabelControl(parent)
				.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).indent(0, 4).create());

		this.tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.CHECK);
		this.tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.tableViewer.setLabelProvider(this.labelProvider);
		this.tableViewer.setInput(this.input);
		this.tableViewer.addSelectionChangedListener(e -> valueChanged());

		final Table table = this.tableViewer.getTable();
		table.setLayoutData(createLayoutData());
		table.setData(DATA_ID, getPreferenceName());

		super.createControl(parent);
	}

	private GridData createLayoutData() {
		return GridDataFactory.fillDefaults().grab(true, this.heightHint == SWT.DEFAULT)
				.hint(SWT.DEFAULT, this.heightHint == SWT.DEFAULT ? 1 : this.heightHint).create();
	}

	void valueChanged() {
		setPresentsDefaultValue(false);

		final ISelection newSelection = this.tableViewer.getSelection();
		if (!Objects.equals(newSelection, this.oldSelection)) {
			fireStateChanged(IS_VALID, false, true);
			fireValueChanged(VALUE, this.oldSelection, newSelection);
			this.oldSelection = newSelection;
		}
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		final GridData gd = (GridData) this.tableViewer.getControl().getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		adjustForNumColumns(numColumns);
	}

	@Override
	protected void doLoad() {
		doLoad(getPreferenceStore().getString(getPreferenceName()));
	}

	private void doLoad(String valuesAsString) {
		final Set<String> valuesAsStringSet = new TreeSet<>(Arrays.asList(valuesAsString.split(SEPARATOR)));
		final Set<Object> valuesAsSet = Arrays.stream(this.input)
				.filter(i -> valuesAsStringSet.contains(this.labelProvider.getPreference(i)))
				.collect(Collectors.toSet());

		for (final TableItem tableItem : this.tableViewer.getTable().getItems()) {
			tableItem.setChecked(valuesAsSet.contains(tableItem.getData()));
		}
	}

	@Override
	protected void doLoadDefault() {
		doLoad(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	@Override
	protected void doStore() {
		final StringBuilder preferenceValue = new StringBuilder();
		for (final TableItem tableItem : this.tableViewer.getTable().getItems()) {
			if (tableItem.getChecked()) {
				if (preferenceValue.length() > 0) {
					preferenceValue.append(SEPARATOR);
				}
				preferenceValue.append(this.labelProvider.getPreference(tableItem.getData()));
			}
		}
		getPreferenceStore().setValue(getPreferenceName(), preferenceValue.toString());
	}

	@Override
	public void setFocus() {
		this.tableViewer.getTable().forceFocus();
	}

	@Override
	public void dispose() {
		getLabelControl().dispose();
		this.tableViewer.getTable().dispose();
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		this.tableViewer.getTable().setEnabled(enabled);
	}

	public PreferenceLabelProvider getLabelProvider() {
		return this.labelProvider;
	}

	public CheckTableFieldEditor labelProvider(PreferenceLabelProvider newLabelProvider) {
		setLabelProvider(newLabelProvider);
		return this;
	}

	public void setLabelProvider(PreferenceLabelProvider labelProvider) {
		this.labelProvider = Objects.requireNonNull(labelProvider);
		if (this.tableViewer != null) {
			this.tableViewer.setLabelProvider(labelProvider);
		}
	}

	public Object[] getInput() {
		return this.input;
	}

	public CheckTableFieldEditor input(Object[] newInput) {
		setInput(newInput);
		return this;
	}

	public void setInput(Object[] input) {
		this.input = Objects.requireNonNull(input);
		if (this.tableViewer != null) {
			this.tableViewer.setInput(input);
		}
	}

	public int getHeightHint() {
		return this.heightHint;
	}

	public CheckTableFieldEditor heightHint(int newHeightHint) {
		setHeightHint(newHeightHint);
		return this;
	}

	public void setHeightHint(int heightHint) {
		this.heightHint = heightHint;

		this.tableViewer.getControl().setLayoutData(createLayoutData());
		this.tableViewer.getControl().getParent().layout(true, true);
	}

}
