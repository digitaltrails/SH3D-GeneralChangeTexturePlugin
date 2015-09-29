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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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

	private static final long serialVersionUID = 1L;

	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

	private final JList<CatalogTexture> choiceList;
	private final CatalogTexture[] catalogTextures;

	private final JTextField isearchField;

	public TextureSelectionPanel(final String title, final List<CatalogTexture> catalogTextureList) {

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JLabel titleLable = new JLabel(title);

		catalogTextures = catalogTextureList.toArray(new CatalogTexture[catalogTextureList.size()]);
		choiceList = new JList<CatalogTexture>(catalogTextures);
		choiceList.setCellRenderer(new CatalogTextureCellRenderer());
		choiceList.setVisibleRowCount(30);
		choiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
					for (CatalogTexture ct: catalogTextures) {
						if (ct.getName().toLowerCase().contains(matchStr.toLowerCase())) {
							matches.add(ct);
						}
					}
					choiceList.setListData(matches.toArray(new CatalogTexture[matches.size()]));
				}
				else {
					choiceList.setListData(catalogTextures);
				}
			}	
		});
		
		final JScrollPane pane = new JScrollPane(choiceList);

		add(titleLable, BorderLayout.PAGE_START);
		add(pane, BorderLayout.CENTER);
		add(isearchPanel, BorderLayout.PAGE_END);
	}

	public void addSelectionListener(ListSelectionListener listener) {
		choiceList.addListSelectionListener(listener);
	}

	public String getSelectedTextureName() {
		final CatalogTexture selected = choiceList.getSelectedValue();
		return (selected == null) ? null : selected.getName();
	}

	public int getFirstVisibleIndex() {
		return choiceList.getFirstVisibleIndex();
	}

	public int getLastVisibleIndex() {
		return choiceList.getLastVisibleIndex();
	}

	public void ensureIndexIsVisible(final int index) {
		choiceList.ensureIndexIsVisible(index);	
	}

	private static final class CatalogTextureCellRenderer extends JLabel implements ListCellRenderer<CatalogTexture> {

		private static final long serialVersionUID = 1L;
		private static final String INDENT = "     ..";

		public CatalogTextureCellRenderer() {
			setOpaque(true);
			setIconTextGap(12);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends CatalogTexture> list, CatalogTexture value,
				int index, boolean isSelected, boolean cellHasFocus) {
			CatalogTexture entry = (CatalogTexture) value;

			final String categoryName = value.getCategory() != null ? value.getCategory().getName() : INDENT;
			final String prefix =  (index == 0 || !list.getModel().getElementAt(index - 1).getCategory().getName().equals(value.getCategory().getName())) ? categoryName : INDENT;

			setText(prefix + " / " + entry.getName());
			//setIcon(IconManager.getInstance().getIcon(entry.getIcon(), DEFAULT_ICON_HEIGHT, this));
			setIcon(new TextureIcon(entry, this));
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
