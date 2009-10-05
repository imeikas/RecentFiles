/*******************************************************************************
 * Copyright (c) 2009 Ivar Meikas, Codehoop OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ivar Meikas
 *******************************************************************************/
package com.meikas.eclipse.recentfiles.views;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDE.SharedImages;

import com.meikas.eclipse.recentfiles.Activator;
import com.meikas.eclipse.recentfiles.core.store.FileLink;

/**
 * @author Ivar Meikas <ivar@codehoop.com>
 */
class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final int BOOKMARK_COLUMN = 0;
	private static final int FILENAME_COLUMN = 1;

	public ViewLabelProvider() {
	}

	public String getColumnText(Object obj, int index) {
		if (index != 1)
			return null;
		if (obj instanceof FileLink) {
			return calculateFileDisplayName((FileLink) obj);
		}
		return getText(obj);
	}

	public Image getColumnImage(Object obj, int column) {
		if (obj instanceof FileLink)
			return getFilelinkImage(column, (FileLink) obj);

		return null;

	}

	private String calculateFileDisplayName(FileLink f) {
		String fileName = f.getFileName();
		String[] split = fileName.split("/");
		if (split.length > 0)
			return split[split.length - 1];
		else
			return fileName;
	}

	private Image getFilelinkImage(int column, FileLink fileLink) {
		switch (column) {
		case BOOKMARK_COLUMN:
			return getBookmarkImage(fileLink);

		case FILENAME_COLUMN:
			return getContentSpecificImage(fileLink.getFileUri());

		default:
			return null;
		}
	}

	private Image getBookmarkImage(FileLink fileLink) {
		if (fileLink.isBookmark()) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_BKMRK_TSK);
		}
		return null;
	}

	private Image getContentSpecificImage(URI fileUri) {
		IFile[] filesForURI = getFilesForUri(fileUri);

		if (filesForURI != null && filesForURI.length > 0) {
			return getImage(filesForURI[0]);
		}

		return null;
	}

	private IFile[] getFilesForUri(URI fileUri) {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(fileUri);
		} catch (IllegalArgumentException e) {
			Activator.logErrorMessage("Finding files failed \"" + fileUri+"\"", e);
		}

		return null;
	}

	private Image getImage(IFile file) {
		IContentType contentType = IDE.getContentType(file);

		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		ImageDescriptor imageDescriptor = editorRegistry.getImageDescriptor(file.getName(), contentType);

		return imageDescriptor.createImage();
	}
}