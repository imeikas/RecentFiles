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
package com.meikas.eclipse.recentfiles.core.store;

import java.util.ArrayList;

/**
 * @author Ivar Meikas <ivar@codehoop.com>
 * 
 */
public class FileLinkStore {

	private ArrayList<FileLink> links = new ArrayList<FileLink>();

	private int bookmarksCount = 0;

	public void add(FileLink fileLink) {
		FileLink existingLink = findFirstOccurrance(fileLink);
		if (existingLink == null) {
			addRegularLink(fileLink);
		} else {
			moveUp(existingLink);
		}
	}

	private void addRegularLink(FileLink fileLink) {
		if (bookmarksCount > links.size())
			cleanUp();

		links.add(bookmarksCount, fileLink);
	}

	/**
	 * Something has gone horribly wrong and we need to clean up the mess.
	 */
	private void cleanUp() {
		bookmarksCount = 0;
		for (FileLink fl : links) {
			if (fl.isBookmark())
				bookmarksCount++;
		}

	}

	private void moveUp(FileLink link) {
		links.remove(link);
		if (link.isBookmark())
			links.add(0, link);
		else
			addRegularLink(link);
	}

	public void addBookmark(FileLink fileLink) {
		FileLink found = findFirstOccurrance(fileLink);

		if (found == null) {
			insertBookmark(fileLink);
		} else {
			replaceAndBookmarkLink(fileLink, found);
		}
	}

	public FileLink get(int i) {
		if (i >= links.size() || i < 0)
			return null;
		return links.get(i);
	}

	public int getBookmarksCount() {
		return bookmarksCount;
	}

	public Object[] getObjectList() {
		return links.toArray();
	}

	public void remove(FileLink f) {
		FileLink findFirst = findFirstOccurrance(f);
		if (findFirst == null)
			return;
		if (findFirst.isBookmark())
			bookmarksCount--;
		links.remove(findFirst);
	}

	public int size() {
		return links.size();
	}

	private FileLink findFirstOccurrance(FileLink fileLink) {
		for (FileLink l : links) {
			if (fileLink.getFileUri().equals(l.getFileUri())) {
				return l;
			}
		}
		return null;
	}

	private void insertBookmark(FileLink fileLink) {
		links.add(0, fileLink);
		bookmarksCount++;
	}

	private void replaceAndBookmarkLink(FileLink fileLink, FileLink found) {
		if (!links.get(links.indexOf(found)).isBookmark())
			bookmarksCount++;
		links.remove(found);
		links.add(0, fileLink);
		fileLink.setBookmark(true);

	}
}
