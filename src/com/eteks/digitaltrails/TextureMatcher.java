package com.eteks.digitaltrails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	// Controls non-working experimental feature to include non-catalog textures in the from list.
	private static final boolean INCLUDE_DEFAULT_TEXTURES = System.getenv("CTP_INCLUDE_DEFAULT_TEXTURES") != null;

	final List<CatalogTexture> catalogTextureList;	
	final Map<String, CatalogTexture> catalogIndex;
	final Map<String, NonCatalogTexture> nonCatalogIndex;

	public TextureMatcher(List<CatalogTexture> catalogTextureList) {
		this.catalogTextureList = catalogTextureList;
		catalogIndex = new HashMap<String, CatalogTexture>();
		nonCatalogIndex = new HashMap<String, NonCatalogTexture>();
		for (CatalogTexture texture: catalogTextureList) {
			catalogIndex.put(texture.getName(), texture);
		}
	}

	public List<CatalogTexture> findUsedBy(final List<HomePieceOfFurniture> furnitureList) {

		final Set<CatalogTexture> resultSet = new HashSet<CatalogTexture>();
		final Set<NonCatalogTexture> nctResultSet = new HashSet<NonCatalogTexture>();

		for (HomePieceOfFurniture piece: furnitureList) {
			traversePieces(piece, resultSet, nctResultSet);
		}

		final ArrayList<CatalogTexture> finalResults = new ArrayList<CatalogTexture>(resultSet.size() + nctResultSet.size());

		for (CatalogTexture inOrderTexture: catalogTextureList) {
			if (resultSet.contains(inOrderTexture)) {
				finalResults.add(inOrderTexture);
			}
		}

		finalResults.addAll(nctResultSet);

		return finalResults; 
	}

	private void traversePieces(final HomePieceOfFurniture piece, final Set<CatalogTexture> resultSet, final Set<NonCatalogTexture> nctResultSet) {
		if (piece instanceof HomeFurnitureGroup) {
			// Recurse into the group
			HomeFurnitureGroup group = (HomeFurnitureGroup) piece;
			for (HomePieceOfFurniture member : group.getFurniture() ) {
				traversePieces(member, resultSet, nctResultSet);
			}
		}
		else {
			final HomeMaterial[] materials = piece.getModelMaterials();
			final HomeMaterial[] defaultMaterials = loadDefaultMaterials(piece);
			if (materials != null) {
				for (HomeMaterial material: materials) {
					if (material != null && material.getTexture() != null) {
						final String textureName = material.getTexture().getName();
						if (catalogIndex.containsKey(textureName)) {
							resultSet.add(catalogIndex.get(textureName));
						}
						else {
							// Orphaned texture (no longer in catalog - probably a deleted user texture. 
							nctResultSet.add(findNonCatalogTexture(material));
						}
					}
				}
			}
			if (defaultMaterials != null) {
				if (INCLUDE_DEFAULT_TEXTURES) {
					for (HomeMaterial material: defaultMaterials) {
						if (material != null && material.getTexture() != null) {
							nctResultSet.add(findNonCatalogTexture(material));
						}
					}
				}
			}
		}	
	}

	public NonCatalogTexture findNonCatalogTexture(final HomeMaterial material) {
		final String key = material.getTexture().getName() != null ? material.getTexture().getName() : material.getName();
		final NonCatalogTexture texture;
		if (!nonCatalogIndex.containsKey(key)) {
			texture = new NonCatalogTexture(key, material.getTexture());
			nonCatalogIndex.put(key, texture);
		}
		else {
			texture = nonCatalogIndex.get(key);
		}
		return texture;
	}

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

	static class NonCatalogTexture extends CatalogTexture {
		private static final long serialVersionUID = 1L;
		private static final TexturesCategory INBUILT_DEFAULT = new TexturesCategory(Local.str("TextureMatcher.NonCatalogCategoryName"));

		public NonCatalogTexture(final String key, final HomeTexture texture) {
			super(key, texture.getImage(), texture.getWidth(), texture.getHeight());
		}

		public String getMaterialName() {
			return getName();
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
