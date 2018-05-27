/*
 * TextureChangePanel.java 19 Sept. 2015
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.model.CatalogTexture;

public class TextureChangePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final TextureSelectionPanel fromSelectionPanel;
	private final TextureSelectionPanel toSelectionPanel;
	private final ModelItemSelectionPanel modelItemSelectionPanel;
	private final ShininessSelectionPanel shininessSelectionPanel;
	private final JButton ok;
	private final JButton close;
	private final JLabel status;

	private boolean handling = false;
	
	private boolean selectingFromAll = true;

	private final TextureOps textureOps;

	private GeneralChangeTexturePlugin pluginContext;

	public TextureChangePanel(final GeneralChangeTexturePlugin pluginContext) {

		this.pluginContext = pluginContext;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Show all textures used by all furniture in use.
		// final List<CatalogTexture> texturesInUse = new TextureMatcher(catalogTextureList).findUsedBy(furnitureList);
		
		textureOps = new TextureOps(
				pluginContext.getHome(), 
				pluginContext.getUserPreferences(), 
				new UndoRedoCallback() {
					@Override
					public void undo(TextureOps textureOps) {
						textureOps.refresh();
						if (selectingFromAll) {
							fromSelectionPanel.setListData(textureOps.findAllCatalogTextures());
							modelItemSelectionPanel.setListData(new ArrayList<TextureUse>());
						}
						else {
							final List<CatalogTexture> texturesInUse = textureOps.findTexturesBeingUsed();
							fromSelectionPanel.setListData(texturesInUse);
							modelItemSelectionPanel.setListData(new ArrayList<TextureUse>());
						}
					}

					@Override
					public void redo(TextureOps textureOps) {
						undo(textureOps); // Same as undo
					}
				});
		
		fromSelectionPanel = new TextureSelectionPanel(Local.str("TextureChangePanel.fromTexture"));
		fromSelectionPanel.setListData(textureOps.findTexturesBeingUsed());
		selectingFromAll = false;
		toSelectionPanel = new TextureSelectionPanel(Local.str("TextureChangePanel.toTextureTo"));
		toSelectionPanel.setListData(textureOps.findAllCatalogTextures());
				
		modelItemSelectionPanel = new ModelItemSelectionPanel();

		fromSelectionPanel.addPopupAction(
				"ShowAll", 
				Local.str("TextureSelectionPanel.popup.showAllLabel"), 
				new TextureSelectionPanel.TextureSelectionAction() {						
					@Override
					public void actionPerformed(final String actionName, final CatalogTexture list) {	
						selectingFromAll = true;
						fromSelectionPanel.setListData(textureOps.findAllCatalogTextures());
						modelItemSelectionPanel.setListData(new ArrayList<TextureUse>());
					}
				});

		fromSelectionPanel.addPopupAction(
				"ShowUsed", 
				Local.str("TextureSelectionPanel.popup.showUsedLabel"), 
				new TextureSelectionPanel.TextureSelectionAction() {						
					@Override
					public void actionPerformed(final String actionName, final CatalogTexture list) {
						selectingFromAll = false;
						final List<CatalogTexture> texturesInUse = textureOps.findTexturesBeingUsed();
						fromSelectionPanel.setListData(texturesInUse);
						modelItemSelectionPanel.setListData(new ArrayList<TextureUse>());
					}
				});

		toSelectionPanel.addPopupAction(
				"ShowAll", 
				Local.str("TextureSelectionPanel.popup.showAllLabel"), 
				new TextureSelectionPanel.TextureSelectionAction() {						
					@Override
					public void actionPerformed(final String actionName, final CatalogTexture list) {	
						toSelectionPanel.setListData(textureOps.findAllCatalogTextures());
					}
				});

		toSelectionPanel.addPopupAction(
				"ShowUsed", 
				Local.str("TextureSelectionPanel.popup.showUsedLabel"), 
				new TextureSelectionPanel.TextureSelectionAction() {						
					@Override
					public void actionPerformed(final String actionName, final CatalogTexture list) {
						final List<CatalogTexture> texturesInUse = textureOps.findTexturesBeingUsed();
						toSelectionPanel.setListData(texturesInUse);
					}
				});
		
		
		modelItemSelectionPanel.addPopupAction(
				"ShowAll", 
				Local.str("FurnitureSelectionPanel.popup.showAllLabel"), 
				new ModelItemSelectionPanel.ModelItemSelectionAction() {			
					@Override
					public void actionPerformed(final String actionName, final TextureUse list) {
						modelItemSelectionPanel.setListData(textureOps.findAllReferencesToTextures());
					}
				});


		shininessSelectionPanel = new ShininessSelectionPanel();

		final JPanel gridThirdPanel = new JPanel(new BorderLayout());
		gridThirdPanel.add(modelItemSelectionPanel, BorderLayout.CENTER);
		gridThirdPanel.add(shininessSelectionPanel, BorderLayout.PAGE_END);

		final JPanel middlePanel = new JPanel(new GridLayout(1,3));
		middlePanel.add(fromSelectionPanel);
		middlePanel.add(toSelectionPanel);
		middlePanel.add(gridThirdPanel);

		add(middlePanel, BorderLayout.CENTER);

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(1,3,30,0));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ok = new JButton(Local.str("TextureChangePanel.ok"));
		close = new JButton(Local.str("TextureChangePanel.close"));
		status = new JLabel("");
		bottomPanel.add(ok);
		bottomPanel.add(close);
		bottomPanel.add(status);

		add(bottomPanel, BorderLayout.PAGE_END);

		status.setText(Local.str("ChangeTexturePlugin.version"));
		
		fromSelectionPanel.addSelectionListener(new ListSelectionListener() {				
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!handling) {
					try {
						handling = true;

						// Scroll the second panel to the same textures.
						final int count;
						final CatalogTexture selectedTexture = fromSelectionPanel.getSelectedTexture();
						if (selectedTexture != null) {

							// Scroll the second panel to the same textures.
							toSelectionPanel.ensureIsVisible(fromSelectionPanel.getFirstVisibleIndex(), fromSelectionPanel.getLastVisibleIndex(), selectedTexture.getName());

							// Show referenced furniture
					
							final List<TextureUse> references = textureOps.findItemsUsingTexture(selectedTexture);
							
							final Float shininess = null;
							modelItemSelectionPanel.setListData(references);
							modelItemSelectionPanel.selectAll();
							if (!references.isEmpty()) {
								shininessSelectionPanel.update(shininess);
							}
							count = references.size();
						}
						else {
							modelItemSelectionPanel.setListData(new ArrayList<TextureUse>());
							count = 0;
						}
						status.setText(Local.str("TextureChangePanel.furnitureSelectedCount", count));
					}
					finally {
						handling = false;
					} 
				}
			}
		});

		toSelectionPanel.addSelectionListener(new ListSelectionListener() {				
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!handling) {
					try {
						handling = true;
						// Check if anything is using this texture
						final CatalogTexture selectedTexture = toSelectionPanel.getSelectedTexture();
						if (selectedTexture != null) {
							final List<TextureUse> references = textureOps.findItemsUsingTexture(selectedTexture);
							status.setText( Local.str("TextureChangePanel.otherFurnatureUsesTexture", references.size()));
						}
					}
					finally {
						handling = false;
					} 
				}
			}
		});

		modelItemSelectionPanel.addSelectionListener(new ListSelectionListener() {				
			@Override
			public void valueChanged(ListSelectionEvent e) {	
				if (!handling) {
					try {
						handling = true;

						status.setText(Local.str("TextureChangePanel.furnitureSelectedCount", modelItemSelectionPanel.getSelectedItems().size()));
						final List<TextureUse> selectedItems = modelItemSelectionPanel.getSelectedItems();
						if (selectedItems.isEmpty()) {
							final List<CatalogTexture> texturesInUse = textureOps.findTexturesBeingUsed();
							fromSelectionPanel.setListData(texturesInUse);
							//fromSelectionPanel.setListData(catalogTextureList);
						}
						else {
							final List<CatalogTexture> matches = textureOps.findUsedBy(selectedItems);
							fromSelectionPanel.setListData(matches);
							//furnitureSelectionPanel.setChoices(selectedFurniture);
						}
					}
					finally {
						handling = false;
					} 
				}
			}});

		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final CatalogTexture fromTexture = fromSelectionPanel.getSelectedTexture();
				final CatalogTexture toTexture = toSelectionPanel.getSelectedTexture();
				List<TextureUse> list = modelItemSelectionPanel.getSelectedItems();
				final int changedCount = changeTexture(list, fromTexture, toTexture, shininessSelectionPanel.getShininess());
				if (changedCount > 0) {
					final List<CatalogTexture> texturesInUse = textureOps.findUsedBy(list);				
					fromSelectionPanel.setListData(texturesInUse);
					fromSelectionPanel.setSelected(toTexture);
					toSelectionPanel.setSelected(null);
				}
				status.setText(Local.str("TextureChangePanel.modified", changedCount));
			}
		});

		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Component comp = SwingUtilities.getRoot(TextureChangePanel.this);
				((Window) comp).dispose();
			}
		});
	}

	private int changeTexture(
			final List<TextureUse> list, 
			final CatalogTexture fromTexture, 
			final CatalogTexture toTexture, 
			final Float shininess) {

		if (fromTexture == null) {
			JOptionPane.showMessageDialog(this, Local.str("TextureChangePanel.noFromTextureSelected"));							
			return 0;
		}
		
		if (toTexture == null) {
			JOptionPane.showMessageDialog(this, Local.str("TextureChangePanel.noToTextureSelected"));							
			return 0;
		}
		

		final TextureChangeResult result = textureOps.change(list, fromTexture, toTexture, shininess);
		pluginContext.getUndoableEditSupport().postEdit(result.getUndoableEdit());
				
		return result.getChangedCount();
	}
}