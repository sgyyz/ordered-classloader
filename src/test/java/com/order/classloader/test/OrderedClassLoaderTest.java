package com.order.classloader.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

import org.junit.Assert;
import org.junit.Test;

import com.order.classloader.OrderedClassLoader;

public class OrderedClassLoaderTest {
	private static final String PATH = "/Users/troy/tmp/test-project-0.0.1-SNAPSHOT-%s.jar";
	private static final String TARGET_CLASS = "troy.young.Test";
	private static final String TARGET_METHOD = "test";

	@Test
	public void testParentFirst() {
		OrderedClassLoader orderedClassLoader = new OrderedClassLoader(true);
		for (int i = 1; i <= 3; i++) {
			try {
				File loadFile = new File(String.format(PATH, i));
				orderedClassLoader.addURL(loadFile.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Assert.fail("Can't load the URL");
			}
		}

		try {
			runMethod(orderedClassLoader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testChildFirst() {
		OrderedClassLoader orderedClassLoader = new OrderedClassLoader(false);
		for (int i = 1; i <= 3; i++) {
			try {
				File loadFile = new File(String.format(PATH, i));
				orderedClassLoader.addURL(loadFile.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Assert.fail("Can't load the URL");
			}
		}

		try {
			runMethod(orderedClassLoader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runMethod(OrderedClassLoader orderedClassLoader)
			throws IOException, ClassNotFoundException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			InstantiationException {

		URLClassLoader classLoader = orderedClassLoader.reload();
		Class<?> targetClass = classLoader.loadClass(TARGET_CLASS);
		Method targetMethod = targetClass.getDeclaredMethod(TARGET_METHOD);
		targetMethod.invoke(targetClass.newInstance());
	}
}
