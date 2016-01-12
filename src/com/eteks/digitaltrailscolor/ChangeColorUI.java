/*
 * ChangeColorPlugin 7 Jan. 2016
 *
 * Sweet Home 3D, Copyright (c) 2016 Michael Hamilton / michael at actrix.gen.nz
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
package com.eteks.digitaltrailscolor;

import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.eteks.sweethome3d.HomeFramePane;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

public class ChangeColorUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public ChangeColorUI(ChangeColorPlugin context) {
		setTitle("Change Colors");
	    setIconImage(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon.png")).getImage());
		
		final List<HomePieceOfFurniture> furnitureList = context.getHome().getFurniture();
		
		final List<Room> roomList = context.getHome().getRooms();
		
		final Collection<Wall> wallList = context.getHome().getWalls();
		
		final ColorChangePanel colorChangePanel = new ColorChangePanel(roomList, wallList, furnitureList, context);
		setContentPane(colorChangePanel);
	}
	
}
