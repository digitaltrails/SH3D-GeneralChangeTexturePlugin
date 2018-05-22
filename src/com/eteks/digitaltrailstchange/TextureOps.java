/*
 * TextureOps.java 21 May 2017
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
package com.eteks.digitaltrailstchange;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eteks.digitaltrailstchange.TextureUse.TextureUsageEnum;
import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

public class TextureOps {
	private final  Map<String, CatalogTexture> catalogTexturesById;
	private final  Map<String, NonCatalogTexture> nonCatalogIndex;
	
	private final  Map<String, List<TextureUse>> usageMap;
	private final  Map<Object, List<CatalogTexture>> reverseMap;
	private final  List<CatalogTexture> catalogTexturesInUse;
	private final  List<CatalogTexture> allTextures;
	private final  Home home;
	
	public TextureOps(final Home home, final UserPreferences preferences) {

		this.home = home;
		allTextures = initAllTextures(preferences);
		catalogTexturesById = new HashMap<String, CatalogTexture>();
		nonCatalogIndex = new HashMap<String, NonCatalogTexture>();
		usageMap = new HashMap<String, List<TextureUse>>();
		reverseMap = new HashMap<Object, List<CatalogTexture>>();
		catalogTexturesInUse = new ArrayList<CatalogTexture>();
		refreshIndexes();
	}

	private void refreshIndexes() {
		usageMap.clear();
		reverseMap.clear();
		catalogTexturesInUse.clear();
		
		for (CatalogTexture texture: allTextures) {
			catalogTexturesById.put(texture.getId(), texture);	
		}
		
		for (Room room: home.getRooms()) {
			if (room.getCeilingTexture() != null) {
				add(new TextureUse(getCatalogTexture(room.getCeilingTexture()), room, TextureUsageEnum.RoomCeiling), usageMap);
			}
			if (room.getFloorTexture() != null) {
				add(new TextureUse(getCatalogTexture(room.getFloorTexture()), room, TextureUsageEnum.RoomFloor), usageMap);
			}
		}

		for (Wall wall: home.getWalls()) {
			if (wall.getLeftSideTexture() != null) {
				add(new TextureUse(getCatalogTexture(wall.getLeftSideTexture()), wall, TextureUsageEnum.WallLeftSide), usageMap);
			}
			if (wall.getRightSideTexture() != null) {
				add(new TextureUse(getCatalogTexture(wall.getRightSideTexture()), wall, TextureUsageEnum.WallRightSide), usageMap);
			}
			if (wall.getLeftSideBaseboard() != null) {
				if (wall.getLeftSideBaseboard().getTexture() != null) {
					add(new TextureUse(getCatalogTexture(wall.getLeftSideBaseboard().getTexture()), wall, TextureUsageEnum.WallLeftSideBaseboard), usageMap);
				}
			}
			if (wall.getRightSideBaseboard() != null) {
				if (wall.getRightSideBaseboard().getTexture() != null) {
					add(new TextureUse(getCatalogTexture(wall.getRightSideBaseboard().getTexture()), wall, TextureUsageEnum.WallRightSideBaseboard), usageMap);
				}
			}
		}

		for (HomePieceOfFurniture piece: home.getFurniture()) {
			traversePieces(piece, usageMap);
		}
		for (List<TextureUse> usages: usageMap.values()) {
			if (usages.size() > 0) {
				catalogTexturesInUse.add(usages.get(0).getCatalogTexture());
			}
		}
	}
	
	private static List<CatalogTexture> initAllTextures(final UserPreferences preferences) {
		final List<CatalogTexture> allCatalogTextures = new ArrayList<CatalogTexture>();
		
		for (TexturesCategory category : preferences.getTexturesCatalog().getCategories()) {
			for (CatalogTexture ct : category.getTextures()) {
				allCatalogTextures.add(ct);
			}
		}	
		return allCatalogTextures;
	}
	
	public List<CatalogTexture> getAllCatalogTextures() {
		refreshIndexes();
		return allTextures;
	}
	
	
	public List<TextureUse> lookupItemsUsing(final CatalogTexture targetTexture) {
		refreshIndexes();
		final List<TextureUse> result = usageMap.get(targetTexture.getId()); 
		return result != null ? result : new ArrayList<TextureUse>(0);
	}
	
	public List<TextureUse> getAllItems() {
		refreshIndexes();
		List<TextureUse> list = new ArrayList<TextureUse>();
		for (List<TextureUse> sublist : usageMap.values()) {
			list.addAll(sublist);
		}
		return list;
	}

	public List<CatalogTexture> getCatalogTexturesInUse() {
		refreshIndexes();
		return catalogTexturesInUse;
	}

	public List<CatalogTexture> findUsedBy(List<TextureUse> uses) {
		refreshIndexes();
		List<CatalogTexture> result = new ArrayList<CatalogTexture>();
		for (TextureUse use: uses) {
			List<CatalogTexture> matches = reverseMap.get(use.getReferer());
			if (matches != null) {
				result.addAll(matches);
			}
		}
		return result;
	}

	public TextureChangeResult change(List<TextureUse> list, CatalogTexture from, CatalogTexture to, Float shininess) {
		int count = 0;
		UndoRedoTextureChange undoableEdit = new UndoRedoTextureChange();
		for (TextureUse target: list) {
			if (target.getRoom() != null) {
				Room room = target.getRoom();
				if (sameTexture(room.getCeilingTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addRoomCeiling(room, newTexture, shininess);
					room.setCeilingTexture(newTexture);
					if (shininess != null) {
						room.setCeilingShininess(shininess);
					}
					count++;
				}
				if (sameTexture(room.getFloorTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addRoomFloor(room, newTexture, shininess);
					room.setFloorTexture(newTexture);
					if (shininess != null) {
						room.setFloorShininess(shininess);
					}
					count++;
				}
			}

			if (target.getWall() != null) {
				Wall wall = target.getWall();
				if (sameTexture(wall.getLeftSideTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addWallLeftSide(wall, newTexture, shininess);
					wall.setLeftSideTexture(newTexture);
					if (shininess != null) {
						wall.setLeftSideShininess(shininess);
					}
					count++;
				}
				if (sameTexture(wall.getRightSideTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addWallRightSide(wall, newTexture, shininess);
					wall.setRightSideTexture(newTexture);
					if (shininess != null) {
						wall.setRightSideShininess(shininess);
					}
					count++;
				}
				
				if (wall.getLeftSideBaseboard() != null && sameTexture(wall.getLeftSideBaseboard().getTexture(), from)) {
					final Baseboard oldBaseboard = wall.getLeftSideBaseboard();
					final HomeTexture newTexture = new HomeTexture(to);
					final Baseboard newBaseboard = Baseboard.getInstance(oldBaseboard.getThickness(), oldBaseboard.getHeight(), oldBaseboard.getColor(), newTexture);
					undoableEdit.addWallLeftSideBaseboard(wall, newTexture);
					wall.setLeftSideBaseboard(newBaseboard);
					count++;
				}
				if (wall.getRightSideBaseboard() != null && sameTexture(wall.getRightSideBaseboard().getTexture(), from)) {
					final Baseboard oldBaseboard = wall.getRightSideBaseboard();
					final HomeTexture newTexture = new HomeTexture(to);
					final Baseboard newBaseboard = Baseboard.getInstance(oldBaseboard.getThickness(), oldBaseboard.getHeight(), oldBaseboard.getColor(), newTexture);
					undoableEdit.addWallRightSideBaseboard(wall, newTexture);
					wall.setRightSideBaseboard(newBaseboard);
					count++;
				}
				
			}
			if (target.getPieceOfFuniture() != null) {
				HomePieceOfFurniture piece = target.getPieceOfFuniture();
				count += traverseAndChangePieces(piece, from, to, shininess, undoableEdit);
			}
		}
		refreshIndexes();
		return new TextureChangeResult(undoableEdit, count);
	}

	private boolean sameTexture(HomeTexture homeTexture, CatalogTexture catalogTexture) {
		if (homeTexture == null && catalogTexture == null) {
			return true;
		}
		else if (homeTexture == null  || catalogTexture == null) {
			return false;
		}
		// compare ignoring alpha 
		final String homeTextureId = homeTexture.getCatalogId() != null ? homeTexture.getCatalogId() : homeTexture.getName();
		System.out.println("HT homeTexture id=" + homeTextureId + "<=> catalogTexture id=" + catalogTexture.getId());
		if (homeTextureId == null || catalogTexture.getId() == null) {
			return false;
		}
		return homeTextureId.equals(catalogTexture.getId());
	}

	private boolean sameTexture(HomeMaterial homeMaterial, CatalogTexture catalogTexture) {
		if (homeMaterial.getTexture() == null && catalogTexture == null) {
			return true;
		}
		else if (homeMaterial.getTexture() == null  || catalogTexture == null) {
			return false;
		}
		// compare ignoring alpha
		final String catalogId = homeMaterial.getTexture().getCatalogId() != null ? homeMaterial.getTexture().getCatalogId() : homeMaterial.getName();
		System.out.println("MT homeMaterial id=" + catalogId + "<=> catalogTexture id=" + catalogTexture.getId());
		if (catalogId == null || catalogTexture.getId() == null) {
			return false;
		}
		return catalogId.equals(catalogTexture.getId());
	}
	
	private void add(TextureUse textureUse, Map<String, List<TextureUse>> usageMap) {

		final CatalogTexture catalogTexture = textureUse.getCatalogTexture();

		
		// Index catalogTexture -> model item name -> count of items with that item name
		List<TextureUse> currentReferences = usageMap.get(catalogTexture.getId());

		if (currentReferences == null) {
			currentReferences = new ArrayList<TextureUse>();
			usageMap.put(catalogTexture.getId(), currentReferences);
		}
		currentReferences.add(textureUse);
		
		List<CatalogTexture> reverseRefs = reverseMap.get(textureUse.getReferer());
		if (reverseRefs == null) {
			reverseRefs = new ArrayList<CatalogTexture>();
			reverseMap.put(textureUse.getReferer(), reverseRefs);
		}
		reverseRefs.add(catalogTexture);
	}
	

	private CatalogTexture getCatalogTexture(HomeTexture homeTexture) {
		final String id = homeTexture.getCatalogId();
		final CatalogTexture catalogTexture;
		if (id != null && catalogTexturesById.containsKey(id)) {
			catalogTexture = catalogTexturesById.get(id);
		}
		else {
			// Orphaned texture (no longer in catalog - probably a deleted user texture. 
			final String nonCatalogId = id != null ? id : homeTexture.getName();
			catalogTexture = findNonCatalogTexture(nonCatalogId, homeTexture);
		}
		return catalogTexture;
	}
	

	private CatalogTexture getCatalogTexture(HomeMaterial homeMaterial) {
		
		final HomeTexture homeTexture = homeMaterial.getTexture();
		final String textureId = homeTexture.getCatalogId();
		final CatalogTexture catalogTexture;
		if (textureId != null && catalogTexturesById.containsKey(textureId)) {
			catalogTexture = catalogTexturesById.get(textureId);
		}
		else {
			// Orphaned texture (no longer in catalog - probably a deleted user texture. 
			final String nonCatalogId = homeMaterial.getName();
			catalogTexture = findNonCatalogTexture(nonCatalogId, homeTexture);
		}
		return catalogTexture;
	}

	private NonCatalogTexture findNonCatalogTexture(final String id, final HomeTexture homeTexture) {
		//final String key = homeTexture.getName() != null ? homeTexture.getName() : homeTexture.;
		final NonCatalogTexture texture;
		if (!nonCatalogIndex.containsKey(id)) {
			texture = new NonCatalogTexture(id, homeTexture);
			nonCatalogIndex.put(id, texture);
		}
		else {
			texture = nonCatalogIndex.get(id);
		}
		return texture;
	}

	private void traversePieces(final HomePieceOfFurniture piece, Map<String, List<TextureUse>> usageMap) {

		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, usageMap);
			}
		}
		else {
			findMaterialTextures(piece, usageMap);
		}
		if (piece.getTexture() != null) {
			add(new TextureUse(getCatalogTexture(piece.getTexture()), piece), usageMap);
		}

	}


	private void findMaterialTextures(final HomePieceOfFurniture piece, Map<String, List<TextureUse>> usageMap) {

		final HomeMaterial[] materials = piece.getModelMaterials();
		final HomeMaterial[] defaultMaterials = Util.loadDefaultMaterials(piece);

		if (materials != null) {
			for (int i = 0; i < materials.length; i++) {
				if (materials[i] != null) {
					if (materials[i].getTexture() != null) {
						add(new TextureUse(getCatalogTexture(materials[i]), piece, materials[i]), usageMap);
					}
				}
			}
		}
		if (defaultMaterials != null) {
			for (int i = 0; i < defaultMaterials.length; i++) {
				if (materials == null || i >= materials.length || materials[i] == null || (materials[i].getTexture() == null && materials[i].getColor() == null)) {
					if (defaultMaterials[i] != null && defaultMaterials[i].getTexture() != null) {
						add(new TextureUse(getCatalogTexture(defaultMaterials[i]), piece, defaultMaterials[i]), usageMap);
					}
				}
			}
		}
	}	


	private int traverseAndChangePieces(HomePieceOfFurniture piece, CatalogTexture from, CatalogTexture to, Float shinyness, UndoRedoTextureChange undoableEdit) {

		int count = 0;

		if (piece != null) {
			if (piece instanceof HomeFurnitureGroup) {
				// Recurse into the group
				HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
				for (HomePieceOfFurniture member : group.getFurniture() ) {
					count += traverseAndChangePieces(member, from, to, shinyness, undoableEdit);
				}
			}
			else { 
				count += changeMaterialTexture(piece, from, to,shinyness, undoableEdit);
			}
			if (piece.getTexture() != null) {
				if (sameTexture(piece.getTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addFurniture(piece, newTexture, shinyness);
					piece.setTexture(newTexture);
					count++;
				}
			}
		}
		return count;
	}


	private int changeMaterialTexture(final HomePieceOfFurniture piece, final CatalogTexture from, final CatalogTexture to, final Float shininess, UndoRedoTextureChange undoableEdit) {

		int changedCount = 0;

		// Make a new materials list copying the old one and replacing matching elements as we go
		final HomeMaterial[] oldMaterials = piece.getModelMaterials();
		final HomeMaterial[] defaultMaterials = Util.loadDefaultMaterials(piece);
		final HomeMaterial[] newMaterials = new HomeMaterial[oldMaterials != null ? oldMaterials.length : defaultMaterials.length];
		if (oldMaterials != null) {
			for (int i = 0; i < newMaterials.length; i++) {
				if (oldMaterials[i] != null) {
					if (oldMaterials[i].getTexture() != null && sameTexture(oldMaterials[i], from)) {
						final HomeMaterial oldMaterial = oldMaterials[i];;	
						final Float newShininess = shininess  != null ? shininess : oldMaterial.getShininess();
						newMaterials[i] = new HomeMaterial(oldMaterial.getName(), oldMaterial.getColor(), new HomeTexture(to), newShininess);
						changedCount++;
					}
					else if (i < oldMaterials.length) {
						newMaterials[i] = oldMaterials[i];
					}
				}
			}
		}
		if (defaultMaterials != null) {
			for (int i = 0; i < newMaterials.length; i++) {
				if ((newMaterials[i] == null || newMaterials[i].getTexture() == null) && defaultMaterials[i] != null && sameTexture(defaultMaterials[i], from)) {
					final HomeMaterial defaultMaterial = defaultMaterials[i];
					newMaterials[i] = new HomeMaterial(defaultMaterial.getName(), null, new HomeTexture(to), shininess != null ? shininess : defaultMaterial.getShininess());
					changedCount++;
				}
			}
		}
		if (changedCount > 0) {
			piece.setColor(null);
			piece.setTexture(null);
			piece.setModelMaterials(newMaterials);
			undoableEdit.addMaterials(piece, oldMaterials, newMaterials);
			piece.setVisible(piece.isVisible());
		}
		return changedCount;
	}

}

