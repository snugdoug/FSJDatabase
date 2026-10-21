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

package com.snugdoug.fsjdatabase.util.field;

import com.snugdoug.fsjdatabase.util.Logger;

import java.util.List;

/**
 * Allows FSJDatabase to create Fields out of thin air without needing the actual class
 */
public class SemiField {

    private final String type;
    private final String name;
    private final List<String> annotations;

    /**
     * Creates a new SemiField instance using a regular class
     * @param type the type
     * @param name the name
     * @param annotations the annotations of the field
     */
    public SemiField(Class<?> type, String name, List<String> annotations) {
        this.type = type.getName();
        this.name = name;
        this.annotations = annotations;
    }

    /**
     * Creates a new SemiField instance using a class name
     * @param fullClass the class name (type)
     * @param name the name
     * @param annotations the annotations of the field
     */
    public SemiField(String fullClass, String name, List<String> annotations) {
        switch (fullClass) {
            case "int" -> type = "java.lang.Integer";
            case "float" -> type = "java.lang.Float";
            case "long" -> type = "java.lang.Long";
            case "byte" -> type = "java.lang.Byte";
            default -> type = fullClass;
        }
        this.name = name;
        this.annotations = annotations;
    }

    /**
     * Get the type
     * @return {@link #type}
     */
    public String getType() {
        return type;
    }

    /**
     * Get the name
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Get the annotations
     * @return {@link #annotations}
     */
    public List<String> getAnnotations() {
        return annotations;
    }

    /**
     * Converts the SemiField to a string
     * @return the SemiField as a string
     */
    @Override
    public String toString() {
        return type + " " + name;
    }

    /**
     * Gets the {@link #type} as a regular class<br>
     * This can be {@code dangerous} as it is attempting to use {@code Class.forName}
     * @return the SemiField type as a normal class
     */
    public Class<?> getSemiFieldClass() {
        try {
            Logger.debug("Type: " + type);

            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find class of type: " + type + "\n" + e);
        }
    }

    /**
     * Checks if the SemiField has an annotation
     * @param annotation the annotation to look for
     * @return if it found the annotation
     */
    public boolean isAnnotationPresent(Class<?> annotation) {
        return annotations.contains(annotation.toString());
    }

    /**
     * Creates a new SemiField
     * @param type the type
     * @param name the name
     * @param annotations the annotations of the field
     * @return the new SemiField
     */
    public static SemiField of(Class<?> type, String name, List<String> annotations) {
        return new SemiField(type, name, annotations);
    }
}
