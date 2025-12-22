package com.tlcsdm.eclipse.mavenview.internal;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.tlcsdm.eclipse.mavenview.Activator;
import com.tlcsdm.eclipse.mavenview.Displayable;
import com.tlcsdm.eclipse.mavenview.MavenViewPreferences;
import com.tlcsdm.eclipse.mavenview.Phase;
import com.tlcsdm.eclipse.mavenview.internal.tree.DependencyNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.PhaseNode;
import com.tlcsdm.eclipse.mavenview.internal.tree.ProfileNode;

public class DisplayableLabelProvider extends StyledCellLabelProvider {

	private static final String TEST_SCOPE_SUFFIX = " (test)";

	private final Font grayFont;

	public DisplayableLabelProvider() {
		FontData[] fontData = getDefaultFont().getFontData();
		for (FontData data : fontData) {
			data.setStyle(SWT.ITALIC); // 设置斜体
		}
		grayFont = new Font(null, fontData); // 灰色字体
	}

	@Override
	public void update(ViewerCell cell) {
		Object obj = cell.getElement(); // 当前对象
		if (obj instanceof PhaseNode) {
			PhaseNode phaseNode = (PhaseNode) obj;
			if (phaseNode.getDisplayName().equals(Phase.TEST.getDisplayName())) {
				boolean skipTests = Activator.getDefault().getPreferenceStore()
						.getBoolean(MavenViewPreferences.SKIP_TESTS);
				if (skipTests) {
					TextStyle textStyle = new TextStyle(cell.getFont(), null, null);
					textStyle.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
					StyleRange styleRange = new StyleRange(textStyle);
					styleRange.start = 0;
					styleRange.length = cell.getText().length();
					styleRange.fontStyle = SWT.ITALIC;
					cell.setText(phaseNode.getDisplayName());
					cell.setStyleRanges(new StyleRange[] { styleRange });
				} else {
					cell.setText(phaseNode.getDisplayName());
					cell.setFont(getDefaultFont());
					cell.setStyleRanges(new StyleRange[] {});
				}
			} else {
				cell.setText(phaseNode.getDisplayName());
				cell.setFont(getDefaultFont());
			}
			cell.setImage(phaseNode.getImage());
		} else if (obj instanceof ProfileNode) {
			ProfileNode profileNode = (ProfileNode) obj;
			// Profile nodes now use different icons for checked/unchecked state
			// No need for text prefix - the icon shows the state
			String displayText = profileNode.getDisplayName();
			
			if (profileNode.isSelected()) {
				// Selected profiles: bold text with blue color
				TextStyle textStyle = new TextStyle(cell.getFont(), null, null);
				textStyle.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE);
				StyleRange styleRange = new StyleRange(textStyle);
				styleRange.start = 0;
				styleRange.length = displayText.length();
				styleRange.fontStyle = SWT.BOLD;
				cell.setText(displayText);
				cell.setStyleRanges(new StyleRange[] { styleRange });
			} else {
				// Unselected profiles: normal text, aligned with other child nodes
				cell.setText(displayText);
				cell.setFont(getDefaultFont());
				cell.setStyleRanges(new StyleRange[] {});
			}
			cell.setImage(profileNode.getImage());
		} else if (obj instanceof DependencyNode) {
			DependencyNode dependencyNode = (DependencyNode) obj;
			String displayName = dependencyNode.getDisplayName();
			
			if (dependencyNode.isTestScope()) {
				// Test scope dependencies: show with gray "(test)" suffix
				String fullText = displayName + TEST_SCOPE_SUFFIX;
				cell.setText(fullText);
				
				// Style the "(test)" suffix in gray
				TextStyle grayStyle = new TextStyle(cell.getFont(), null, null);
				grayStyle.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
				StyleRange grayRange = new StyleRange(grayStyle);
				grayRange.start = displayName.length();
				grayRange.length = TEST_SCOPE_SUFFIX.length();
				cell.setStyleRanges(new StyleRange[] { grayRange });
			} else {
				cell.setText(displayName);
				cell.setFont(getDefaultFont());
				cell.setStyleRanges(new StyleRange[] {});
			}
			cell.setImage(dependencyNode.getImage());
		} else if (obj instanceof Displayable) {
			Displayable displayable = (Displayable) obj;
			cell.setText(displayable.getDisplayName());
			cell.setFont(getDefaultFont());
			cell.setImage(displayable.getImage());
		}
		super.update(cell);
	}

	private Font getDefaultFont() {
		return Display.getDefault().getSystemFont();
	}

	@Override
	public void dispose() {
		if (grayFont != null && !grayFont.isDisposed()) {
			grayFont.dispose();
		}
		super.dispose();
	}
}
