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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDE.SharedImages;

import com.meikas.eclipse.recentfiles.core.store.FileLink;

/**
 * @author Ivar Meikas <ivar@codehoop.com>
 */
class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	public ViewLabelProvider() {
	}

	public String getColumnText(Object obj, int index) {
		if (index != 1)
			return null;
		if (obj instanceof FileLink) {
			FileLink f = (FileLink) obj;
			String fileName = f.getFileName();
			String[] split = fileName.split("/");
			if (split.length > 0)
				return split[split.length - 1];
			else
				return fileName;
		}
		return getText(obj);
	}

	public Image getColumnImage(Object obj, int index) {
		if (index == 0) {
			if (obj instanceof FileLink && (((FileLink) obj).isBookmark())) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_BKMRK_TSK);
			}
			return null;
		} else if (index == 1)
			return getImage(obj);
		return null;

	}

	public Image getImage(Object obj) {
		if (obj instanceof FileLink) {
			URI fileUri = ((FileLink) obj).getFileUri();
			IFile[] filesForURI = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(fileUri);
			if (filesForURI.length > 0) {
				IContentType contentType = IDE.getContentType(filesForURI[0]);
				ImageDescriptor imageDescriptor = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(filesForURI[0].getName(), contentType);
				return imageDescriptor.createImage();
			}
		}
		return null;
	}
}