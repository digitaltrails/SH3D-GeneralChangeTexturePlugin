package com.eteks.digitaltrails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.eteks.digitaltrails.TextureMatcher.UncatalogedTexture;
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

	public List<HomePieceOfFurniture> findUsing(final CatalogTexture targetTexture) {
		shininess = null;
		for (HomePieceOfFurniture piece: inputList) {
			traversePieces(piece, targetTexture);
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

	private void traversePieces(final HomePieceOfFurniture piece, final CatalogTexture targetTexture) {
		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, targetTexture);
			}
		}
		else {
			// Include if either not matching anything, or it has materials with textures that match
			if (targetTexture == null || checkMaterials(piece, targetTexture)) {
				resultList.add(piece);
			}
		}	
	}

	private boolean checkMaterials(final HomePieceOfFurniture piece, final CatalogTexture targetTexture) {

		final HomeMaterial[] materials = piece.getModelMaterials();
		final HomeMaterial[] defaultMaterials = TextureMatcher.loadDefaultMaterials(piece);
		if (materials != null) {
			for (int i = 0; i < materials.length; i++) {
				// TODO consider URL as the unique ID rather than the name.
				if (materials[i] != null && materials[i].getTexture() != null && materials[i].getTexture().getName().equals(targetTexture.getName())) {
					if (materials[i].getShininess() != null) {
						shininess = materials[i].getShininess();
					}
					else if (defaultMaterials != null && defaultMaterials.length > i) {
						if (defaultMaterials[i].getShininess() != null) {
							shininess = defaultMaterials[i].getShininess();
						}
						
					}
					return true;
				}
//				else if (targetTexture instanceof UncatalogedTexture) {
//					if (i < defaultMaterials.length && defaultMaterials[i] != null && defaultMaterials[i].getName().equals(targetTexture.getName())) {
//						if (defaultMaterials[i].getShininess() != null) {
//							shininess = defaultMaterials[i].getShininess();
//						}
//						return true;
//					}
//				}
			}
		}
		return false;
	}	
}
