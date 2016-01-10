/*
 * TextureChoicePanel.java 19 Sept. 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Michael Hamilton / michael at actrix.gen.nz
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.digitaltrailscolor;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class ShininessSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JSlider shininessSlider;
	private final JCheckBox enableSliderCheckBox;

	public ShininessSelectionPanel() {

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel titleLable = new JLabel(Local.str("ShininessSelectionPanel.title"));
		
		enableSliderCheckBox = new JCheckBox(Local.str("ShininessSelectionPanel.enabled"), true);

		shininessSlider = new JSlider(JSlider.HORIZONTAL, 0, 128, 100);
		shininessSlider.setToolTipText(Local.str("ShininessSelectionPanel.tip"));

		final Hashtable<Integer, JComponent> shininessLabels = new Hashtable<Integer,JComponent>();
		shininessLabels.put(0, new JLabel(Local.str("ShininessSelectionPanel.mattLabel")));
		shininessLabels.put(128, new JLabel(Local.str("ShininessSelectionPanel.glossLabel")));
		shininessSlider.setLabelTable(shininessLabels);
		shininessSlider.setPaintLabels(true);
		shininessSlider.setPaintTicks(true);
		shininessSlider.setMajorTickSpacing(16);
				
		add(titleLable, BorderLayout.PAGE_START);
		add(enableSliderCheckBox, BorderLayout.PAGE_END);
		add(shininessSlider,  BorderLayout.CENTER);
		
		enableSliderCheckBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				shininessSlider.setEnabled(enableSliderCheckBox.isSelected());
				if (enableSliderCheckBox.isSelected()) {
					
				}
				
			}
		});
	}

	public void update(Float newShininess) {
		if (newShininess != null) {
			shininessSlider.setValue((int) (newShininess * 128));
		}
	}
	
	public Float getShininess() {
		if (!enableSliderCheckBox.isSelected()) {
			return null;
		}
		return shininessSlider.getValue() / 128.0f;
	}
}
