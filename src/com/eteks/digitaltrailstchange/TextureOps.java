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
import java.util.Collections;
import java.util.Comparator;
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
	private static final boolean PRIORITIZE_NAME_AS_IDENTIFIER = true;
	private final  Map<String, CatalogTexture> catalogTexturesById;
	private final  Map<String, NonCatalogTexture> nonCatalogIndex;
	
	private final  Map<String, List<TextureUse>> usageMap;
	private final  Map<Object, List<CatalogTexture>> reverseMap;
	private final  List<CatalogTexture> texturesInUse;
	private final  List<CatalogTexture> allTextures;
	private final  Home home;
	private final  UserPreferences userPreferences;
	private final  UndoRedoCallback undoRedoCallback;
	
	public TextureOps(final Home home, final UserPreferences preferences, final UndoRedoCallback callback) {
		this.home = home;
		userPreferences = preferences;
		allTextures = new ArrayList<CatalogTexture>();
		catalogTexturesById = new HashMap<String, CatalogTexture>();
		nonCatalogIndex = new HashMap<String, NonCatalogTexture>();
		usageMap = new HashMap<String, List<TextureUse>>();
		reverseMap = new HashMap<Object, List<CatalogTexture>>();
		texturesInUse = new ArrayList<CatalogTexture>();
		undoRedoCallback = callback;
		refreshIndexes();
	}
	
	public UndoRedoCallback getUndoRedoCallback() {
		return undoRedoCallback;
	}

	/**
	 * Finds only cataloged textures (some items have their on non catalog default textures).
	 * @return list of catalog textures
	 */
	public List<CatalogTexture> findAllCatalogTextures() {
		return allTextures;
	}
	
	/**
	 * Find items using this texture.
	 * @param targetTexture
	 * @return
	 */
	public List<TextureUse> findItemsUsingTexture(final CatalogTexture targetTexture) {
		final String identifier = getIndentifier(targetTexture);
		final List<TextureUse> result = usageMap.get(identifier); 
		return result != null ? result : new ArrayList<TextureUse>(0);
	}
	
	/**
	 * Find all items using any textures, both catalog and non-catalog (default textures).
	 * @return
	 */
	public List<TextureUse> findAllReferencesToTextures() {
		List<TextureUse> list = new ArrayList<TextureUse>();
		for (List<TextureUse> sublist : usageMap.values()) {
			list.addAll(sublist);
		}
		return list;
	}

	/**
	 * Find all catalog and non catalog textures in use in this home.
	 * @return list of textures being used in this home.
	 */
	public List<CatalogTexture> findTexturesBeingUsed() {
		Collections.sort(texturesInUse, new Comparator<CatalogTexture>() {
			@Override
			public int compare(CatalogTexture o1, CatalogTexture o2) {
				if (o1 != null && o2 != null) {
					int result = (o1.getCategory() != null && o2.getCategory() != null) ? o1.getCategory().getName().compareToIgnoreCase(o2.getCategory().getName()) : 0;
					if (result != 0) {
						return result;
					}
					return getIndentifier(o1).compareToIgnoreCase(getIndentifier(o2));
				}
				return 0;
			} 
		});	
		return texturesInUse;
	}

	/**
	 * For the texture references, find what other textures the items are also using.
	 * @param uses
	 * @return
	 */
	public List<CatalogTexture> findUsedBy(List<TextureUse> uses) {
		List<CatalogTexture> result = new ArrayList<CatalogTexture>();
		for (TextureUse use: uses) {
			List<CatalogTexture> matches = reverseMap.get(use.getReferer());
			if (matches != null) {
				result.addAll(matches);
			}
		}
		return result;
	}

	public void refresh() {
		refreshIndexes();
	}
	
	public TextureChangeResult change(List<TextureUse> list, CatalogTexture from, CatalogTexture to, Float shininess) {
		int count = 0;
		UndoRedoTextureChange undoableEdit = new UndoRedoTextureChange(this);
		for (TextureUse target: list) {
			if (target.getRoom() != null) {
				Room room = target.getRoom();
				if (isMatchForTexture(room.getCeilingTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addRoomCeiling(room, newTexture, shininess);
					room.setCeilingTexture(newTexture);
					if (shininess != null) {
						room.setCeilingShininess(shininess);
					}
					count++;
				}
				if (isMatchForTexture(room.getFloorTexture(), from)) {
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
				if (isMatchForTexture(wall.getLeftSideTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addWallLeftSide(wall, newTexture, shininess);
					wall.setLeftSideTexture(newTexture);
					if (shininess != null) {
						wall.setLeftSideShininess(shininess);
					}
					count++;
				}
				if (isMatchForTexture(wall.getRightSideTexture(), from)) {
					final HomeTexture newTexture = new HomeTexture(to);
					undoableEdit.addWallRightSide(wall, newTexture, shininess);
					wall.setRightSideTexture(newTexture);
					if (shininess != null) {
						wall.setRightSideShininess(shininess);
					}
					count++;
				}
				
				if (wall.getLeftSideBaseboard() != null && isMatchForTexture(wall.getLeftSideBaseboard().getTexture(), from)) {
					final Baseboard oldBaseboard = wall.getLeftSideBaseboard();
					final HomeTexture newTexture = new HomeTexture(to);
					final Baseboard newBaseboard = Baseboard.getInstance(oldBaseboard.getThickness(), oldBaseboard.getHeight(), oldBaseboard.getColor(), newTexture);
					undoableEdit.addWallLeftSideBaseboard(wall, newTexture);
					wall.setLeftSideBaseboard(newBaseboard);
					count++;
				}
				if (wall.getRightSideBaseboard() != null && isMatchForTexture(wall.getRightSideBaseboard().getTexture(), from)) {
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
				count += changeFurnitureTexture(piece, from, to, shininess, undoableEdit);
			}
		}
		refreshIndexes();
		return new TextureChangeResult(undoableEdit, count);
	}
	
	private void refreshIndexes() {
		allTextures.clear();
		usageMap.clear();
		reverseMap.clear();
		texturesInUse.clear();
		
		for (TexturesCategory category : userPreferences.getTexturesCatalog().getCategories()) {
			for (CatalogTexture ct : category.getTextures()) {
				allTextures.add(ct);
			}
		}	
		
		for (CatalogTexture texture: allTextures) {
			catalogTexturesById.put(getIndentifier(texture), texture);	
		}
		
		for (Room room: home.getRooms()) {
			if (room.getCeilingTexture() != null) {
				addToMappings(new TextureUse(getCatalogTexture(room.getCeilingTexture()), room, TextureUsageEnum.RoomCeiling), usageMap);
			}
			if (room.getFloorTexture() != null) {
				addToMappings(new TextureUse(getCatalogTexture(room.getFloorTexture()), room, TextureUsageEnum.RoomFloor), usageMap);
			}
		}

		for (Wall wall: home.getWalls()) {
			if (wall.getLeftSideTexture() != null) {
				addToMappings(new TextureUse(getCatalogTexture(wall.getLeftSideTexture()), wall, TextureUsageEnum.WallLeftSide), usageMap);
			}
			if (wall.getRightSideTexture() != null) {
				addToMappings(new TextureUse(getCatalogTexture(wall.getRightSideTexture()), wall, TextureUsageEnum.WallRightSide), usageMap);
			}
			if (wall.getLeftSideBaseboard() != null) {
				if (wall.getLeftSideBaseboard().getTexture() != null) {
					addToMappings(new TextureUse(getCatalogTexture(wall.getLeftSideBaseboard().getTexture()), wall, TextureUsageEnum.WallLeftSideBaseboard), usageMap);
				}
			}
			if (wall.getRightSideBaseboard() != null) {
				if (wall.getRightSideBaseboard().getTexture() != null) {
					addToMappings(new TextureUse(getCatalogTexture(wall.getRightSideBaseboard().getTexture()), wall, TextureUsageEnum.WallRightSideBaseboard), usageMap);
				}
			}
		}

		for (HomePieceOfFurniture piece: home.getFurniture()) {
			findUsedTextures(piece, usageMap);
		}
		for (List<TextureUse> usages: usageMap.values()) {
			if (usages.size() > 0) {
				texturesInUse.add(usages.get(0).getCatalogTexture());
			}
		}
	}
	
	private void addToMappings(TextureUse textureUse, Map<String, List<TextureUse>> usageMap) {

		final CatalogTexture catalogTexture = textureUse.getCatalogTexture();

		
		// Index catalogTexture -> model item name -> count of items with that item name
		final String identifier = getIndentifier(catalogTexture);
		List<TextureUse> currentReferences = usageMap.get(identifier);

		if (currentReferences == null) {
			currentReferences = new ArrayList<TextureUse>();
			usageMap.put(identifier, currentReferences);
		}
		currentReferences.add(textureUse);
		
		List<CatalogTexture> reverseRefs = reverseMap.get(textureUse.getReferer());
		if (reverseRefs == null) {
			reverseRefs = new ArrayList<CatalogTexture>();
			reverseMap.put(textureUse.getReferer(), reverseRefs);
		}
		reverseRefs.add(catalogTexture);
	}
	
	private void findUsedTextures(final HomePieceOfFurniture piece, final Map<String, List<TextureUse>> usageMap) {
		FunatureInspector funitureInspector = new FunatureInspector() {
			@Override
			public void process(HomePieceOfFurniture piece, HomeMaterial material, boolean isDefaultMaterial) {
				if (material != null) {
					if (material.getTexture() != null) {
						addToMappings(new TextureUse(getCatalogTexture(material), piece, material), usageMap);
					}
				}
				else {
					if (piece.getTexture() != null) {
						addToMappings(new TextureUse(getCatalogTexture(piece.getTexture()), piece), usageMap);
					}
				}
			}
		};
		funitureInspector.inspect(piece);
	}

	private CatalogTexture getCatalogTexture(HomeTexture homeTexture) {
		final String identifier = getIndentifier(homeTexture);
		final CatalogTexture catalogTexture;
		if (identifier != null && catalogTexturesById.containsKey(identifier)) {
			catalogTexture = catalogTexturesById.get(identifier);
		}
		else {
			// Orphaned texture (no longer in catalog - probably a deleted user texture. 
			catalogTexture = findNonCatalogTexture(identifier, homeTexture);
		}
		return catalogTexture;
	}
	
	private CatalogTexture getCatalogTexture(HomeMaterial homeMaterial) {
		
		final HomeTexture homeTexture = homeMaterial.getTexture();
		final String textureIdentifier = getIndentifier(homeTexture);
		final CatalogTexture catalogTexture;
		if (textureIdentifier != null && catalogTexturesById.containsKey(textureIdentifier)) {
			catalogTexture = catalogTexturesById.get(textureIdentifier);
		}
		else {
			// Orphaned texture (no longer in catalog - probably a deleted user texture. 
			final String nonCatalogIdentifier = getIndentifier(homeMaterial);
			catalogTexture = findNonCatalogTexture(nonCatalogIdentifier, homeTexture);
		}
		return catalogTexture;
	}

	private NonCatalogTexture findNonCatalogTexture(final String identifier, final HomeTexture homeTexture) {
		//final String key = homeTexture.getName() != null ? homeTexture.getName() : homeTexture.;
		final NonCatalogTexture texture;
		if (!nonCatalogIndex.containsKey(identifier)) {
			texture = new NonCatalogTexture(identifier, homeTexture);
			nonCatalogIndex.put(identifier, texture);
		}
		else {
			texture = nonCatalogIndex.get(identifier);
		}
		return texture;
	}

	private int changeFurnitureTexture(final HomePieceOfFurniture piece, final CatalogTexture from, final CatalogTexture to, final Float shininess, final UndoRedoTextureChange undoableEdit) {
		final int changedCount[] = {0} ;
		final List<HomeMaterial> newMaterialsList =  new ArrayList<HomeMaterial>();//new HomeMaterial[oldMaterials != null ? oldMaterials.length : defaultMaterials.length];
		
		final FunatureInspector furnatureInspector = new FunatureInspector() {
			@Override
			public void process(HomePieceOfFurniture piece, HomeMaterial material, boolean isDefaultMaterial) {
				if (material != null) {
					if (material.getTexture() != null && isMatchForTexture(material, from)) {
						final HomeMaterial oldMaterial = material;	
						final Float newShininess = shininess  != null ? shininess : oldMaterial.getShininess();
						newMaterialsList.add(new HomeMaterial(oldMaterial.getName(), null, new HomeTexture(to), newShininess));
						changedCount[0]++;
					}
					else if (!isDefaultMaterial) {
						newMaterialsList.add(material);
					}
				}
				else {
					if (piece.getTexture() != null) {
						if (isMatchForTexture(piece.getTexture(), from)) {
							final HomeTexture newTexture = new HomeTexture(to);
							undoableEdit.addFurniture(piece, newTexture, shininess);
							piece.setTexture(newTexture);
							changedCount[0]++;
						}
					}
				}
			}
		};
		
		furnatureInspector.inspect(piece);
		if (changedCount[0] > 0) {
			final HomeMaterial[] oldMaterials = piece.getModelMaterials();
			piece.setColor(null);
			piece.setTexture(null);
			final HomeMaterial[] newMaterials = newMaterialsList.toArray(new HomeMaterial[newMaterialsList.size()]);
			piece.setModelMaterials(newMaterials);
			undoableEdit.addMaterials(piece, oldMaterials, newMaterials);
			piece.setVisible(piece.isVisible());
		}
		return changedCount[0];
	}
	
	private boolean isMatchForTexture(HomeTexture homeTexture, CatalogTexture catalogTexture) {
		if (homeTexture == null && catalogTexture == null) {
			return true;
		}
		else if (homeTexture == null  || catalogTexture == null) {
			return false;
		}
		// compare ignoring alpha 
		final String leftIdentifier = getIndentifier(homeTexture);
		final String rightIdentifier = getIndentifier(catalogTexture);
		System.out.println("HT homeTexture idf=" + leftIdentifier + "<=> catalogTexture idf=" + rightIdentifier);
		if (leftIdentifier == null || rightIdentifier == null) {
			return false;
		}
		return leftIdentifier.equals(rightIdentifier);
	}

	public String getIndentifier(HomeTexture homeTexture) {
		if (PRIORITIZE_NAME_AS_IDENTIFIER) {
			return homeTexture.getName() != null ? homeTexture.getName() : homeTexture.getCatalogId();
		}
		return homeTexture.getCatalogId() != null ? homeTexture.getCatalogId() : homeTexture.getName();
	}
	
	public String getIndentifier(CatalogTexture homeTexture) {
		if (PRIORITIZE_NAME_AS_IDENTIFIER) {
			return homeTexture.getName() != null ? homeTexture.getName() : homeTexture.getId();
		}
		return homeTexture.getId() != null ? homeTexture.getId() : homeTexture.getName();
	}

	public String getIndentifier(HomeMaterial homeMaterial) {
		if (homeMaterial.getTexture() == null) {
			return null;
		}
		if (PRIORITIZE_NAME_AS_IDENTIFIER) {
			if (homeMaterial.getTexture().getName() != null) {
				return homeMaterial.getTexture().getName();
			}
			if (homeMaterial.getTexture().getCatalogId() != null) {
				return homeMaterial.getTexture().getCatalogId();
			}
			return homeMaterial.getName();
		}
		return homeMaterial.getTexture().getCatalogId() != null ? homeMaterial.getTexture().getCatalogId() : homeMaterial.getName();
	}

	private boolean isMatchForTexture(HomeMaterial homeMaterial, CatalogTexture catalogTexture) {
		if (homeMaterial.getTexture() == null && catalogTexture == null) {
			return false;
		}
		else if (homeMaterial.getTexture() == null  || catalogTexture == null) {
			return false;
		}
		// compare ignoring alpha
		final String leftIdentifier = getIndentifier(homeMaterial);
		final String rightIdentifier = getIndentifier(catalogTexture);
		System.out.println("MT homeMaterial idf=" + leftIdentifier + "<=> catalogTexture idf=" + rightIdentifier);
		if (leftIdentifier == null || rightIdentifier == null) {
			return false;
		}
		return leftIdentifier.equals(rightIdentifier);
	}
	
	
	
	public static abstract class FunatureInspector {
		
		public abstract void process(HomePieceOfFurniture piece, HomeMaterial material, boolean isDefaultMaterial);
		
		public void inspect(HomePieceOfFurniture piece) {
			if (piece != null) {
				if (piece instanceof HomeFurnitureGroup) {
					// Recurse into the group
					HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
					for (HomePieceOfFurniture member : group.getFurniture() ) {
						inspect(member);
					}
				}
				else { 
					inspectMaterials(piece);
				}
				process(piece, null, false);
			}
		}

		private void inspectMaterials(final HomePieceOfFurniture piece) {


			// Make a new materials list copying the old one and replacing matching elements as we go
			final HomeMaterial[] customMaterials = piece.getModelMaterials();
			final HomeMaterial[] defaultMaterials = Util.loadDefaultMaterials(piece);
			
			if (customMaterials != null) {
				for (int i = 0; i < customMaterials.length; i++) {
					if (customMaterials[i] != null) {
						process(piece, customMaterials[i], false);
					}
				}
			}
			if (defaultMaterials != null) {
				for (int i = 0; i < defaultMaterials.length; i++) {
					if (defaultMaterials[i] != null) {
						final HomeMaterial defaultMaterial = defaultMaterials[i];
						boolean overridden = false;
						if (customMaterials != null ) {
							for (HomeMaterial customMaterial: customMaterials) {
								if (customMaterial != null && customMaterial.getName() != null && customMaterial.getName().equals(defaultMaterial.getName())) {
									overridden = true;
									break;
								}		
							}
						}
						if (!overridden) {
							process(piece, defaultMaterial, true);
						}
					}
				}
			}
		}
	}


	
	
}

