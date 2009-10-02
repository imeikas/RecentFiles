package com.meikas.eclipse.recentfiles.core.store;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

public class FileLinkTests {
	@Test
	public void testAddFileToStore() throws Exception {
		FileLinkStore fls = new FileLinkStore();
		assertEquals(0, fls.size());

		FileLink fileLink1 = new FileLink(new URI("1"));
		FileLink fileLink2 = new FileLink(new URI("2"));
		FileLink fileLink3 = new FileLink(new URI("3"));
		FileLink fileLink4 = new FileLink(new URI("4"));
		FileLink fileLink5 = new FileLink(new URI("5"));

		fls.addBookmark(fileLink1);
		assertEquals(1, fls.size());
		assertEquals(1, fls.getBookmarksCount());

		fls.addBookmark(new FileLink(new URI("1")));
		assertEquals(1, fls.size());
		assertEquals(1, fls.getBookmarksCount());

		fls.addBookmark(fileLink2);
		assertEquals(2, fls.size());
		assertEquals(2, fls.getBookmarksCount());

		fls.addBookmark(fileLink3);
		assertEquals(3, fls.size());

		assertEquals(fileLink3, fls.get(0));
		assertEquals(fileLink2, fls.get(1));
		assertEquals(fileLink1, fls.get(2));
		assertEquals(null, fls.get(3));
		assertEquals(null, fls.get(-1));

		assertArrayEquals(new Object[] { fileLink3, fileLink2, fileLink1 }, fls.getObjectList());

		fls.remove(fileLink2);
		assertArrayEquals(new Object[] { fileLink3, fileLink1 }, fls.getObjectList());

		fls.addBookmark(fileLink2);
		assertArrayEquals(new Object[] { fileLink2, fileLink3, fileLink1 }, fls.getObjectList());

		fls.addBookmark(fileLink1);
		assertArrayEquals(new Object[] { fileLink1, fileLink2, fileLink3 }, fls.getObjectList());

		fls.addBookmark(fileLink3);
		assertArrayEquals(new Object[] { fileLink3, fileLink1, fileLink2 }, fls.getObjectList());

		assertEquals(3, fls.getBookmarksCount());

		fls.add(fileLink4);
		assertArrayEquals(new Object[] { fileLink3, fileLink1, fileLink2, fileLink4 }, fls.getObjectList());

		fls.add(fileLink5);
		assertArrayEquals(new Object[] { fileLink3, fileLink1, fileLink2, fileLink5, fileLink4 }, fls.getObjectList());

	}

	@Test
	public void testFileLinkSerialization() throws Exception {
		FileLink fileLink1 = new FileLink(new URI("http://www.meikas.com:8080/test/url.html?get=5&post=13%34"));
		FileLink fileLink2 = new FileLink(new URI("file:/pub/workspaces/singleProjects/runtime-EclipseApplication/trest/sdsdsd/sdsdsd"));
		FileLink fileLink3 = new FileLink(new URI("file:/media/disk-4/boot.ini"));
		fileLink1.setBookmark(true);
		fileLink1.setRelative(true);

		assertEquals(fileLink1, FileLink.getDeserialized(fileLink1.getSerialized()));
		assertEquals(fileLink2, FileLink.getDeserialized(fileLink2.getSerialized()));
		assertEquals(fileLink3, FileLink.getDeserialized(fileLink3.getSerialized()));

	}

	@Test
	public void testFileLinkMethods() throws Exception {
		assertEquals(new FileLink(new URI(".")), new FileLink(new URI(".")));
		assertNotSame(new FileLink(new URI(".")), new FileLink(new URI("..")));

		assertFalse(new FileLink(new URI(".")).equals(new Object()));

		assertEquals("FileLink [bookmark=false, relative=false, uri=.]", new FileLink(new URI(".")).toString());

		assertEquals(".", new FileLink(new URI(".")).getFileName());

		assertEquals(new URI("."), new FileLink(new URI(".")).getFileUri());

		FileLink fileLink = new FileLink(new URI("."));
		fileLink.setRelative(true);
		assertTrue(fileLink.isRelative());

		fileLink.setRelative(false);
		assertFalse(fileLink.isRelative());

		fileLink.setBookmark(true);
		assertTrue(fileLink.isBookmark());

		fileLink.setBookmark(false);
		assertFalse(fileLink.isBookmark());

	}
}
