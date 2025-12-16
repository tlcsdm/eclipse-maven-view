package com.tlcsdm.eclipse.mavenview.internal.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class SelectElementsFromViewerDialog<E> extends TrayDialog {

	private static int DIALOG_WIDTH = 400;

	final CheckBoxLabelProvider labelProvider = new CheckBoxLabelProvider();
	Label messageLabel;
	Text filterText;
	TableViewer viewer;

	private E[] elements;
	private Boolean[] elementsSelection;
	private String title = "";
	private String message = "";
	private String filter = "";
	private Function<E, String> toStringFunction = object -> object == null ? "" : object.toString();

	public SelectElementsFromViewerDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(this.title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		this.messageLabel = new Label(composite, SWT.WRAP);
		this.messageLabel.setLayoutData(
				GridDataFactory.fillDefaults().hint(DIALOG_WIDTH, SWT.DEFAULT).grab(true, false).create());
		setMessage(this.message);

		this.filterText = new Text(composite, SWT.BORDER);
		this.filterText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		this.filterText.setText(this.filter);
		this.filterText.addModifyListener(e -> filterViewer());

		this.labelProvider
				.setToBooleanFunction(element -> this.elementsSelection[Arrays.asList(this.elements).indexOf(element)]);

		this.viewer = new TableViewer(composite,
				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.viewer.setLabelProvider(this.labelProvider);
		this.viewer.setContentProvider(ArrayContentProvider.getInstance());
		this.viewer.setInput(this.elements);
		this.viewer.getControl().setLayoutData(
				GridDataFactory.fillDefaults().grab(true, true).hint(DIALOG_WIDTH, SWT.DEFAULT).create());
		this.viewer.getTable().addListener(SWT.Selection, e -> validateSelection(e.item));

		filterViewer();

		return composite;
	}

	private void filterViewer() {
		if (this.elements == null)
			return;

		this.filter = this.filterText.getText().toLowerCase();
		this.viewer.setInput(Arrays.stream(this.elements).filter(element -> {
			final String elementString = this.toStringFunction.apply(element);
			return elementString.toLowerCase().contains(this.filter);
		}).toArray());
	}

	@SuppressWarnings("unchecked")
	private void validateSelection(Widget widget) {
		if (widget == null)
			return;
		final E element = (E) widget.getData();
		final Boolean elementSelection = getElementSelection(element);

		Boolean nextElementSelection = null;
		if (elementSelection == null) {
			nextElementSelection = Boolean.TRUE;
		} else if (elementSelection == Boolean.TRUE) {
			nextElementSelection = Boolean.FALSE;
		}

		selectElement(element, nextElementSelection);
	}

	public E[] getElements() {
		return this.elements;
	}

	public SelectElementsFromViewerDialog<E> elements(E[] newElements) {
		setElements(newElements);
		return this;
	}

	public void setElements(E[] elements) {
		this.elements = elements;
		this.elementsSelection = elements == null ? null : new Boolean[elements.length];

		if (this.viewer != null) {
			this.viewer.setInput(this.elements);
		}
	}

	public String getFilter() {
		return this.filter;
	}

	public SelectElementsFromViewerDialog<E> filter(String newFilter) {
		setFilter(newFilter);
		return this;
	}

	public void setFilter(String filter) {
		this.filter = Objects.requireNonNull(filter);

		if (this.filterText != null) {
			this.filterText.setText(filter);
		}
	}

	public String getTitle() {
		return this.title;
	}

	public SelectElementsFromViewerDialog<E> title(String newTitle) {
		setTitle(newTitle);
		return this;
	}

	public void setTitle(String title) {
		this.title = Objects.requireNonNull(title);

		final Shell shell = getShell();
		if (shell != null) {
			shell.setText(title);
		}
	}

	public String getMessage() {
		return this.message;
	}

	public SelectElementsFromViewerDialog<E> message(String newMessage) {
		setMessage(newMessage);
		return this;
	}

	public void setMessage(String message) {
		this.message = Objects.requireNonNull(message);

		if (this.messageLabel != null) {
			this.messageLabel.setText(message);
			((GridData) this.messageLabel.getLayoutData()).heightHint = message.isEmpty() ? 0 : SWT.DEFAULT;
			this.messageLabel.getParent().layout(true, true);
		}
	}

	public Function<E, String> getToStringFunction() {
		return this.toStringFunction;
	}

	public SelectElementsFromViewerDialog<E> toStringFunction(Function<E, String> newToStringFunction) {
		setToStringFunction(newToStringFunction);
		return this;
	}

	@SuppressWarnings("unchecked")
	public void setToStringFunction(Function<E, String> toStringFunction) {
		this.toStringFunction = Objects.requireNonNull(toStringFunction);

		this.labelProvider.setToStringFunction((Function<Object, String>) toStringFunction);
		refreshViewerIfNecessary();
	}

	private void refreshViewerIfNecessary() {
		if (this.viewer != null) {
			this.viewer.refresh();
		}
	}

	public Boolean getElementSelection(E element) {
		for (int i = 0; i < this.elements.length; i++) {
			if (element.equals(this.elements[i]))
				return this.elementsSelection[i];
		}
		throw new IllegalArgumentException("Element is unknown!");
	}

	public Object[] getSelectedElements(Boolean selected) {
		final List<E> result = new ArrayList<>();

		for (int i = 0; i < this.elements.length; i++) {
			if (this.elementsSelection[i] == selected) {
				result.add(this.elements[i]);
			}
		}
		return result.toArray();
	}

	public void selectAllElements(Boolean selected) {
		selectElements(e -> true, selected);
	}

	public void selectElements(E[] selectedElements, Boolean selected) {
		final List<E> selectedElementsList = Arrays.asList(selectedElements);
		selectElements(selectedElementsList::contains, selected);
	}

	public void selectElement(E selectedElement, Boolean selected) {
		selectElements(Predicate.isEqual(selectedElement), selected);
	}

	private void selectElements(Predicate<E> elementTester, Boolean selected) {
		Objects.requireNonNull(this.elements, "You need to setElements() before changing the selection state.");

		for (int i = 0; i < this.elements.length; i++) {
			if (elementTester.test(this.elements[i])) {
				this.elementsSelection[i] = selected;
			}
		}
		refreshViewerIfNecessary();
	}

}
