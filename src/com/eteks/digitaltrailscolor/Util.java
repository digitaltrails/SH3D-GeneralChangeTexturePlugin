package com.eteks.digitaltrailscolor;

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
