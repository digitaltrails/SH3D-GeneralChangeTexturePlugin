/*
 * ChangeTexturePlugin.java 1 Sep 2015
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

import com.eteks.sweethome3d.plugin.PluginAction;


public class ChangeTextureAction extends PluginAction {

	private final GeneralChangeTexturePlugin context;

	private ChangeTextureUI userInterface;

	public ChangeTextureAction(GeneralChangeTexturePlugin context) {
		this.context = context;
		putPropertyValue(Property.NAME, Local.str("ChangeTextureAction.pluginName"));
		putPropertyValue(Property.MENU, "Tools");
		setEnabled(true);
	}

	@Override
	public void execute() {
		userInterface = new ChangeTextureUI(context);
		userInterface.pack();
		userInterface.setVisible(true);
	}
}
