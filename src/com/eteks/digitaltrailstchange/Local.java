/*
 * Local.java 24 Sept. 2015
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

import java.util.Locale;
import java.util.ResourceBundle;

public final class Local {

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
				"com.eteks.digitaltrailstchange.ApplicationPlugin",
				Locale.getDefault(),
				Local.class.getClassLoader());
	
	static final String str(String key, Object...args) {
		final String str = BUNDLE.getString(key);
		if (str != null && args.length > 0) {
			return String.format(str, args);
		}      
		return str;
	}
}
