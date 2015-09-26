/*
 * TextureChangeFrame.java 19 Sept. 2015
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.HomeFramePane;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

public class ChangeTextureUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private final TextureChangePanel textureChangePanel;
	
	public ChangeTextureUI(final List<CatalogTexture> catalogTextureList, final List<HomePieceOfFurniture> furnitureList, final TextureChangeCallback listener) {		 
		textureChangePanel = new TextureChangePanel(catalogTextureList, furnitureList, listener);
		setContentPane(textureChangePanel);
	}
	
	public interface TextureChangeCallback {
		public int invoke(final List<HomePieceOfFurniture> list, final String nameOfFrom, final String nameOfTo,  final Float shininess);
	}
	
	public final class TextureChangePanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		private final TextureSelectionPanel fromSelectionPanel;
		private final TextureSelectionPanel toSelectionPanel;
		private final FurnitureSelectionPanel furnitureSelectionPanel;
		private final ShininessSelectionPanel shininessSelectionPanel;
		private final JButton ok;
		private final JButton close;
		private final JLabel status;
		
		public TextureChangePanel(final List<CatalogTexture> catalogTextureList, final List<HomePieceOfFurniture> furnitureList, final TextureChangeCallback callback) {
			
			setTitle("Change Furniture Textures");
		    setIconImage(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon.png")).getImage());
			
		    setLayout(new BorderLayout());
		    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		    
			fromSelectionPanel = new TextureSelectionPanel(Local.str("TextureChangePanel.fromTexture"), catalogTextureList);
			toSelectionPanel = new TextureSelectionPanel(Local.str("TextureChangePanel.toTextureTo"), catalogTextureList);
			furnitureSelectionPanel = new FurnitureSelectionPanel();
			shininessSelectionPanel = new ShininessSelectionPanel();
			
			final JPanel gridThirdPanel = new JPanel(new BorderLayout());
			gridThirdPanel.add(furnitureSelectionPanel, BorderLayout.CENTER);
			gridThirdPanel.add(shininessSelectionPanel, BorderLayout.PAGE_END);

			final JPanel middlePanel = new JPanel(new GridLayout(1,3));
			middlePanel.add(fromSelectionPanel);
			middlePanel.add(toSelectionPanel);
			middlePanel.add(gridThirdPanel);
			
			add(middlePanel, BorderLayout.CENTER);

			final JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridLayout(1,3,30,0));
			bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			ok = new JButton(Local.str("TextureChangePanel.ok"));
			close = new JButton(Local.str("TextureChangePanel.close"));
			status = new JLabel("");
			bottomPanel.add(ok);
			bottomPanel.add(close);
			bottomPanel.add(status);
			
			add(bottomPanel, BorderLayout.PAGE_END);
			
			fromSelectionPanel.addSelectionListener(new ListSelectionListener() {				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					final int topIndex = fromSelectionPanel.getFirstVisibleIndex();
					final int bottomIndex = fromSelectionPanel.getLastVisibleIndex();
					toSelectionPanel.ensureIndexIsVisible(topIndex);
					toSelectionPanel.ensureIndexIsVisible(bottomIndex);
					final FurnitureMatcher matcher = new FurnitureMatcher(furnitureList);
					final List<HomePieceOfFurniture> references = matcher.findUsing(fromSelectionPanel.getSelectedTextureName());
					final Float shininess = matcher.getShininess();
					furnitureSelectionPanel.update(references);
					shininessSelectionPanel.update(shininess);
					status.setText(Local.str("TextureChangePanel.selected", references.size()));
				}
			});
			
			furnitureSelectionPanel.addSelectionListener(new ListSelectionListener() {				
				@Override
				public void valueChanged(ListSelectionEvent e) {				
					status.setText(Local.str("TextureChangePanel.selected", furnitureSelectionPanel.getSelectedFurniture().size()));
				}
			});
			
			ok.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					final String nameOfFrom = fromSelectionPanel.getSelectedTextureName();
					final String nameOfTo = toSelectionPanel.getSelectedTextureName();
					List<HomePieceOfFurniture> list = furnitureSelectionPanel.getSelectedFurniture();
					final int changedCount = callback.invoke(list, nameOfFrom, nameOfTo, shininessSelectionPanel.getShininess());
					status.setText(Local.str("TextureChangePanel.modified", changedCount));
				}
			});
			
			close.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
	}
}
