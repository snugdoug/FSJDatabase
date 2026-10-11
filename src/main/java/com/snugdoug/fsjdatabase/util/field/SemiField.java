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

import java.util.List;

public class SemiField {

    private final String type;
    private final String name;
    private final List<String> annotations;

    public SemiField(Class<?> type, String name, List<String> annotations) {
        this.type = type.getName();
        this.name = name;
        this.annotations = annotations;
    }

    public SemiField(String fullClass, String name, List<String> annotations) {
        type = fullClass;
        this.name = name;
        this.annotations = annotations;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    public Class<?> getSemiFieldClass() {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find a base java class, your java is likely needs to see the graveyard or my code failed.\n" + e);
        }
    }

    public boolean isAnnotationPresent(Class<?> annotation) {
        return annotations.contains(annotation.toString());
    }

    public static SemiField of(Class<?> type, String name, List<String> annotations) {
        return new SemiField(type, name, annotations);
    }
}
