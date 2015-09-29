package com.eteks.digitaltrails;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.BranchGroup;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

public final class FurnitureMatcher {

	private Float shininess = null;

	private final List<HomePieceOfFurniture> inputList;
	private final List<HomePieceOfFurniture> resultList;


	public FurnitureMatcher(List<HomePieceOfFurniture> furnitureList) {
		inputList = furnitureList;
		resultList = new ArrayList<HomePieceOfFurniture>();
	}

	public List<HomePieceOfFurniture> findUsing(final String textureName) {
		shininess = null;
		for (HomePieceOfFurniture piece: inputList) {
			traversePieces(piece, textureName);
		}	
		return resultList;
	}

	public List<HomePieceOfFurniture> getResults() {
		return resultList;
	}

	public Float getShininess() {
		return shininess;
	}

	private void traversePieces(final HomePieceOfFurniture piece, final String textureName) {
		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, textureName);
			}
		}
		else {

			if (checkMaterials(piece, textureName)) {
				resultList.add(piece);
			}
		}	
	}

	private HomeMaterial[] loadDefaultMaterials(HomePieceOfFurniture piece) {
		final ArrayList<HomeMaterial[]> defaultMaterialsContainer = new ArrayList<HomeMaterial[]>(1);

		ModelManager.getInstance().loadModel(piece.getModel(), true, new ModelManager.ModelObserver() {
			public void modelUpdated(BranchGroup modelRoot) {
				defaultMaterialsContainer.add(ModelManager.getInstance().getMaterials(modelRoot));
			}

			public void modelError(Exception ex) {
				// Let the list be empty  
			}
		});
		return defaultMaterialsContainer.size() > 0 ? defaultMaterialsContainer.get(0) : null;
	}

	private boolean checkMaterials(final HomePieceOfFurniture piece, final String textureName) {

		final HomeMaterial[] materials = piece.getModelMaterials();
		if (materials != null) {
			for (int i = 0; i < materials.length; i++) {
				// TODO consider URL as the unique ID rather than the name.
				if (materials[i] != null && materials[i].getTexture() != null && materials[i].getTexture().getName().equals(textureName)) {
					if (materials[i].getShininess() != null) {
						shininess = materials[i].getShininess();
					}
					else {
						HomeMaterial[] defaultMaterials = loadDefaultMaterials(piece);
						if (defaultMaterials != null && defaultMaterials.length > i && defaultMaterials[i].getShininess() != null) {
							shininess = defaultMaterials[i].getShininess();
						}
					}
					return true;
				}
			}
		}
		return false;
	}


}
