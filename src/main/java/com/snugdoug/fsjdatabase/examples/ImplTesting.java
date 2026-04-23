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

package com.snugdoug.fsjdatabase.examples;

import com.snugdoug.fsjdatabase.data.Parser;
import com.snugdoug.fsjdatabase.map.ParserMap;

import java.util.Map;

/**
 * This class contains things related to testing custom implementations such as {@link ParserMap}
 *
 * @since 1.3.1
 */
public class ImplTesting {
    public static void main(String[] args) {
        // create a new ParserMap
        ParserMap<String, String> parserMap = new ParserMap<>();

        // add Alice
        parserMap.put("firstName=Alice");

        System.out.println(parserMap);

        // replace Alice with Bob
        parserMap.replaceAll((Object key, Object value) -> {
            if(value.equals("Alice")) {
                return "Bob";
            } else {
                return value.toString();
            }
        });

        // print the updated list
        System.out.println(parserMap);
    }
}
