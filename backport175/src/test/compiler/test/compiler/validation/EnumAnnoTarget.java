/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.compiler.validation;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class EnumAnnoTarget {

    /**
     * @test.compiler.validation.ValidationTest.EnumAnno(test.compiler.validation.MyEnum.class)
     */
    public void nonWellFormedAnno0() {
    }

    /**
     * @test.compiler.validation.ValidationTest.EnumAnno(test.compiler.validation.MyEnum.WALUE)
     */
    public void nonWellFormedAnno1() {
    }

    /**
     * @test.compiler.validation.ValidationTest.EnumAnno(123)
     */
    public void nonWellFormedAnno2() {
    }
}