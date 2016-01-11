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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

public class ColorChangePanel extends JPanel {

	private static final long serialVersionUID = 1L;



	public ColorChangePanel(final List<Room> roomList, Collection<Wall> wallList, final List<HomePieceOfFurniture> furnitureList) {
		final ColorChanger changer = new ColorChanger(roomList, wallList, furnitureList);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Panel containing the two choosers (from and to)
		final JPanel choosersPanel = new JPanel(new BorderLayout());
		//choosersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final Map<Integer, Map<String, Integer>> index = new HashMap<Integer, Map<String, Integer>>();
		final List<Integer> inUse = changer.findInUse(index);

		final InUseColorChooser fromCustomPanel = new InUseColorChooser(inUse, index);
		final JColorChooser fromChooser = createChooser(fromCustomPanel, inUse, index);
		choosersPanel.add(addTitle(Local.str("ColorChangePanel.fromChooser.title"), addMatchListPanel(fromChooser, index)), BorderLayout.PAGE_START);

		final InUseColorChooser toCustomPanel = new InUseColorChooser(inUse, index);
		final JColorChooser toChooser = createChooser(toCustomPanel, inUse, index);	
		choosersPanel.add(addTitle(Local.str("ColorChangePanel.toChooser.title"), addMatchListPanel(toChooser, index)), BorderLayout.PAGE_END);

		add(choosersPanel, BorderLayout.LINE_START);

		JButton commitButton = new JButton(Local.str("ColorChangePanel.changeButton.label"));

		add(commitButton, BorderLayout.PAGE_END);

		commitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final Color fromColor = fromChooser.getSelectionModel().getSelectedColor();

				if (fromColor != null) {

					final Color toColor = toChooser.getSelectionModel().getSelectedColor();

					if (toColor != null) {

						final Integer from = ColorChanger.colorNoAlpha(fromColor.getRGB());
						final Integer to = ColorChanger.colorNoAlpha(toColor.getRGB());

						// Show all textures used by all furniture in use.

						int count = changer.change(from, to);

						index.clear();
						final List<Integer> inUse = changer.findInUse(index);
						toCustomPanel.updateColorsInUse(inUse, index);
						fromCustomPanel.updateColorsInUse(inUse, index);
						fromChooser.setColor(toColor);
						JOptionPane.showMessageDialog(ColorChangePanel.this, Local.str("ColorChangePanel.modifiedColors.text", count));
					}
				}

			}
		});

	}

	private JColorChooser createChooser(final InUseColorChooser customPanel, final List<Integer> inUse, final Map<Integer, Map<String, Integer>> index) {
		final JColorChooser chooser = new JColorChooser();
		chooser.addChooserPanel(customPanel);
		chooser.setColor(new Color(0xDEADBEEF)); // Defaults to white - which we don't want.
		return chooser;
	}

	private JPanel addMatchListPanel(final JColorChooser chooser,	final Map<Integer, Map<String, Integer>> index) {
		final JPanel matchListPanel = new JPanel(new BorderLayout());
		final JLabel label = new JLabel(Local.str("ColorChangePanel.fromChooser.matchesForNothing"));
		matchListPanel.add(label, BorderLayout.PAGE_START);
		final DefaultListModel<String> itemsUsingColorModel = new DefaultListModel<String>();
		itemsUsingColorModel.addElement(Local.str("ColorChangePanel.fromChooser.nothingSelected"));
		final JList<String> view = new JList<String>(itemsUsingColorModel);
		final JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.setMinimumSize(new Dimension(600,-1));
		// Disable selection in the list in case its misleading
		view.setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void setSelectionInterval(int index0, int index1) {
				super.setSelectionInterval(-1, -1);
			}
		});
		chooser.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				itemsUsingColorModel.removeAllElements();
				final Color fromColor = chooser.getColor();

				if (fromColor != null) {
					final int colorNoAlpha = ColorChanger.colorNoAlpha(fromColor.getRGB());
					label.setText(Local.str("ColorChangePanel.fromChooser.matchesFor", colorNoAlpha));
					final Map<String, Integer> uses = index.get(colorNoAlpha);
					if (uses != null) {
						itemsUsingColorModel.removeAllElements();
						for (Entry<String, Integer> use: uses.entrySet()) {
							itemsUsingColorModel.addElement(use.getValue() + " x " + use.getKey());
						}
						return;
					}
				}
				itemsUsingColorModel.addElement(Local.str("ColorChangePanel.fromChooser.nothingSelected"));
			}
		});


		matchListPanel.add(scrollPane, BorderLayout.CENTER);

		final JPanel containerPanel = new JPanel(new BorderLayout());

		containerPanel.add(chooser, BorderLayout.LINE_START);
		containerPanel.add(matchListPanel, BorderLayout.CENTER);

		return containerPanel;
	}

	private Component addTitle(String string, JPanel comp) {
		final JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createBevelBorder(BevelBorder.RAISED)));
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(string), BorderLayout.PAGE_START);
		panel.add(comp, BorderLayout.CENTER);
		return panel;
	}

	private static class InUseColorChooser extends AbstractColorChooserPanel {

		private static final long serialVersionUID = 1L;

		private List<Integer> colorsInUse;

		private Map<Integer, Map<String, Integer>> itemsUsingColor;

		public InUseColorChooser(List<Integer> inUse, Map<Integer, Map<String, Integer>> index) {
			setLayout(new GridLayout(0, 10));
			this.colorsInUse = inUse;
			this.itemsUsingColor = index;
		}

		public void updateColorsInUse(List<Integer> inUse, Map<Integer, Map<String, Integer>> index) {
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
				final Map<String, Integer> usesOfColor = itemsUsingColor.get(colorRgb);
				for (Entry<String, Integer> ref: usesOfColor.entrySet()) {
					buf.append("<br>");
					buf.append(ref.getValue() + " x ");
					buf.append(ref.getKey());
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