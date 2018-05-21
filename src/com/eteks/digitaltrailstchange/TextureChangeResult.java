package com.eteks.digitaltrailstchange;

import javax.swing.undo.UndoableEdit;

public class TextureChangeResult {

	private final UndoableEdit undoableEdit;
	private final int changedCount;
	
	public TextureChangeResult(UndoableEdit undoableEdit, int changedCount) {
		super();
		this.undoableEdit = undoableEdit;
		this.changedCount = changedCount;
	}
	
	public UndoableEdit getUndoableEdit() {
		return undoableEdit;
	}

	public int getChangedCount() {
		return changedCount;
	}

}
