/*
 * ChangeColorPlugin 7 Jan. 2016
 *
 * Sweet Home 3D, Copyright (c) 2016 Michael Hamilton / michael at actrix.gen.nz
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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

public class ColorChangePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	
	
	public ColorChangePanel(final List<Room> roomList, Collection<Wall> wallList, final List<HomePieceOfFurniture> furnitureList) {
		final ColorChanger changer = new ColorChanger(roomList, wallList, furnitureList);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JColorChooser fromChooser = new JColorChooser();
		
		final Map<Integer, Set<String>> index = new HashMap<Integer, Set<String>>();
		final List<Integer> inUse = changer.findInUse(index);
		
		final InUseColorChooser fromCustomPanel = new InUseColorChooser(inUse, index);
		fromChooser.addChooserPanel(fromCustomPanel);
		add(addTitle(Local.str("ColorChangePanel.fromChooser.title"), fromChooser), BorderLayout.PAGE_START);
		
		final JColorChooser toChooser = new JColorChooser();
		final InUseColorChooser toCustomPanel = new InUseColorChooser(inUse, index);
		toChooser.addChooserPanel(toCustomPanel);
		add(addTitle(Local.str("ColorChangePanel.toChooser.title"), toChooser), BorderLayout.CENTER);

		JButton commitButton = new JButton(Local.str("ColorChangePanel.changeButton.label"));

		add(commitButton, BorderLayout.PAGE_END);

		commitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final Color fromColor = fromChooser.getSelectionModel().getSelectedColor();

				if (fromColor != null) {

					final Color toColor = toChooser.getSelectionModel().getSelectedColor();

					if (toColor != null) {

						final Integer from = fromColor.getRGB();
						final Integer to = toColor.getRGB();

						// Show all textures used by all furniture in use.

						int count = changer.change(from, to);
						
						index.clear();
						final List<Integer> inUse = changer.findInUse(index);
						toCustomPanel.updateColorsInUse(inUse, index);
						fromCustomPanel.updateColorsInUse(inUse, index);
						
						JOptionPane.showMessageDialog(ColorChangePanel.this, Local.str("ColorChangePanel.modifiedColors.text", count));
					}
				}

			}
		});

	}
	
	private Component addTitle(String string, JColorChooser comp) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(string), BorderLayout.PAGE_START);
		panel.add(comp, BorderLayout.CENTER);
		return panel;
	}

	private static class InUseColorChooser extends AbstractColorChooserPanel {

		private static final long serialVersionUID = 1L;

		private List<Integer> colorsInUse;

		private Map<Integer, Set<String>> itemsUsingColor;

		public InUseColorChooser(List<Integer> inUse, Map<Integer, Set<String>> index) {
			setLayout(new GridLayout(0, 10));
			this.colorsInUse = inUse;
			this.itemsUsingColor = index;
		}

		public void updateColorsInUse(List<Integer> inUse, Map<Integer, Set<String>> index) {
			this.colorsInUse = inUse;
			this.itemsUsingColor = index;
			updateChooser();
			repaint();
		}
		
		public void buildChooser() {
			updateChooser();
		}

		public void updateChooser() {
			removeAll();
			if (colorsInUse == null) {
				return;
			}
			for (Integer colorRgb: colorsInUse) {
				final Color realColor = new Color(colorRgb);
				final String hexColor = String.format("#%02X%02X%02X", realColor.getRed(), realColor.getGreen(), realColor.getBlue());
				final StringBuffer buf = new StringBuffer("<html>" + hexColor);
				for (String name: itemsUsingColor.get(colorRgb)) {
					buf.append("<br>");
					buf.append(name);
				}
				buf.append("</html>");
				addSwatchButton(buf.toString(), realColor);
			}
			validate();
		}

		public String getDisplayName() {
			return Local.str("ColorChangePanel.title");
		}

		public Icon getSmallDisplayIcon() {
			return null;
		}
		public Icon getLargeDisplayIcon() {
			return null;
		}
		
		private void addSwatchButton(String info, Color color) {
			final JButton button = new JButton(info);
			button.setContentAreaFilled(false);
			button.setOpaque(true);
			button.setBackground(color);
			button.setAction(setColorAction);
			button.setText("");

			button.setToolTipText(info);
			add(button);
		}

		Action setColorAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				final JButton button = (JButton) event.getSource();

				getColorSelectionModel().setSelectedColor(button.getBackground());
			}


		};
	}

}