/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://backport175.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.java5;

import org.codehaus.backport175.reader.Annotation;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import sun.reflect.annotation.AnnotationType;
import test.reader.TestAnnotations;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AnnotationReaderTest extends TestCase {

    public AnnotationReaderTest(String name) {
        super(name);
    }

    public void testClassAnnReflection() {
        java.lang.annotation.Annotation[] annotations = Target5.class.getAnnotations();
        assertEquals(1, annotations.length);
    }

    public void testMethodAnnReflection() {
        java.lang.annotation.Annotation[] annotations = Target5.METHOD.getAnnotations();
        assertEquals(1, annotations.length);
    }

    public void testJava5ClassAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.class
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5ConstructorAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.CONSTRUCTOR
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5MethodAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.METHOD
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5FieldAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.FIELD
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    // === for testing Java 5 reflection compatibility ===

    public void testAnnotationCCompiledClassAnnReflection() {
        java.lang.annotation.Annotation[] annotations = test.Target.class.getAnnotations();
        // only one is set with RuntimeRetention..
        assertEquals(1, annotations.length);
        //TODO refine the assert
    }


    //TODO refine the assert
    public void testAnnotationCCompiledMembersAnnReflection() {
        java.lang.annotation.Annotation[] annotations = test.Target.METHOD.getAnnotations();
        assertTrue(annotations.length > 0);

        annotations = test.Target.FIELD.getAnnotations();
        assertTrue(annotations.length > 0);

        annotations = test.Target.CONSTRUCTOR.getAnnotations();
        assertTrue(annotations.length > 0);
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationReaderTest.class);
    }
}