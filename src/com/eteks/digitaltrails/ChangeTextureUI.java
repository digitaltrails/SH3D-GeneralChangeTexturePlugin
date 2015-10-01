/*
 * ChangeTextureUI.java 19 Sept. 2015
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

import javax.swing.ImageIcon;
import javax.swing.JFrame;


import com.eteks.sweethome3d.HomeFramePane;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

public class ChangeTextureUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public ChangeTextureUI(ChangeTexturePlugin context) {
		setTitle("Change Furniture Textures");
	    setIconImage(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon.png")).getImage());
		final List<CatalogTexture> catalogTextureList = getAllTextures(context);
		final List<HomePieceOfFurniture> furnitureList = context.getHome().getFurniture();
		final TextureChangePanel textureChangePanel = new TextureChangePanel(catalogTextureList, furnitureList);
		setContentPane(textureChangePanel);
	}
	
	private List<CatalogTexture> getAllTextures(ChangeTexturePlugin context) {
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
}
