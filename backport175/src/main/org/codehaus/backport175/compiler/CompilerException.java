/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Thrown when error in compilation of the annotations.
 * Those errors should interrupts the compilation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class CompilerException extends RuntimeException {
    /**
     * Original exception which caused this exception.
     */
    protected Throwable m_originalException;

    /**
     * Optional location hint
     */
    protected SourceLocation m_sourceLocation;

    /**
     * Sets the message for the exception.
     *
     * @param message the message
     */
    public CompilerException(final String message) {
        super(message);
    }

    /**
     * Sets the message and location for the exception.
     *
     * @param message the message
     * @param sourceLocation
     */
    public CompilerException(final String message, final SourceLocation sourceLocation) {
        super(message);
        m_sourceLocation = sourceLocation;
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public CompilerException(final String message, final Throwable throwable) {
        super(message);
        m_originalException = throwable;
    }

    /**
     * Print the full stack trace, including the original exception.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param ps the byte stream in which to print the stack trace
     */
    public void printStackTrace(final PrintStream ps) {
        super.printStackTrace(ps);
        if (m_originalException != null) {
            m_originalException.printStackTrace(ps);
        }
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param pw the character stream in which to print the stack trace
     */
    public void printStackTrace(final PrintWriter pw) {
        super.printStackTrace(pw);
        if (m_originalException != null) {
            m_originalException.printStackTrace(pw);
        }
    }

    /**
     * Returns the source location for the exception.
     *
     * @return
     */
    public SourceLocation getLocation() {
        return m_sourceLocation;
    }
}