package org.openimaj.tools.imagecollection.collection;

import java.util.Iterator;

import org.openimaj.image.Image;

public class CountingImageCollectionIterator<T extends Image<?,T>> implements Iterator<ImageCollectionEntry<T>> {

	private Iterator<T> imageIterator;
	
	private int imageCount = 0;

	private ImageCollectionEntrySelection<T> selection;

	public CountingImageCollectionIterator(ImageCollectionEntrySelection<T> selection, Iterator<T> imageIterator) {
		this.imageIterator = imageIterator;
		this.selection = selection;
	}

	public CountingImageCollectionIterator(Iterator<T> imageIterator) {
		this.imageIterator = imageIterator;
		this.selection = new ImageCollectionEntrySelection.All<T>();
	}

	@Override
	public boolean hasNext() {
		return this.imageIterator.hasNext();
	}

	@Override
	public ImageCollectionEntry<T> next() {
		
		ImageCollectionEntry<T> entry = new ImageCollectionEntry<T>();
		T image = this.imageIterator.next();
		entry.image = image;
		entry.name = imageCount++ + "";
		entry.accepted = selection.acceptEntry(image);
		
		return entry;
	}

	@Override
	public void remove() {
		this.imageIterator.remove();
	}

}
