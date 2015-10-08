package com.eteks.digitaltrails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.media.j3d.BranchGroup;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TexturesCategory;

public final class TextureMatcher {

	final List<CatalogTexture> catalogTextureList;	
	final Map<String, CatalogTexture> catalogIndex;

	public TextureMatcher(List<CatalogTexture> catalogTextureList) {
		this.catalogTextureList = catalogTextureList;
		this.catalogIndex = new HashMap<String, CatalogTexture>();
		for (CatalogTexture texture: catalogTextureList) {
			catalogIndex.put(texture.getName(), texture);
		}
	}

	public List<CatalogTexture> findUsedBy(final List<HomePieceOfFurniture> furnitureList) {
	

		final Set<CatalogTexture> resultSet = new HashSet<CatalogTexture>();

		for (HomePieceOfFurniture piece: furnitureList) {
			traversePieces(piece, resultSet);
		}
				
//		final List<UncatalogedTexture> defaultTextures = getDefaultTextures(furnitureList);
//		
//		for (CatalogTexture defaultTexture: defaultTextures) {
//			results.add(defaultTexture);
//		}
		
		return new ArrayList<CatalogTexture>(resultSet); 
	}

	private void traversePieces(final HomePieceOfFurniture piece, Set<CatalogTexture> resultSet) {
		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, resultSet);
			}
		}
		else {
			final HomeMaterial[] materials = piece.getModelMaterials();
			if (materials != null) {
				for (HomeMaterial material: materials) {
					if (material != null && material.getTexture() != null) {
						final String textureName = material.getTexture().getName();
						if (catalogIndex.containsKey(textureName)) {
							resultSet.add(catalogIndex.get(textureName));
						}
						else {
							//results.add(new UncatalogedTexture(material.getName() + "_Texture", material.getTexture()));
						}
					}
				}
			}
		}	
	}
	
	public static HomeMaterial[] loadDefaultMaterials(HomePieceOfFurniture piece) {
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

	private List<UncatalogedTexture> getDefaultTextures(final List<HomePieceOfFurniture> furnitureList) {
		final List<UncatalogedTexture> results = new ArrayList<TextureMatcher.UncatalogedTexture>();

		final Map<String, HomeTexture> homeTextures = getDefaultHomeTextures(furnitureList);
		for (Entry<String, HomeTexture> entry :homeTextures.entrySet()) {
			results.add(new UncatalogedTexture(entry.getKey(), entry.getValue()));
		}
		return results;
	}

	private Map<String, HomeTexture> getDefaultHomeTextures(final List<HomePieceOfFurniture> furnitureList) {
		final Map<String, HomeTexture> results = new HashMap<String, HomeTexture>();
		for (HomePieceOfFurniture piece: furnitureList) {
			final HomeMaterial[] materials = piece.getModelMaterials();
			final HomeMaterial[] defaultMaterials = loadDefaultMaterials(piece);
			if (defaultMaterials != null) {
				for (int i = 0; i < defaultMaterials.length; i++) {

					// TODO consider URL as the unique ID rather than the name.
					if (materials != null && materials[i] != null ) {
						// Has overridden the default at this position.
					}
					else if (defaultMaterials != null && defaultMaterials[i] != null) {
						final HomeMaterial defaultMaterial = defaultMaterials[i];
						if (defaultMaterial.getTexture() != null && !results.containsKey(defaultMaterial.getName())) {
							results.put(defaultMaterial.getName(), defaultMaterial.getTexture());
						}
					}
					else {
						// No texture at this position.
					}
				}
			}
		}
		return results;
	}

	static class UncatalogedTexture extends CatalogTexture {
		private static final long serialVersionUID = 1L;
		private static final TexturesCategory INBUILT_DEFAULT = new TexturesCategory("Inbuilt");
		public UncatalogedTexture(final String materialName, final HomeTexture texture) {
			super(materialName, texture.getImage(), texture.getWidth(), texture.getHeight());
		}
		@Override
		public TexturesCategory getCategory() {
			return INBUILT_DEFAULT;
		}
		@Override
		public String getCreator() {
			return "Anon";
		}
		
	
			 
	}
}
