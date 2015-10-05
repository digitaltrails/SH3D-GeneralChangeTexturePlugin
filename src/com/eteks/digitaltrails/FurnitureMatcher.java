package com.eteks.digitaltrails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.media.j3d.BranchGroup;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.CatalogTexture;
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

	public List<HomePieceOfFurniture> findUsing(final CatalogTexture catalogTexture) {
		shininess = null;
		for (HomePieceOfFurniture piece: inputList) {
			traversePieces(piece, catalogTexture);
		}	
		Collections.sort(resultList, new Comparator<HomePieceOfFurniture>() {

			@Override
			public int compare(HomePieceOfFurniture o1, HomePieceOfFurniture o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return resultList;
	}

	public Float getShininess() {
		return shininess;
	}

	private void traversePieces(final HomePieceOfFurniture piece, final CatalogTexture catalogTexture) {
		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, catalogTexture);
			}
		}
		else {

			if (catalogTexture == null || checkMaterials(piece, catalogTexture)) {
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

	private boolean checkMaterials(final HomePieceOfFurniture piece, final CatalogTexture catalogTexture) {

		final HomeMaterial[] materials = piece.getModelMaterials();
		if (materials != null) {
			for (int i = 0; i < materials.length; i++) {
				// TODO consider URL as the unique ID rather than the name.
				if (materials[i] != null && materials[i].getTexture() != null && materials[i].getTexture().getName().equals(catalogTexture.getName())) {
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
