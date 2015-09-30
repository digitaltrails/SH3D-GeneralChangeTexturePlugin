/*
 * ChangeTexturePlugin.java 1 sept. 2015
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;


public class ChangeTextureAction extends PluginAction {

	private final ChangeTexturePlugin context;

	private ChangeTextureUI userInterface;

	public ChangeTextureAction(ChangeTexturePlugin context) {
		this.context = context;
		putPropertyValue(Property.NAME, Local.str("ChangeTextureAction.pluginName"));
		putPropertyValue(Property.MENU, "Tools");
		setEnabled(true);
	}

	@Override
	public void execute() {

		List<CatalogTexture> choices = getAllTextures();
		List<HomePieceOfFurniture> furnitureList = context.getHome().getFurniture();
		userInterface = new ChangeTextureUI(choices, furnitureList, new ChangeTextureUI.TextureChangeCallback() {		
			@Override
			public int invoke(final List<HomePieceOfFurniture> list, final CatalogTexture fromTexture, final CatalogTexture toTexture, final Float shininess) {
				if (fromTexture != null && toTexture != null) {
					return changeTexture(list, fromTexture, toTexture, shininess);
				}
				return 0;
			}
		});

		userInterface.pack();
		userInterface.setVisible(true);
	}

	private List<CatalogTexture> getAllTextures() {

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

// TODO Remove
//	private CatalogTexture getCatalogTexture(final String name) {
//
//		final UserPreferences preferences = context.getUserPreferences();
//		for (TexturesCategory category : preferences.getTexturesCatalog().getCategories()) {
//			for (CatalogTexture ct : category.getTextures()) {
//				if (ct.getName().equals(name)) {
//					return ct;
//				}
//			}
//		}	
//		return null;
//	}

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



}
