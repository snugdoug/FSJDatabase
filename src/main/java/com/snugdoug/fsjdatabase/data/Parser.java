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

package com.snugdoug.fsjdatabase.data;

import com.snugdoug.fsjdatabase.exception.InvalidParserData;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    /**
     * Converts text into data that FSJDB can use during writing data
     * <br><br>
     * Given String: firstName=name<br>
     * Output Map: (K)"firstName", (V)"name"
     *
     *
     * @param dataLine the data to be parsed
     * @return returns the parsed data
     */
    public static Map parse(String dataLine) {
        if(!isValid(dataLine))
            throw new InvalidParserData("Invalid data line: " + dataLine);

        Map<String, String> finalData = new HashMap();

        char[] part1 = dataLine.toCharArray();
        int i = 0;
        String[] parts = dataLine.split("\\s+(?=[^=]+=)");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2); // Limit split to 2 parts
            if (keyValue.length == 2) {
                finalData.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return finalData;
    }

    public static boolean isValid(String dataLine) {
        return dataLine.matches("^[a-zA-Z0-9_]+=[a-zA-Z0-9_\\\\s]+$");
    }
}
