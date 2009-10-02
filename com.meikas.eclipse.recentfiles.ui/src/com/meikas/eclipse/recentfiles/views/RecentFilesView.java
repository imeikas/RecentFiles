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
import java.net.URISyntaxException;
import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.meikas.eclipse.recentfiles.Activator;
import com.meikas.eclipse.recentfiles.core.store.FileLink;
import com.meikas.eclipse.recentfiles.core.store.FileLinkStore;

/**
 * 
 * @author Ivar Meikas <ivar@codehoop.com>
 */

public class RecentFilesView extends ViewPart {

	private static final String BOOKMARKED_FILES = "bookmarkedFiles";

	private static FileLinkStore fileLinkStore = new FileLinkStore();

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.meikas.eclipse.recentfiles.views.RecentFilesView";

	private TableViewer viewer;
	private Action addAction;
	private Action toggleAction;
	private Action removeAction;
	private Action doubleClickAction;

	private boolean called = false;

	private TableColumn mainColumn;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	/**
	 * The constructor.
	 */
	public RecentFilesView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		addListeners();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		addListeners();
		super.init(site, memento);

		if (memento != null)
			restoreBookmarks(memento.getString(BOOKMARKED_FILES));

	}

	private void restoreBookmarks(String value) {
		if (value == null)
			return;

		String[] split = value.split(",");
		for (String string : split) {
			try {
				fileLinkStore.addBookmark(FileLink.getDeserialized(string));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void addListeners() {
		if (called)
			return;
		called = true;
		final IPartListener partListener = createPartListener();
		IPageListener pageListener = createPageListener(partListener);

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		window.addPageListener(pageListener);
		IWorkbenchPage[] pages = window.getPages();
		for (IWorkbenchPage p : pages) {
			p.addPartListener(partListener);
		}
	}

	private IPageListener createPageListener(final IPartListener partListener) {
		IPageListener pageListener = new IPageListener() {

			@Override
			public void pageOpened(IWorkbenchPage page) {
				page.addPartListener(partListener);
			}

			@Override
			public void pageClosed(IWorkbenchPage page) {
				page.removePartListener(partListener);
			}

			@Override
			public void pageActivated(IWorkbenchPage page) {
			}
		};
		return pageListener;
	}

	private IPartListener createPartListener() {
		final IPartListener partListener = new IPartListener() {

			@Override
			public void partOpened(IWorkbenchPart part) {
				updateList(part);
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}

			@Override
			public void partClosed(IWorkbenchPart part) {
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}

			@Override
			public void partActivated(IWorkbenchPart part) {
				updateList(part);
			}

			private void updateList(IWorkbenchPart part) {
				boolean isEditor = part instanceof IEditorPart;
				if (isEditor) {
					addFile((IEditorPart) part, false);
				}
			}
		};
		return partListener;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		// table.setHeaderVisible(true);
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		mainColumn = new TableColumn(table, SWT.LEFT);
		tc.setWidth(20);
		mainColumn.pack();
		viewer.setContentProvider(new ViewContentProvider(fileLinkStore));
		viewer.setLabelProvider(new ViewLabelProvider());
		// viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RecentFilesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(toggleAction);
		manager.add(new Separator());
		manager.add(addAction);
		manager.add(removeAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(toggleAction);
		manager.add(removeAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(toggleAction);
		manager.add(addAction);
		manager.add(removeAction);
	}

	private void makeActions() {

		createAddAction();
		createToggleAction();
		createRemoveAction();
		createDoubleClickAction();
	}

	private void createDoubleClickAction() {
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof FileLink) {
					FileLink f = (FileLink) obj;
					openEditor(f);
				}
			}
		};
	}

	private void createRemoveAction() {
		removeAction = new Action() {
			public void run() {
				int selectionStart = viewer.getTable().getSelectionIndex();

				removeSelectedLinks((IStructuredSelection) viewer.getSelection());
				setNewSelection(selectionStart);

				viewer.refresh();
			}

			private void setNewSelection(int selectionIndex) {
				int lastItemIndex = viewer.getTable().getItemCount() - 1;
				selectionIndex = Math.min(selectionIndex, lastItemIndex);
				viewer.getTable().setSelection(selectionIndex);
			}

			private void removeSelectedLinks(IStructuredSelection sel) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object obj = iterator.next();
					if (obj instanceof FileLink) {
						FileLink f = (FileLink) obj;
						fileLinkStore.remove(f);
					}
				}
			}
		};

		removeAction.setText("Remove file");
		removeAction.setToolTipText("Remove file from the list");
		ImageDescriptor deleteImage = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				"/icons/delete.png");
		removeAction.setImageDescriptor(deleteImage);
	}

	private void createToggleAction() {
		toggleAction = new Action() {

			public void run() {

				ISelection selection = viewer.getSelection();
				IStructuredSelection sel = (IStructuredSelection) selection;

				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object obj = iterator.next();
					if (obj instanceof FileLink) {
						FileLink f = (FileLink) obj;
						toggleBookmark(f);
					}
				}
			}

			private void toggleBookmark(FileLink f) {
				if (f.isBookmark()) {
					fileLinkStore.remove(f);
					f.setBookmark(false);
					fileLinkStore.add(f);
				} else {
					fileLinkStore.addBookmark(f);
				}
				viewer.refresh();
			}
		};
		toggleAction.setText("Toggle bookmark");
		toggleAction.setToolTipText("Toggle selection bookmark status");
		ImageDescriptor bookmarkImage = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				SharedImages.IMG_OBJS_BKMRK_TSK);
		toggleAction.setImageDescriptor(bookmarkImage);
	}

	private void createAddAction() {
		addAction = new Action() {

			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

				IEditorPart activeEditor = window.getActivePage().getActiveEditor();
				addFile(activeEditor, true);
			}
		};
		addAction.setText("Bookmark editor");
		addAction.setToolTipText("Add current editors file as bookmark");
		ImageDescriptor addImage = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/add.png");
		addAction.setImageDescriptor(addImage);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private static void openEditor(FileLink f) {
		IFileStore file = getFile(f);

		IFileInfo i = file.fetchInfo();
		if (i.exists() && !i.isDirectory()) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditorOnFileStore(page, file);
			} catch (PartInitException e) {
				// TODO Put your exception handler here if you wish to
			}
		} else {
			// TODO Do something if the file does not exist
		}

	}

	private static IFileStore getFile(FileLink f) {
		IFileStore file;
		if (f.isRelative()) {
			file = EFS.getLocalFileSystem().getStore(Platform.getLocation().append(f.getFileName()));
		} else {
			file = EFS.getLocalFileSystem().getStore(f.getFileUri());
		}
		return file;
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);

		StringBuffer buf = createSerializedLinks(fileLinkStore.getObjectList());

		memento.putString(BOOKMARKED_FILES, buf.toString());

	}

	private StringBuffer createSerializedLinks(Object[] objectList) {
		StringBuffer buf = new StringBuffer();
		for (Object object : objectList) {
			FileLink fl = (FileLink) object;
			if (fl.isBookmark())
				buf.append(fl.getSerialized()).append(",");
		}
		return buf;
	}

	private void addFile(IEditorPart activeEditor, boolean b) {
		IEditorInput ei = activeEditor.getEditorInput();

		if (ei instanceof IURIEditorInput) {
			IURIEditorInput urieditor = (IURIEditorInput) ei;
			URI uri = urieditor.getURI();
			FileLink fileLink = new FileLink(uri);
			fileLink.setRelative(!uri.isAbsolute());
			if (b)
				fileLinkStore.addBookmark(fileLink);
			else
				fileLinkStore.add(fileLink);
			viewer.refresh();
			mainColumn.pack();
		}
	}

}