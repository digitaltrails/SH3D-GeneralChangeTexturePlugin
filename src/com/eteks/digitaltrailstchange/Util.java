/*
 * Util.java 21 May 2017
 *
 * Copyright (c) 2015 Michael Hamilton / michael at actrix.gen.nz
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

import javax.media.j3d.BranchGroup;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

public final class Util {

	public static HomeMaterial[] loadDefaultMaterials(HomePieceOfFurniture piece) {
		final ArrayList<HomeMaterial[]> defaultMaterialsContainer = new ArrayList<HomeMaterial[]>(1);

		final ModelManager mm = ModelManager.getInstance();
		mm.loadModel(piece.getModel(), true, new ModelManager.ModelObserver() {
			public void modelUpdated(BranchGroup modelRoot) {
				defaultMaterialsContainer.add(mm.getMaterials(modelRoot));
			}

			public void modelError(Exception ex) {
				// Let the list be empty  
			}
		});
		return defaultMaterialsContainer.size() > 0 ? defaultMaterialsContainer.get(0) : null;
	}

}
