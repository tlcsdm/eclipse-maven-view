package com.tlcsdm.eclipse.mavenview.internal.common;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CheckBoxLabelProvider extends LabelProvider {

	private static final String IMAGE_KEY_CHECKED = "checkedImageCheckBox";
	private static final String IMAGE_KEY_UNCHECKED = "uncheckedImageCheckBox";
	private static final String IMAGE_KEY_GRAYED = "grayedImageCheckBox";

	static {
		if (JFaceResources.getImageRegistry().getDescriptor(IMAGE_KEY_CHECKED) == null) {
			JFaceResources.getImageRegistry().put(IMAGE_KEY_CHECKED, createImageFromCheckBox(Boolean.TRUE));
			JFaceResources.getImageRegistry().put(IMAGE_KEY_UNCHECKED, createImageFromCheckBox(Boolean.FALSE));
			JFaceResources.getImageRegistry().put(IMAGE_KEY_GRAYED, createImageFromCheckBox(null));
		}
	}

	private static Image createImageFromCheckBox(Boolean type) {
		final Shell shell = new Shell(SWT.NO_TRIM);

		final Button button = new Button(shell, SWT.CHECK);
		if (type != null) {
			button.setSelection(type.booleanValue());
		} else {
			button.setSelection(true);
			button.setGrayed(true);
		}
		final Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		button.setSize(buttonSize);
		button.setLocation(0, 0);

		shell.setSize(buttonSize);
		shell.open();

		final GC gc = new GC(button);
		final Image image = new Image(Display.getCurrent(), buttonSize.x, buttonSize.y);
		gc.copyArea(image, 0, 0);

		gc.dispose();
		shell.close();

		return image;
	}

	private Function<Object, Boolean> toBooleanFunction = o -> o instanceof Boolean ? (Boolean) o : null;
	@SuppressWarnings("synthetic-access")
	private Function<Object, String> toStringFunction = super::getText;

	@Override
	public String getText(Object element) {
		return this.toStringFunction.apply(element);
	}

	@Override
	public Image getImage(Object element) {
		final Boolean bool = this.toBooleanFunction.apply(element);
		if (bool == null)
			return JFaceResources.getImageRegistry().get(IMAGE_KEY_GRAYED);
		if (bool.booleanValue())
			return JFaceResources.getImageRegistry().get(IMAGE_KEY_CHECKED);
		return JFaceResources.getImageRegistry().get(IMAGE_KEY_UNCHECKED);
	}

	public Function<Object, Boolean> getToBooleanFunction() {
		return this.toBooleanFunction;
	}

	public CheckBoxLabelProvider toBooleanFunction(Function<Object, Boolean> newToBooleanFunction) {
		setToBooleanFunction(newToBooleanFunction);
		return this;
	}

	public void setToBooleanFunction(Function<Object, Boolean> toBooleanFunction) {
		this.toBooleanFunction = Objects.requireNonNull(toBooleanFunction);
	}

	public Function<Object, String> getToStringFunction() {
		return this.toStringFunction;
	}

	public CheckBoxLabelProvider toStringFunction(Function<Object, String> newToStringFunction) {
		setToStringFunction(newToStringFunction);
		return this;
	}

	public void setToStringFunction(Function<Object, String> toStringFunction) {
		this.toStringFunction = Objects.requireNonNull(toStringFunction);
	}

}