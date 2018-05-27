package com.eteks.digitaltrailstchange;

public interface UndoRedoCallback {
	public void undo(TextureOps textureOps);
	public void redo(TextureOps textureOps);
}