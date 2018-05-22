package com.eteks.digitaltrailstchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * UndoRedoTextureChange.java 21 May 2017
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
import java.util.Map.Entry;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;


import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

public final class UndoRedoTextureChange extends AbstractUndoableEdit {
	private static final long serialVersionUID = 1L;

	private static abstract class SimpleChange {
		private final HomeTexture oldTexture;
		private final Float oldShinyness;
		private final HomeTexture newTexture;
		private final Float newShinyness;

		public SimpleChange(final HomeTexture oldTexture, final Float oldShinyness, final HomeTexture newTexture, final	Float newShinyness) {
			super();
			this.oldTexture = oldTexture;
			this.oldShinyness = oldShinyness;
			this.newTexture = newTexture;
			this.newShinyness = newShinyness;
		}

		public HomeTexture getOldTexture() {
			return oldTexture;
		}

		public Float getOldShinyness() {
			return oldShinyness;
		}

		public HomeTexture getNewTexture() {
			return newTexture;
		}

		public Float getNewShinyness() {
			return newShinyness;
		}

		public abstract void undo();
		public abstract void redo();
	}

	private final List<UndoRedoTextureChange.SimpleChange> simpleChanges = new ArrayList<UndoRedoTextureChange.SimpleChange>();

	private final Map<HomePieceOfFurniture, HomeMaterial[][]> materials = new HashMap<HomePieceOfFurniture, HomeMaterial[][]>();

	public void addRoomFloor(final Room room, final HomeTexture newTexture, final Float newShinyness) {
		simpleChanges.add(new SimpleChange(room.getFloorTexture(), room.getFloorShininess(), newTexture, newShinyness) {
			@Override
			public void undo() {
				room.setFloorTexture(getOldTexture());
				room.setFloorShininess(getOldShinyness());
			}
			@Override
			public void redo() {
				room.setFloorTexture(getNewTexture());
				room.setFloorShininess(getNewShinyness());
			}} );
	}

	public void addRoomCeiling(final Room room, final HomeTexture newTexture, final Float newShinyness) {
		simpleChanges.add(new SimpleChange(room.getCeilingTexture(), room.getCeilingShininess(), newTexture, newShinyness) {
			@Override
			public void undo() {
				room.setCeilingTexture(getOldTexture());
				room.setCeilingShininess(getOldShinyness());
			}
			@Override
			public void redo() {
				room.setCeilingTexture(getNewTexture());
				room.setCeilingShininess(getNewShinyness());
			}} );
	}

	public void addWallLeftSide(final Wall wall, final HomeTexture newTexture, final Float newShinyness) {
		simpleChanges.add(new SimpleChange(wall.getLeftSideTexture(), wall.getLeftSideShininess(), newTexture, newShinyness) {
			@Override
			public void undo() {
				wall.setLeftSideTexture(getOldTexture());
				wall.setLeftSideShininess(getOldShinyness());
			}
			@Override
			public void redo() {
				wall.setLeftSideTexture(getNewTexture());
				wall.setLeftSideShininess(getNewShinyness());
			}} );
	}

	public void addWallRightSide(final Wall wall, final HomeTexture newTexture, final Float newShinyness) {
		simpleChanges.add(new SimpleChange(wall.getRightSideTexture(), wall.getRightSideShininess(), newTexture, newShinyness) {
			@Override
			public void undo() {
				wall.setRightSideTexture(getOldTexture());
				wall.setRightSideShininess(getOldShinyness());
			}
			@Override
			public void redo() {
				wall.setRightSideTexture(getNewTexture());
				wall.setRightSideShininess(getNewShinyness());
			}} );
	}

	public void addWallLeftSideBaseboard(final Wall wall, final HomeTexture newTexture) { 
		simpleChanges.add(new SimpleChange(wall.getLeftSideBaseboard().getTexture(), null, newTexture, null) {
			@Override
			public void undo() {
				final Baseboard existing = wall.getLeftSideBaseboard();
				final Baseboard newBaseboard = Baseboard.getInstance(existing.getThickness(), existing.getHeight(), existing.getColor(), getOldTexture());
				wall.setLeftSideBaseboard(newBaseboard);
			}
			@Override
			public void redo() {
				final Baseboard existing = wall.getLeftSideBaseboard();
				final Baseboard newBaseboard = Baseboard.getInstance(existing.getThickness(), existing.getHeight(), existing.getColor(), getNewTexture());
				wall.setLeftSideBaseboard(newBaseboard);
			}} );
	}

	public void addWallRightSideBaseboard(final Wall wall, final HomeTexture newTexture) {
		simpleChanges.add(new SimpleChange(wall.getRightSideBaseboard().getTexture(), null, newTexture, null) {
			@Override
			public void undo() {
				final Baseboard existing = wall.getRightSideBaseboard();
				final Baseboard newBaseboard = Baseboard.getInstance(existing.getThickness(), existing.getHeight(), existing.getColor(), getOldTexture());
				wall.setRightSideBaseboard(newBaseboard);
			}
			@Override
			public void redo() {
				final Baseboard existing = wall.getRightSideBaseboard();
				final Baseboard newBaseboard = Baseboard.getInstance(existing.getThickness(), existing.getHeight(), existing.getColor(), getNewTexture());
				wall.setRightSideBaseboard(newBaseboard);
			}} );
	}


	public void addFurniture(final HomePieceOfFurniture piece, final HomeTexture newTexture, final Float newShinyness) {
		simpleChanges.add(new SimpleChange(piece.getTexture(), piece.getShininess(), newTexture, newShinyness) {
			@Override
			public void undo() {
				piece.setTexture(getOldTexture());
				piece.setShininess(getOldShinyness());
			}
			@Override
			public void redo() {
				piece.setTexture(getNewTexture());
				piece.setShininess(getNewShinyness());
			}} );
	}

	public void addMaterials(HomePieceOfFurniture piece, HomeMaterial[] oldMaterials, HomeMaterial newMaterials[]) {
		materials.put(piece, new HomeMaterial[][] { oldMaterials, newMaterials });
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (UndoRedoTextureChange.SimpleChange simpleChange: simpleChanges) {
			simpleChange.undo();
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
		for (UndoRedoTextureChange.SimpleChange simpleChange: simpleChanges) {
			simpleChange.redo();
		}
		for (Entry<HomePieceOfFurniture, HomeMaterial[][]> entry: materials.entrySet()) {
			final HomePieceOfFurniture piece = entry.getKey();
			piece.setModelMaterials(entry.getValue()[1]);
			piece.setVisible(piece.isVisible());
		}
	}

	@Override
	public String getPresentationName() {
		return Local.str("TextureOps.undoPresentationName");
	}
}