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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * @author Ivar Meikas <ivar@codehoop.com>
 *
 */
public class FileLink {

	private final URI uri;
	private boolean relative = false;
	private boolean bookmark;

	public FileLink(URI uri) {
		this.uri = uri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (bookmark ? 1231 : 1237);
		result = prime * result + (relative ? 1231 : 1237);
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileLink other = (FileLink) obj;
		if (bookmark != other.bookmark)
			return false;
		if (relative != other.relative)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileLink [bookmark=" + bookmark + ", relative=" + relative + ", uri=" + uri + "]";
	}

	
	public String getFileName() {
		return uri.getPath();
	}

	public void setRelative(boolean relative) {
		this.relative = relative;
	}

	public boolean isRelative() {
		return relative;
	}

	public URI getFileUri() {
		return uri;
	}

	public String getSerialized() {

		return MessageFormat.format("{0}??{1}??{2}", uri, relative, bookmark);
	}

	public static FileLink getDeserialized(String serialized) throws URISyntaxException {
		String[] split = serialized.split("\\?\\?");
		
		FileLink fileLink = new FileLink(new URI(split[0]));
		
		if (split.length > 1)
			fileLink.setRelative(Boolean.parseBoolean(split[1]));
		
		if (split.length > 2)
			fileLink.setBookmark(Boolean.parseBoolean(split[2]));
		
		return fileLink;
	}

	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;

	}

	public boolean isBookmark() {
		return bookmark;
	}

}
