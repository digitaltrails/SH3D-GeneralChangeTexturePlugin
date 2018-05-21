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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.swing.IconManager;
import com.eteks.sweethome3d.tools.URLContent;

public class TextureSelectionPanel extends JPanel {
	
	public interface TextureSelectionAction {
		public void actionPerformed(final String actionName, final CatalogTexture actionTarget);
	}

	private static final long serialVersionUID = 1L;

	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

	private final JList<CatalogTexture> listView;
	;

	private final JTextField isearchField;

	private final JPopupMenu popupMenu;
	
	private List<CatalogTexture> allData = new ArrayList<CatalogTexture>();
	private List<CatalogTexture> viewableData = allData;
	
	private CatalogTexture popupTarget;
	
	public TextureSelectionPanel(final String title) {

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JLabel titleLable = new JLabel(title);

		listView = new JList<CatalogTexture>();
		listView.setCellRenderer(new CatalogTextureCellRenderer());
		listView.setVisibleRowCount(30);
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setViewableData(viewableData);

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
		
		final JLabel isearchLable = new JLabel(Local.str("TextureSelectionPanel.isearchLabel"));
		isearchField = new JTextField();
		final JPanel isearchPanel =  new JPanel(new BorderLayout());
		isearchPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		isearchPanel.add(isearchLable, BorderLayout.WEST);
		isearchPanel.add(isearchField, BorderLayout.CENTER);
		
		isearchField.addKeyListener(new KeyListener() {
						
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// XXX is this safe beyond UTF-8?
				final String matchStr = isearchField.getText();
				if (matchStr.length() > 0) {
					final List<CatalogTexture> matches = new ArrayList<CatalogTexture>();
					for (CatalogTexture ct: allData) {
						if (ct.getName().toLowerCase().contains(matchStr.toLowerCase())) {
							matches.add(ct);
						}
					}
					setViewableData(matches);
				}
				else {
					setViewableData(allData);
				}
			}	
		});
		
		final JScrollPane pane = new JScrollPane(listView);

		add(titleLable, BorderLayout.PAGE_START);
		add(pane, BorderLayout.CENTER);
		add(isearchPanel, BorderLayout.PAGE_END);
	}

	public void addSelectionListener(ListSelectionListener listener) {
		listView.addListSelectionListener(listener);
	}

	public CatalogTexture getSelectedTexture() {
		final CatalogTexture selected = listView.getSelectedValue();
		return selected;
	}

	public int getFirstVisibleIndex() {
		return listView.getFirstVisibleIndex();
	}

	public int getLastVisibleIndex() {
		return listView.getLastVisibleIndex();
	}

	public void ensureIndexIsVisible(final int index) {
		listView.ensureIndexIsVisible(index);	
	}

	public void setListData(final List<CatalogTexture> dataList) {
		allData = dataList;
		final CatalogTexture oldSelected = listView.getSelectedValue();
		setViewableData(dataList);
		for (CatalogTexture choice: dataList) {
			if (choice == oldSelected) { // Pointers the same
				listView.setSelectedValue(choice, false);
			}
		}		
		isearchField.setText("");
	}
	
	public void setSelected(CatalogTexture selected) {
		if (selected == null) {
			listView.clearSelection();
		}
		else if (viewableData.contains(selected)) {
			listView.setSelectedValue(selected, true);
		}
		else {
			System.err.println("Attempt to select an item not in the visible list of textures.");
		}
	}
	
	private void setViewableData(final List<CatalogTexture> viewableData) {
		this.viewableData = viewableData;
		listView.setListData(viewableData.toArray(new CatalogTexture[viewableData.size()]));
	}
	
	public void ensureIsVisible(final int first, final int last, final String name) {
		final int pos = indexOf(name);
		if (pos < 0) {
			return;
		}
		if (first <= pos && pos <= last) {
			listView.ensureIndexIsVisible(last);
			listView.ensureIndexIsVisible(first);
			return;
		}
		int v = listView.getVisibleRowCount();
		int top = pos - v > 0 ? pos - v : 0;
		int bottom = pos + v < viewableData.size() ? pos + v : viewableData.size() - 1;
		listView.ensureIndexIsVisible(top);
		listView.ensureIndexIsVisible(bottom);
	}
	
	public int indexOf(final String name) {
		int i = 0;
		for (CatalogTexture piece: viewableData) {
			if (piece.getName().equals(name)) {
				return i;
			}
			i++;
		};
		return -1;
	}
	
	public void addPopupAction(final String actionKey, final String label, final TextureSelectionAction action) {
		popupMenu.add(
				new JMenuItem(
						new AbstractAction(label) {
							private static final long serialVersionUID = 1L;
							public void actionPerformed(ActionEvent e) {
								action.actionPerformed(actionKey, popupTarget);			
							}
						}));
	}

	
	private static final class CatalogTextureCellRenderer extends JLabel implements ListCellRenderer<CatalogTexture> {

		private static final long serialVersionUID = 1L;
		
		public CatalogTextureCellRenderer() {
			setOpaque(true);
			setIconTextGap(12);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends CatalogTexture> list, CatalogTexture value,
				int index, boolean isSelected, boolean cellHasFocus) {
			CatalogTexture entry = (CatalogTexture) value;

			final String categoryName = value.getCategory() != null ? value.getCategory().getName() : "Unknown";
			final String prefix = categoryName;

			setText(prefix + " / " + entry.getName());
			
			// Make the root pane redraw when icons finish loading (might be a lot of callbacks?).
			setIcon(new TextureIcon(entry, getRootPane()));
			if (isSelected) {
				setBackground(HIGHLIGHT_COLOR);
				setForeground(Color.white);
			} else {
				setBackground(Color.white);
				setForeground(Color.black);
			}
			setToolTipText(value.getCategory().getName() + "/" + entry.getName());
			// Taking advantage of the fact that the images are in the user's eteks folder
			setToolTipText(createToolTipHtml(value, entry));
			return this;
		}

		private String createToolTipHtml(CatalogTexture value,
				CatalogTexture entry) {
			final String urlStr = ((URLContent)entry.getIcon()).getURL().toString();
			final String nameCat = value.getCategory().getName() + "/" + entry.getName();
			final String byline = entry.getCreator() != null ? Local.str("TextureSelectionPanel.byline", entry.getCreator()) : "";
			return String.format("<html><img src='%s'><p>%s %s</html>", urlStr, nameCat, byline);

		}			  
	}

	
	/**
	 * Icon displaying a texture.  
	 * (copy of private inner class from com.eteks.sweethome3d.swing.TextureChoiceComponent)
	 */
	private static final class TextureIcon implements Icon {
		static final int SIZE = 16;

		private TextureImage texture;
		private JComponent   component;

		public TextureIcon(TextureImage texture,
				JComponent component) {
			this.texture = texture;
			this.component = component;
		}

		public int getIconWidth() {
			return SIZE;
		}

		public int getIconHeight() {
			return SIZE;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Icon icon = IconManager.getInstance().getIcon(
					this.texture.getImage(), getIconHeight(), this.component);
			if (icon.getIconWidth() != icon.getIconHeight()) {
				Graphics2D g2D = (Graphics2D)g;
				AffineTransform previousTransform = g2D.getTransform();
				g2D.translate(x, y);
				g2D.scale((float)icon.getIconHeight() / icon.getIconWidth(), 1);
				icon.paintIcon(c, g2D, 0, 0);
				g2D.setTransform(previousTransform);
			} else {
				icon.paintIcon(c, g, x, y);
			}
		}
	}

}
