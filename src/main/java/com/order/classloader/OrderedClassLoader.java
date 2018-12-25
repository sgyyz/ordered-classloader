package com.order.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderedClassLoader {
	/*
	 * save the original parent class loader, used to recovery the original
	 * status.
	 */
	private final ClassLoader originParentClassLoader;

	/*
	 * save all added URLs, use addURL or removeURL to change it.
	 */
	private List<URL> urls;

	/*
	 * figure out the order of URLs which need to be loaded.
	 */
	private boolean parentFirst;

	/*
	 * the class loader which need to attached after parent.
	 */
	private URLClassLoader attachedURLClassLoader;

	public OrderedClassLoader(boolean parentFirst) {
		this.originParentClassLoader = Thread.currentThread()
				.getContextClassLoader();
		this.urls = new ArrayList<URL>();
		this.parentFirst = parentFirst;
	}

	/**
	 * add URLs to list.
	 * 
	 * @param urls jar urls
	 */
	public void addURL(URL... urls) {
		this.urls.addAll(Arrays.asList(urls));
	}

	/**
	 * remove the URL for list, once delete the jar file.
	 * 
	 * @param removeURL jar urls need to remove
	 */
	public void removeURL(URL removeURL) {
		urls.remove(removeURL);
	}

	/**
	 * reload the attached URLClassLoader. <br/>
	 * firstly release the attachedURLClassLoader if it not null, just keep the
	 * origin parent class loader without reference. <br/>
	 * secondly, attach the new class loader to origin parent class loader by
	 * order.
	 * 
	 * @return URLClassLoader loaded new class loader
	 * @throws IOException normal io exception
	 */
	public URLClassLoader reload() throws IOException {
		// release previously class loader
		if (attachedURLClassLoader != null) {
			attachedURLClassLoader.close();
		}

		// reload all the class loader by order
		if (!this.parentFirst) {
			Collections.reverse(urls);
		}

		attachedURLClassLoader = new URLClassLoader(urls.toArray(new URL[urls
				.size()]), originParentClassLoader);

		return attachedURLClassLoader;
	}

}
