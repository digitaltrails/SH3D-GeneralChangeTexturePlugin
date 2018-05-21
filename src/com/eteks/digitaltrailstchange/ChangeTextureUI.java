/*
 * ChangeTextureUI.java 19 Sept. 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Michael Hamilton / michael at actrix.gen.nz
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

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.eteks.sweethome3d.HomeFramePane;

public class ChangeTextureUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public ChangeTextureUI(GeneralChangeTexturePlugin context) {
		setTitle(Local.str("ChangeTextureAction.pluginName"));
	    setIconImage(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon.png")).getImage());

		
		final TextureChangePanel textureChangePanel = new TextureChangePanel(context);
		setContentPane(textureChangePanel);
	}
	

}
