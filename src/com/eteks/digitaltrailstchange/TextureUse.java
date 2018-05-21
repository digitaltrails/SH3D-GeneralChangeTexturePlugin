/*
 * TextureUse.java UndoRedoTextureChange.java 21 May 2017
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

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

public class TextureUse {
		
	public enum TextureUsageEnum {
		WallLeftSide,
		WallRightSide,
		WallLeftSideBaseboard,
		WallRightSideBaseboard,
		RoomCeiling,
		RoomFloor,
		PieceOfFurnature,
		MaterialOfFurnature
	}
	
	private final CatalogTexture catalogTexture;
	private final TextureUsageEnum usageType;
	private final Wall wall;
	private final Room room;
	private final HomePieceOfFurniture pieceOfFuniture;
	private final HomeMaterial material;
	private final String description;
	
	public TextureUse(final CatalogTexture texture, final Wall wallItem, final TextureUsageEnum side) {
		catalogTexture = texture;
		usageType = side;			
		wall = wallItem;
		room = null;
		pieceOfFuniture = null;
		material = null;
		switch (side) {
		case WallLeftSide:
			description = Local.str("TextureUse.wallLeftSide");
			break;
		case WallRightSide:
			description = Local.str("TextureUse.wallRightSide");
			break;
		case WallLeftSideBaseboard:
			description = Local.str("TextureUse.wallLeftSideBaseboard");
			break;
		case WallRightSideBaseboard:
			description = Local.str("TextureUse.wallRightSideBaseboard");
			break;
		default:
			description = Local.str("TextureUse.wall");
		}
	}
	
	public TextureUse(final CatalogTexture texture, final Room roomItem, final TextureUsageEnum part) {
		catalogTexture = texture;
		usageType = part;			
		wall = null;
		room = roomItem;
		pieceOfFuniture = null;
		material = null;
		final String roomName = room.getName() != null ? room.getName() : Local.str("TextureUse.roomWithNoName"); 
		switch (part) {
		case RoomCeiling: 
			description = "Room " + roomName + " ceiling ";
			break;
		case RoomFloor: 
			description = "Room " + roomName + " floor ";
			break;

		default:
			description = "Room " + roomName + part;
			break;
		}
	}
	
	public TextureUse(final CatalogTexture texture, final HomePieceOfFurniture funitureItem) {
		catalogTexture = texture;
		usageType = TextureUsageEnum.PieceOfFurnature;			
		wall = null;
		room = null;
		pieceOfFuniture = funitureItem;
		material = null;
		description = funitureItem.getName();
		funitureItem.getShininess();
	}
	
	public TextureUse(final CatalogTexture texture, final HomePieceOfFurniture funitureItem, HomeMaterial funitureMaterial) {
		catalogTexture = texture;
		usageType = TextureUsageEnum.PieceOfFurnature;			
		wall = null;
		room = null;
		pieceOfFuniture = funitureItem;
		material = funitureMaterial;
		description = funitureItem.getName() + ":" + material.getName();
	}

	public HomeMaterial getMaterial() {
		return material;
	}

	public CatalogTexture getCatalogTexture() {
		return catalogTexture;
	}

	public TextureUsageEnum getUsageType() {
		return usageType;
	}

	public Wall getWall() {
		return wall;
	}

	public Room getRoom() {
		return room;
	}

	public HomePieceOfFurniture getPieceOfFuniture() {
		return pieceOfFuniture;
	}

	public String getDescription() {
		return description;
	}
	
	public Object getReferer() {
		if (wall != null) {
			return wall;
		}
		if (room != null) {
			return room;
		}
		if (pieceOfFuniture != null) {
			return pieceOfFuniture;
		}
		// Not supposed to reach here.
		return null;
	}
		
}
