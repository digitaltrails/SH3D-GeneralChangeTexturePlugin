/*
 * NonCatalogTexture.java 21 May 2017
 *
 * Copyright (c) 2017 Michael Hamilton / michael at actrix.gen.nz
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