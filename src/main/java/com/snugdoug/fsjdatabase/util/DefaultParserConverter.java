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

package com.snugdoug.fsjdatabase.util;

import com.google.common.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DefaultParserConverter implements ParserConverter {
    private final Map<Class, Function<String, ?>> registry = new HashMap<>();

    public DefaultParserConverter() {
        registry.put(String.class, s -> s);
        registry.put(Integer.class, Integer::parseInt);
        registry.put(Long.class, Long::parseLong);
        registry.put(Float.class, Float::parseFloat);
        registry.put(Double.class, Double::parseDouble);
        registry.put(Boolean.class, Boolean::parseBoolean);
        registry.put(Byte.class, Byte::parseByte);
        registry.put(Short.class, Short::parseShort);
    }


    /**
     * Converts a type string to another type
     *
     * @param input      the string to convert
     * @param targetType the target type to convert the input str to
     * @return           returns the modified input
     * @param <T>        This makes it so you can actually get an output
     */
    @Override
    public <T> T convert(String input, TypeToken<T> targetType) {

        Function<String, ?> func = registry.get(targetType.getRawType());

        if (func == null) {
            throw new UnsupportedOperationException("No converter registered for " + targetType.getRawType());
        }

        return (T) func.apply(input);
    }
}
