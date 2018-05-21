/*
 * ChangeColorPlugin 7 Jan. 2016
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
package com.eteks.digitaltrailscolor;



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

public class ColorChanger {
	final List<Room> roomList;
	final Collection<Wall> wallList;
	final List<HomePieceOfFurniture> furnatureList;


	public ColorChanger(List<Room> roomList, Collection<Wall> wallList, List<HomePieceOfFurniture> furnatureList) {
		this.roomList = roomList;
		this.wallList = wallList;
		this.furnatureList = furnatureList;
	}

	public List<Integer> findInUse(Map<Integer, Map<String,Integer>> index) {
		Set<Integer> result = new TreeSet<Integer>();
		for (Room room: roomList) {
			addIfNotNull(room.getName() + ".ceiling" , room.getCeilingColor(), result, index);
			addIfNotNull(room.getName() + ".floor", room.getFloorColor(), result, index);
		}

		for (Wall wall: wallList) {
			addIfNotNull("wall", wall.getLeftSideColor(), result, index);
			addIfNotNull("wall", wall.getRightSideColor(), result, index);
		}

		for (HomePieceOfFurniture piece: furnatureList) {
			traversePieces(piece, result, index);
		}
		return new ArrayList<Integer>(result);
	}

	public int change(Integer from, Integer to, UndoRedoColorChange undoableEdit) {
		int count = 0;
		for (Room room: roomList) {
			if (sameColor(room.getCeilingColor(), from)) {
				room.setCeilingColor(to);
				undoableEdit.addRoomCeiling(room);
				count++;
			}
			if (sameColor(room.getFloorColor(), from)) {
				room.setFloorColor(to);
				undoableEdit.addRoomFloor(room);
				count++;
			}
		}

		for (Wall wall: wallList) {
			if (sameColor(wall.getLeftSideColor(), from)) {
				wall.setLeftSideColor(to);
				undoableEdit.addWallLeftSide(wall);
				count++;
			}
			if (sameColor(wall.getRightSideColor(), from)) {
				wall.setRightSideColor(to);
				undoableEdit.addWallRightSide(wall);
				count++;
			}
		}

		for (HomePieceOfFurniture piece: furnatureList) {
			count += traverseAndChangePieces(piece, from, to, undoableEdit);
		}		

		return count;
	}

	private boolean sameColor(Integer left, Integer right) {
		if (left == null && right == null) {
			return true;
		}
		else if (left == null  || right == null) {
			return false;
		}
		// compare ignoring alpha 
		return colorNoAlpha(left) == colorNoAlpha(right);
		//return left.equals(right); 
	}

	private void addIfNotNull(String reference, Integer color, Set<Integer> result, Map<Integer, Map<String, Integer>> index) {
		if (color != null) {
			result.add(colorNoAlpha(color));
			if (index != null) {
				Map<String, Integer> currentReferences = index.get(colorNoAlpha(color));
				if (reference != null) {
					if (currentReferences == null) {
						currentReferences = new HashMap<String,Integer>();
					}
					currentReferences.put(reference, (currentReferences.containsKey(reference) ? currentReferences.get(reference) : 0) + 1);
					index.put(colorNoAlpha(color), currentReferences);
				}
			}
		}
	}

	private void traversePieces(final HomePieceOfFurniture piece, Set<Integer> result, Map<Integer, Map<String, Integer>> index) {

		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, result, index);
			}
		}
		else {
			findMaterialColors(piece, result, index);
		}
		addIfNotNull(piece.getName(), piece.getColor(), result, index);

	}


	private void findMaterialColors(final HomePieceOfFurniture piece, Set<Integer> result, Map<Integer, Map<String, Integer>> index) {

		final HomeMaterial[] materials = piece.getModelMaterials();
		final HomeMaterial[] defaultMaterials = Util.loadDefaultMaterials(piece);

		if (materials != null) {
			for (int i = 0; i < materials.length; i++) {
				if (materials[i] != null) {
					if (materials[i].getColor() != null) {
						addIfNotNull(piece.getName() + "." + materials[i].getName()  ,materials[i].getColor(), result, index);
					}
				}
			}
		}
		if (defaultMaterials != null) {
			for (int i = 0; i < defaultMaterials.length; i++) {
				if (materials == null || materials[i] == null || (materials[i].getTexture() == null && materials[i].getColor() == null)) {
					if (defaultMaterials[i] != null && defaultMaterials[i].getColor() != null) {
						addIfNotNull(piece.getName() + "." + defaultMaterials[i].getName(), defaultMaterials[i].getColor(), result, index);
					}
				}
			}
		}
	}	


	private int traverseAndChangePieces(HomePieceOfFurniture piece, Integer from, Integer to, UndoRedoColorChange undoableEdit) {

		int count = 0;

		if (piece != null) {
			if (piece instanceof HomeFurnitureGroup) {
				// Recurse into the group
				HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
				for (HomePieceOfFurniture member : group.getFurniture() ) {
					count += traverseAndChangePieces(member, from, to, undoableEdit);
				}
			}
			else { 
				count += changeMaterialColors(piece, from, to, undoableEdit);
			}

			if (sameColor(piece.getColor(), from)) {
				piece.setColor(copyAlpha(piece.getColor(), to));
				undoableEdit.addFurniture(piece);
				count++;
			}

		}
		return count;
	}


	private int changeMaterialColors(final HomePieceOfFurniture piece, final Integer from, final Integer to, UndoRedoColorChange undoableEdit) {

		int changedCount = 0;
		Float shininess = null;

		// Make a new materials list copying the old one and replacing matching elements as we go
		final HomeMaterial[] oldMaterials = piece.getModelMaterials();
		final HomeMaterial[] defaultMaterials = Util.loadDefaultMaterials(piece);
		final HomeMaterial[] newMaterials = new HomeMaterial[oldMaterials != null ? oldMaterials.length : defaultMaterials.length];

		for (int i = 0; i < newMaterials.length; i++) {

			if (oldMaterials != null && oldMaterials[i] != null) {
				if (sameColor(oldMaterials[i].getColor(), from)) {
					final HomeMaterial oldMaterial = oldMaterials[i];;	
					final Float newShininess = shininess  != null ? shininess : oldMaterial.getShininess();
					newMaterials[i] = new HomeMaterial(oldMaterial.getName(), copyAlpha(oldMaterial.getColor(), to), oldMaterial.getTexture(), newShininess);
					changedCount++;
				}
				else {
					newMaterials[i] = oldMaterials[i];
				}
			}
			else if (defaultMaterials != null && defaultMaterials[i] != null && sameColor(defaultMaterials[i].getColor(), from)) {
				if (oldMaterials == null || oldMaterials[i] == null || (oldMaterials[i].getTexture() == null && oldMaterials[i].getColor() == null)) {
					HomeMaterial defaultMaterial = defaultMaterials[i];
					newMaterials[i] = new HomeMaterial(defaultMaterial.getName(), copyAlpha(defaultMaterial.getColor(), to), defaultMaterial.getTexture(), defaultMaterial.getShininess());
					changedCount++;
				}
			}

		}
		if (changedCount > 0) {
			piece.setModelMaterials(newMaterials);
			undoableEdit.addMaterials(piece, oldMaterials, newMaterials);
			Util.loadDefaultMaterials(piece);
			piece.setVisible(piece.isVisible());
		}

		return changedCount;
	}

	public final static int colorNoAlpha(int color) {
		return color & 0x00FFFFFF;
	}

	private int copyAlpha(final int from, final int to) {
		// Alpha's don't seem to contribute anything?
		return to;
		//		if ((to & 0xFF000000) != 0xFF000000) {
		//			return to;
		//		}
		//		return (from & 0xFF000000) | (to & 0x00FFFFFF);
	}
	
	public final static class UndoRedoColorChange extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1L;
		private final List<Room> floors = new ArrayList<Room>();
		private final List<Room> ceilings = new ArrayList<Room>();
		private final List<Wall> leftWalls = new ArrayList<Wall>();
		private final List<Wall> rightWalls = new ArrayList<Wall>();
		private final List<HomePieceOfFurniture> furnitureColors = new ArrayList<HomePieceOfFurniture>();
		private final Map<HomePieceOfFurniture, HomeMaterial[][]> materials = new HashMap<HomePieceOfFurniture, HomeMaterial[][]>();
		private final int from;
		private final int to;

		public UndoRedoColorChange(final int from, final int to) {
			this.from = from;
			this.to = to;
		}
		
		public void addRoomFloor(Room room) {
			floors.add(room);
		}
		
		public void addRoomCeiling(Room room) {
			ceilings.add(room);
		}
		
		public void addWallLeftSide(Wall wall) {
			leftWalls.add(wall);
		}
		
		public void addWallRightSide(Wall wall) {
			rightWalls.add(wall);
		}
		
		public void addFurniture(HomePieceOfFurniture piece) {
			furnitureColors.add(piece);
		}
		
		public void addMaterials(HomePieceOfFurniture piece, HomeMaterial[] oldMaterials, HomeMaterial newMaterials[]) {
			materials.put(piece, new HomeMaterial[][] { oldMaterials, newMaterials });
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			for (Room room: floors) {
				room.setFloorColor(from);
			}
			for (Room room: ceilings) {
				room.setCeilingColor(from);
			}
			for (Wall wall: leftWalls) {
				wall.setLeftSideColor(from);
			}
			for (Wall wall: rightWalls) {
				wall.setRightSideColor(from);
			}
			for (HomePieceOfFurniture piece: furnitureColors) {
				piece.setColor(from);
			}
			for (Entry<HomePieceOfFurniture, HomeMaterial[][]> entry: materials.entrySet()) {
				final HomePieceOfFurniture piece = entry.getKey();
				piece.setModelMaterials(entry.getValue()[0]);
				piece.setVisible(piece.isVisible());
			}
		}
		
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			for (Room room: floors) {
				room.setFloorColor(to);
			}
			for (Room room: ceilings) {
				room.setCeilingColor(to);
			}
			for (Wall wall: leftWalls) {
				wall.setLeftSideColor(to);
			}
			for (Wall wall: rightWalls) {
				wall.setRightSideColor(to);
			}
			for (HomePieceOfFurniture piece: furnitureColors) {
				piece.setColor(to);
			}
			for (Entry<HomePieceOfFurniture, HomeMaterial[][]> entry: materials.entrySet()) {
				final HomePieceOfFurniture piece = entry.getKey();
				piece.setModelMaterials(entry.getValue()[1]);
				piece.setVisible(piece.isVisible());
			}
		}
		
		@Override
		public String getPresentationName() {
			return Local.str("ColorChanger.undoPresentationName");
		}
		
   		
	}
}

