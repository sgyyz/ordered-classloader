package com.order.classloader.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.order.classloader.OrderedClassLoader;

public class OrderedClassLoaderTest {
    private static final String TARGET_FILE_PATTERN = "test-classloader-0.0.1-SNAPSHOT-%s.jar";
    private static final String TARGET_CLASS = "troy.young.Test";
    private static final String TARGET_METHOD = "test";

    @Test
    @Ignore
    public void testParentFirst() {
        OrderedClassLoader orderedClassLoader = new OrderedClassLoader(true);
        for (int i = 1; i <= 3; i++) {
            URL loadFile = preapreFile(i);
            orderedClassLoader.addURL(loadFile);
        }

        try {
            String actualResult = runMethod(orderedClassLoader);
            Assert.assertEquals(
                    "This should return " + String.format(TARGET_FILE_PATTERN, 1) + "'s test method return.",
                    "This is test-classloader-0.0.1-SNAPSHOT-1.jar", actualResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testChildFirst() {
        OrderedClassLoader orderedClassLoader = new OrderedClassLoader(false);
        for (int i = 1; i <= 3; i++) {
            URL loadFile = preapreFile(i);
            orderedClassLoader.addURL(loadFile);
        }

        try {
            String actualResult = runMethod(orderedClassLoader);
            Assert.assertEquals(
                    "This should return " + String.format(TARGET_FILE_PATTERN, 3) + "'s test method return.",
                    "This is test-classloader-0.0.1-SNAPSHOT-3.jar", actualResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testChildFirstAndAddNewFileDeleteOldFile() {
        // original class loader
        OrderedClassLoader orderedClassLoader = new OrderedClassLoader(false);
        for (int i = 1; i <= 3; i++) {
            URL loadFile = preapreFile(i);
            orderedClassLoader.addURL(loadFile);
        }
        
        // copy the first one file to last with new name
        try {
            orderedClassLoader.reload();
            
            File firstFile = new File(preapreFile(1).toURI());
            File fourthFile = new File(getNewFilePath(String.format(TARGET_FILE_PATTERN, 4)));
            if(fourthFile.exists()) {
                fourthFile.delete();
            }
            
            // create new file
            Files.copy(firstFile.toPath(), fourthFile.toPath());
            
            // remove old one
            firstFile.delete();
            
            // add new one to classloader
            orderedClassLoader.removeURL(firstFile.toURI().toURL());
            orderedClassLoader.addURL(fourthFile.toURI().toURL());
            
            // test the run result
            String actualResult = runMethod(orderedClassLoader);
            
            // recovery jar files.
            File recoveryFile = new File(getNewFilePath(String.format(TARGET_FILE_PATTERN, 1)));
            if(!recoveryFile.exists()) {
                Files.copy(fourthFile.toPath(), recoveryFile.toPath());
            }
            fourthFile.delete();
            
            // assert the result
            Assert.assertEquals(
                    "This should return " + String.format(TARGET_FILE_PATTERN, 1) + "'s test method return.",
                    "This is test-classloader-0.0.1-SNAPSHOT-1.jar", actualResult);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String runMethod(OrderedClassLoader orderedClassLoader)
            throws IOException, ClassNotFoundException, NoSuchMethodException,
            SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            InstantiationException {

        URLClassLoader classLoader = orderedClassLoader.reload();
        Class<?> targetClass = classLoader.loadClass(TARGET_CLASS);
        Method targetMethod = targetClass.getDeclaredMethod(TARGET_METHOD);
        Object result = targetMethod.invoke(targetClass.newInstance());
        return result.toString();
    }

    private URL preapreFile(int index) {
        try {
            URL url = OrderedClassLoaderTest.class.getClassLoader()
                    .getResource(String.format(TARGET_FILE_PATTERN, index));
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Can't load file: " + String.format(TARGET_FILE_PATTERN, index));
            return null;
        }
    }
    
    private String getNewFilePath(String fileName) {
        String path = OrderedClassLoaderTest.class.getClassLoader()
                .getResource("").toString() + fileName;
        path = path.substring(path.indexOf(":") + 1);
        return path;
    }
}
