# SH3D-GeneralChangeTexturePlugin

## GeneralChangeTexturePlugin for SweetHome3D: find/change textures in use

The General Change Texture plugin changes one texture to another by traversing all furniture, 
furnature materials, wall sides, room ceilings, room floors, and room baseboards. 
The plugin can find and change catalog and non-catalog textures (non-catalog textures are 
most likely furniture default materials).

The plugin is useful in those situations where you want to trial different finishes for 
a large number of items. For example: choosing the right stain for refinishing all timber features; 
selecting a common stone for all benchtops; or choosing which wallpaper to use to several rooms. 
The plugin is also useful for determining and tidying up which textures are in use.

Undo/redo is supported, but being relatively new code, it is recommended that you make 
a backup copy of your home before using the plugin.

This plugin obsoletes the Change Texture Plugin which can only handle funiture.

### UI Screenshot

![screenshot](/src/screenshot.png?raw=true "GeneralChangeTexturePlugin UI")

## Requirements:
- [SweetHome3D](https://www.sweethome3d.com/) > 1.5
- Java/JDK >= 1.5

## Development:
- See [SweetHome3D - Plug-in developer's guide](https://www.sweethome3d.com/pluginDeveloperGuide.jsp)

## SourceForge SweetHome3D plugin submission link

[https://sourceforge.net/p/sweethome3d/plug-ins/21/](https://sourceforge.net/p/sweethome3d/plug-ins/21/)
