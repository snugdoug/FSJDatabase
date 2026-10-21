/*
 * Copyright (c) 2026 snugdoug
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.snugdoug.fsjdatabase.util.clazz;

import java.util.Objects;

/**
 * A pseudo class that allows FSJDatabase to create classes out of thin air without java saying "No."
 */
public class SemiClass {
    private final String className;

    /**
     * Creates a SemiClass instance off of a class name
     * @param className the class name
     */
    public SemiClass(String className) {
        this.className = className;
    }

    /**
     * Creates a SemiClass instance off of a regular class
     * @param clazz the class to create a SemiClass off of
     */
    public SemiClass(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    /**
     * Converts the SemiClass to a string
     * @return the SemiClass as a string
     */
    @Override
    public String toString() {
        return getClassName();
    }

    /**
     * Checks if the SemiClass is equal to another object
     * @param obj the reference object with which to compare.
     * @return if this SemiClass is equal to {@code obj}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if(!(obj instanceof SemiClass other))
            return false;
        return Objects.equals(className, other.className);
    }

    /**
     * Gets the class name
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * The same as {@link #getClassName()}, this exists for not wanting to change code reasons
     * @return the class name
     */
    public String getSimpleName() {
        return getClassName();
    }

    /**
     * Creates a new SemiClass instance off of a name
     * @param name the name of the new SemiClass
     * @return the SemiClass
     */
    public static SemiClass nameOf(String name) {
        return new SemiClass(name);
    }

}
