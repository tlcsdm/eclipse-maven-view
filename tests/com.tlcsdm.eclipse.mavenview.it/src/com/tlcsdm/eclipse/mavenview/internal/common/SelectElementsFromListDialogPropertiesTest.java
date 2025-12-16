package com.tlcsdm.eclipse.mavenview.internal.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Table;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SelectElementsFromListDialogPropertiesTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		final List<Object[]> result = new ArrayList<>();

		result.addAll(createDataWithoutArrange("elements", dialog -> {
			Assert.assertArrayEquals(Element.values(), dialog.getElements());
			Assert.assertEquals(5, dialog.viewer.getTable().getItemCount());
		}, dialog -> dialog.setElements(Element.values()), dialog -> dialog.elements(Element.values())));

		final String filter = "a";
		result.addAll(createDataWithElements("filter", dialog -> {
			Assert.assertEquals(filter, dialog.getFilter());
			Assert.assertEquals(filter, dialog.filterText.getText());
			Assert.assertEquals(1, dialog.viewer.getTable().getItemCount());
		}, dialog -> dialog.setFilter(filter), dialog -> dialog.filter(filter)));

		final String title = UUID.randomUUID().toString();
		result.addAll(createDataWithoutArrange("title", dialog -> {
			Assert.assertEquals(title, dialog.getTitle());
			Assert.assertEquals(title, dialog.getShell().getText());
		}, dialog -> dialog.setTitle(title), dialog -> dialog.title(title)));

		final String message = UUID.randomUUID().toString();
		result.addAll(createDataWithoutArrange("message", dialog -> {
			Assert.assertEquals(message, dialog.getMessage());
			Assert.assertEquals(message, dialog.messageLabel.getText());
		}, dialog -> dialog.setMessage(message), dialog -> dialog.message(message)));

		result.addAll(createDataWithElements("setToStringFunction", dialog -> {
			final Table table = dialog.viewer.getTable();

			Assert.assertEquals(5, table.getItemCount());
			Assert.assertEquals("one", table.getItem(0).getText());
			Assert.assertEquals("two", table.getItem(1).getText());
			Assert.assertEquals("three", table.getItem(2).getText());
		}, dialog -> dialog.setToStringFunction(Element::getDisplayName),
				dialog -> dialog.toStringFunction(Element::getDisplayName)));

		final Element[] selectedElementsTrue = {Element.A, Element.B};
		result.addAll(createData("selectedElementsTrue", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(Boolean.FALSE);
		}, dialog -> {
			Assert.assertArrayEquals(selectedElementsTrue, dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[]{Element.C, Element.D, Element.E},
					dialog.getSelectedElements(Boolean.FALSE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(null));
		}, dialog -> dialog.selectElements(selectedElementsTrue, Boolean.TRUE)));

		final Element[] selectedElementsFalse = {Element.C, Element.D};
		result.addAll(createData("selectedElementsFalse", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(null);
		}, dialog -> {
			Assert.assertArrayEquals(selectedElementsFalse, dialog.getSelectedElements(Boolean.FALSE));
			Assert.assertArrayEquals(new Element[]{Element.A, Element.B, Element.E}, dialog.getSelectedElements(null));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.TRUE));
		}, dialog -> dialog.selectElements(selectedElementsFalse, Boolean.FALSE)));

		final Element[] selectedElementsNull = {Element.E};
		result.addAll(createData("selectedElementsNull", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(Boolean.TRUE);
		}, dialog -> {
			Assert.assertArrayEquals(selectedElementsNull, dialog.getSelectedElements(null));
			Assert.assertArrayEquals(new Element[]{Element.A, Element.B, Element.C, Element.D},
					dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.FALSE));
		}, dialog -> dialog.selectElements(selectedElementsNull, null)));

		result.addAll(createData("selectAllElementsTrue", dialog -> {
			dialog.setElements(Element.values());
		}, dialog -> {
			Assert.assertArrayEquals(Element.values(), dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.FALSE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(null));
		}, dialog -> dialog.selectAllElements(Boolean.TRUE)));

		result.addAll(createData("selectAllElementsFalse", dialog -> {
			dialog.setElements(Element.values());
		}, dialog -> {
			Assert.assertArrayEquals(Element.values(), dialog.getSelectedElements(Boolean.FALSE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(null));
		}, dialog -> dialog.selectAllElements(Boolean.FALSE)));

		result.addAll(createData("selectAllElementsNull", dialog -> {
			dialog.setElements(Element.values());
		}, dialog -> {
			Assert.assertArrayEquals(Element.values(), dialog.getSelectedElements(null));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.FALSE));
		}, dialog -> dialog.selectAllElements(null)));

		result.addAll(createData("selectedElementTrue", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(Boolean.FALSE);
		}, dialog -> {
			Assert.assertArrayEquals(new Element[]{Element.A}, dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[]{Element.B, Element.C, Element.D, Element.E},
					dialog.getSelectedElements(Boolean.FALSE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(null));
		}, dialog -> dialog.selectElement(Element.A, Boolean.TRUE)));

		result.addAll(createData("selectedElementFalse", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(null);
		}, dialog -> {
			Assert.assertArrayEquals(new Element[]{Element.D}, dialog.getSelectedElements(Boolean.FALSE));
			Assert.assertArrayEquals(new Element[]{Element.A, Element.B, Element.C, Element.E},
					dialog.getSelectedElements(null));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.TRUE));
		}, dialog -> dialog.selectElement(Element.D, Boolean.FALSE)));

		result.addAll(createData("selectedElementNull", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(Boolean.TRUE);
		}, dialog -> {
			Assert.assertArrayEquals(new Element[]{Element.E}, dialog.getSelectedElements(null));
			Assert.assertArrayEquals(new Element[]{Element.A, Element.B, Element.C, Element.D},
					dialog.getSelectedElements(Boolean.TRUE));
			Assert.assertArrayEquals(new Element[0], dialog.getSelectedElements(Boolean.FALSE));
		}, dialog -> dialog.selectElement(Element.E, null)));

		result.addAll(createData("getElementSelectionTrue", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(Boolean.FALSE);
		}, dialog -> {
			Assert.assertEquals(Boolean.TRUE, dialog.getElementSelection(Element.A));
		}, dialog -> dialog.selectElement(Element.A, Boolean.TRUE)));

		result.addAll(createData("getElementSelectionFalse", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(null);
		}, dialog -> {
			Assert.assertEquals(Boolean.FALSE, dialog.getElementSelection(Element.D));
		}, dialog -> dialog.selectElement(Element.D, Boolean.FALSE)));

		result.addAll(createData("getElementSelectionNull", dialog -> {
			dialog.setElements(Element.values());
			dialog.selectAllElements(Boolean.TRUE);
		}, dialog -> {
			Assert.assertEquals(null, dialog.getElementSelection(Element.E));
		}, dialog -> dialog.selectElement(Element.E, null)));

		return result;
	}

	@SafeVarargs
	private static List<Object[]> createDataWithoutArrange(String displayName,
			Consumer<SelectElementsFromViewerDialog<Element>> assertion,
			Consumer<SelectElementsFromViewerDialog<Element>>... acts) {
		return createData(displayName, dialog -> {
			// no arrangement
		}, assertion, acts);
	}

	@SafeVarargs
	private static List<Object[]> createDataWithElements(String displayName,
			Consumer<SelectElementsFromViewerDialog<Element>> assertion,
			Consumer<SelectElementsFromViewerDialog<Element>>... acts) {
		return createData(displayName, dialog -> dialog.setElements(Element.values()), assertion, acts);
	}

	@SafeVarargs
	private static List<Object[]> createData(String displayName,
			Consumer<SelectElementsFromViewerDialog<Element>> arrange,
			Consumer<SelectElementsFromViewerDialog<Element>> assertion,
			Consumer<SelectElementsFromViewerDialog<Element>>... acts) {
		final List<Object[]> result = new ArrayList<>();

		int index = 0;
		for (final Consumer<SelectElementsFromViewerDialog<Element>> act : acts) {
			final String name = acts.length == 1 ? displayName : displayName + '[' + index + ']';
			result.add(new Object[]{name, arrange, act, assertion});
			index++;
		}

		return result;
	}

	static enum Element {
		A("one"),

		B("two"),

		C("three"),

		D("four"),

		E("five");

		String displayName;

		private Element(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return this.displayName;
		}
	}

	private final Consumer<SelectElementsFromViewerDialog<Element>> arrange;
	private final Consumer<SelectElementsFromViewerDialog<Element>> act;
	private final Consumer<SelectElementsFromViewerDialog<Element>> assertion;

	private SelectElementsFromViewerDialog<Element> dialog;

	public SelectElementsFromListDialogPropertiesTest(@SuppressWarnings("unused") String displayName,
			Consumer<SelectElementsFromViewerDialog<Element>> arrange,
			Consumer<SelectElementsFromViewerDialog<Element>> act,
			Consumer<SelectElementsFromViewerDialog<Element>> assertion) {
		this.arrange = arrange;
		this.act = act;
		this.assertion = assertion;
	}

	@Before
	public void setUp() {
		this.dialog = new SelectElementsFromViewerDialog<>(null);
		this.dialog.setBlockOnOpen(false);
	}

	@After
	public void tearDown() {
		this.dialog.close();
	}

	@Test
	public void testOpenLast() throws Exception {
		this.arrange.accept(this.dialog);
		this.act.accept(this.dialog);

		this.dialog.open();

		this.assertion.accept(this.dialog);
	}

	@Test
	public void testActLast() throws Exception {
		this.arrange.accept(this.dialog);
		this.dialog.open();

		this.act.accept(this.dialog);

		this.assertion.accept(this.dialog);
	}
}
