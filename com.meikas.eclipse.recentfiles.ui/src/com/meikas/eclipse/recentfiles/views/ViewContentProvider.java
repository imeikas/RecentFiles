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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.meikas.eclipse.recentfiles.core.store.FileLinkStore;

/**
 * @author Ivar Meikas <ivar@codehoop.com>
 */
class ViewContentProvider implements IStructuredContentProvider {

	private final FileLinkStore fileLinkStore;

	public ViewContentProvider(FileLinkStore fls) {
		this.fileLinkStore = fls;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		return fileLinkStore.getObjectList();
	}
}