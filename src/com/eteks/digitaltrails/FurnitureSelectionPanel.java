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
package com.eteks.digitaltrails;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.model.HomePieceOfFurniture;

public class FurnitureSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
	
	private final JList<HomePieceOfFurniture> pieceJList;

	public FurnitureSelectionPanel() {

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JLabel titleLable = new JLabel(Local.str("FurnitureSelectionPanel.title"));

		pieceJList = new JList<HomePieceOfFurniture>();
		pieceJList.setVisibleRowCount(30);
		pieceJList.setCellRenderer(new HomePieceCellRenderer());
		pieceJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane pane = new JScrollPane(pieceJList);
		
		add(titleLable, BorderLayout.PAGE_START);
		add(pane, BorderLayout.CENTER);
	}

	public void update(final List<HomePieceOfFurniture> list) {
		pieceJList.setListData(list.toArray(new HomePieceOfFurniture[list.size()]));
		final int[] indices = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			indices[i] = i;
		}
		pieceJList.setSelectedIndices(indices);
	}

	public void addSelectionListener(final ListSelectionListener listener) {
		pieceJList.addListSelectionListener(listener);
	}
	
	public List<HomePieceOfFurniture> getSelectedFurniture() {
		
		// JDK 1.7
		//final List<HomePieceOfFurniture> selected = pieceList.getSelectedValuesList();
		//return (selected == null) ? new ArrayList<HomePieceOfFurniture>() : selected;
		@SuppressWarnings("deprecation")
		final Object[] objSelectedList = pieceJList.getSelectedValues();
		final List<HomePieceOfFurniture> selected = new ArrayList<HomePieceOfFurniture>();
		for (Object obj: objSelectedList) {
			selected.add((HomePieceOfFurniture) obj);
		}
		return selected;	
	}

	public final class HomePieceCellRenderer extends JLabel implements ListCellRenderer<HomePieceOfFurniture> {

		private static final long serialVersionUID = 1L;

		public HomePieceCellRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends HomePieceOfFurniture> list,
				HomePieceOfFurniture value, int index, boolean isSelected,
				boolean cellHasFocus) {
			setText(value.getName());

			if (isSelected) {
				setBackground(HIGHLIGHT_COLOR);
				setForeground(Color.white);
			} else {
				setBackground(Color.white);
				setForeground(Color.black);
			}

			return this;
		}
	}
}
