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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

public class ChangeTextureUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public ChangeTextureUI(ChangeTexturePlugin context) {
		final List<CatalogTexture> catalogTextureList = getAllTextures(context);
		final List<HomePieceOfFurniture> furnitureList = context.getHome().getFurniture();
		final TextureChangePanel textureChangePanel = new TextureChangePanel(catalogTextureList, furnitureList);
		setContentPane(textureChangePanel);
	}

	private int changeTexture(final List<HomePieceOfFurniture> list, final CatalogTexture fromTexture, final CatalogTexture toTexture, final Float shininess) {
		int changedCount = 0;

		for (HomePieceOfFurniture piece: list) {
			// Make a new materials list copying the old one and replacing matching elements as we go
			final HomeMaterial[] oldMaterials = piece.getModelMaterials();
			final HomeMaterial[] materials = new HomeMaterial[oldMaterials.length];
			for (int i = 0; i < materials.length; i++) {
				if (oldMaterials[i] != null && oldMaterials[i].getTexture() != null && oldMaterials[i].getTexture().getName().equals(fromTexture.getName())) {
					final HomeMaterial old = oldMaterials[i];
					final HomeTexture newTexture = new HomeTexture(toTexture);	
					final Float newShininess = shininess != null ? shininess : old.getShininess();
					// TODO check old shininess really does get the old value
					materials[i] = new HomeMaterial(old.getName(), old.getColor(), newTexture, newShininess);
					changedCount++;
				}
				else {
					materials[i] = piece.getModelMaterials()[i];
				}
			}
			piece.setModelMaterials(materials);
		}
		return changedCount;
	}
	
	private static List<CatalogTexture> getAllTextures(ChangeTexturePlugin context) {
		final List<CatalogTexture> choices = new ArrayList<CatalogTexture>();
		final Map<String, CatalogTexture> catalogIds = new HashMap<String, CatalogTexture>();
		final UserPreferences preferences = context.getUserPreferences();
		for (TexturesCategory category : preferences.getTexturesCatalog().getCategories()) {
			for (CatalogTexture ct : category.getTextures()) {
				choices.add(ct);
				catalogIds.put(ct.getId(), ct);
			}
		}	
		return choices;
	}
	
	private final class TextureChangePanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		private final TextureSelectionPanel fromSelectionPanel;
		private final TextureSelectionPanel toSelectionPanel;
		private final FurnitureSelectionPanel furnitureSelectionPanel;
		private final ShininessSelectionPanel shininessSelectionPanel;
		private final JButton ok;
		private final JButton close;
		private final JLabel status;
		
		public TextureChangePanel(final List<CatalogTexture> catalogTextureList, final List<HomePieceOfFurniture> furnitureList) {
			
			setTitle("Change Furniture Textures");
		    setIconImage(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon.png")).getImage());
			
		    setLayout(new BorderLayout());
		    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		    
			fromSelectionPanel = new TextureSelectionPanel(Local.str("TextureChangePanel.fromTexture"), catalogTextureList);
			toSelectionPanel = new TextureSelectionPanel(Local.str("TextureChangePanel.toTextureTo"), catalogTextureList);
			furnitureSelectionPanel = new FurnitureSelectionPanel(furnitureList);
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
					final List<HomePieceOfFurniture> references = matcher.findUsing(fromSelectionPanel.getSelectedTexture());
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
					final CatalogTexture fromTexture = fromSelectionPanel.getSelectedTexture();
					final CatalogTexture toTexture = toSelectionPanel.getSelectedTexture();
					List<HomePieceOfFurniture> list = furnitureSelectionPanel.getSelectedFurniture();
					final int changedCount = changeTexture(list, fromTexture, toTexture, shininessSelectionPanel.getShininess());
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
