package com.eteks.digitaltrailstchange;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TexturesCategory;

class NonCatalogTexture extends CatalogTexture {
	private static final long serialVersionUID = 1L;
	private static final TexturesCategory INBUILT_DEFAULT = new TexturesCategory(Local.str("TextureMatcher.NonCatalogCategoryName"));

	public NonCatalogTexture(final String id, final HomeTexture texture) {
		super(id, id, texture.getImage(), texture.getWidth(), texture.getHeight(), "internal");
	}

	public String getMaterialName() {
		return getName();
	}

	@Override
	public TexturesCategory getCategory() {
		return INBUILT_DEFAULT;
	}
		 
}