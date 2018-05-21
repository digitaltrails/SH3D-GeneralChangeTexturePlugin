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
package com.eteks.digitaltrailstchange;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class ModelItemSelectionPanel extends JPanel {

	public interface ModelItemSelectionAction {
		public void actionPerformed(final String actionName, final TextureUse popupTarget);
	}

	private static final long serialVersionUID = 1L;

	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
	
	private final JList<TextureUse> listView;

	private final JPopupMenu popupMenu;
	
	private TextureUse popupTarget = null;
	
	private List<TextureUse> listData;

	public ModelItemSelectionPanel() {
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JLabel titleLable = new JLabel(Local.str("ModelItemSelectionPanel.title"));

		listView = new JList<TextureUse>();
		listView.setVisibleRowCount(30);
		listView.setCellRenderer(new HomePieceCellRenderer());
		listView.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		popupMenu = new JPopupMenu() {
			private static final long serialVersionUID = 1L;
			@Override
			public void setVisible(boolean aFlag) {
				// Remember where the mouse was when the menu was popped up.
				final Point point = listView.getMousePosition();
				if (point != null) {
					final int popupIndex = listView.locationToIndex(point);
					if (listView.getModel() != null && popupIndex >= 0 && popupIndex < listView.getModel().getSize()) {
						popupTarget = listView.getModel().getElementAt(popupIndex);
					}
				}
				super.setVisible(aFlag);
			}
		};
		
		listView.setComponentPopupMenu(popupMenu);
		
		JScrollPane pane = new JScrollPane(listView);
		
		add(titleLable, BorderLayout.PAGE_START);
		add(pane, BorderLayout.CENTER);
	}

	public void addPopupAction(final String actionKey, final String label, final ModelItemSelectionAction action) {
		popupMenu.add(
				new JMenuItem(
						new AbstractAction(label) {
							private static final long serialVersionUID = 1L;
							public void actionPerformed(ActionEvent e) {
								action.actionPerformed(actionKey, popupTarget);			
							}
						}));
	}

	public List<TextureUse> getListData() {
		return listData;
	}
	
	public void setListData(final List<TextureUse> list) {
		listData = list;
		listView.setListData(list.toArray(new TextureUse[list.size()]));
	}
	
	public void selectAll() {
		listView.setSelectionInterval(0, listView.getModel().getSize() - 1);
	}

	public int indexOf(final String name) {
		int i = 0;
		for (TextureUse piece: listData) {
			if (piece.getDescription().equals(name)) {
				return i;
			}
			i++;
		};
		return -1;
	}

	public void addSelectionListener(final ListSelectionListener listener) {
		listView.addListSelectionListener(listener);
	}
	
	public List<TextureUse> getSelectedItems() {

		if (listView.isSelectionEmpty()) {
			return new ArrayList<TextureUse>();
		}
		// JDK 1.7
		//final List<HomePieceOfFurniture> selected = pieceList.getSelectedValuesList();
		//return (selected == null) ? new ArrayList<HomePieceOfFurniture>() : selected;
		@SuppressWarnings("deprecation")
		final Object[] objSelectedList = listView.getSelectedValues();
		final List<TextureUse> selected = new ArrayList<TextureUse>();
		for (Object obj: objSelectedList) {
			selected.add((TextureUse) obj);
		}
		return selected;	
	}

	private final static class HomePieceCellRenderer extends JLabel implements ListCellRenderer<TextureUse> {

		private static final long serialVersionUID = 1L;

		public HomePieceCellRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends TextureUse> list,
				TextureUse value, int index, boolean isSelected,
				boolean cellHasFocus) {
			setText(value.getDescription());

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
	
//	private static boolean isRightClick(MouseEvent e) {
//	    return (e.getButton()==MouseEvent.BUTTON3 ||
//	            (System.getProperty("os.name").contains("Mac OS X") &&
//	                    (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 &&
//	                    (e.getModifiers() & InputEvent.CTRL_MASK) != 0));
//	}
}
